package com.example.bankcards.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardTransferRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.CryptoConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository repo;
    private final CardTransferRepository transferRepo;
    private final CryptoConverter crypto = new CryptoConverter();

    public CardServiceImpl(CardRepository repo, CardTransferRepository transferRepo) {
        this.repo = repo;
        this.transferRepo = transferRepo;
    }
    @Override
    public CardDto create(CreateCardRequest req) {
        String username = currentUsername();
        return createForOwner(username, req);
    }
    @Override
    public CardDto createForOwner(String ownerUsername, CreateCardRequest req) {
        var card = new Card();
        card.setPanEncrypted(crypto.convertToDatabaseColumn(req.pan()));
        card.setOwner(ownerUsername);
        card.setExpiry(req.expiry());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        var saved = repo.save(card);
        var pan = crypto.convertToEntityAttribute(saved.getPanEncrypted());
        return CardMapper.toDto(saved, pan);
    }
    @Override
    public Page<CardDto> listByOwner(String owner, String[] statuses, Pageable pageable) {
        var username = owner != null ? owner : currentUsername();
        var st = statuses == null || statuses.length == 0
                ? new CardStatus[]{CardStatus.ACTIVE, CardStatus.BLOCKED, CardStatus.EXPIRED}
                : java.util.Arrays.stream(statuses)
                .map(String::toUpperCase)
                .map(CardStatus::valueOf)
                .toArray(CardStatus[]::new);

        return repo.findByOwnerIgnoreCaseAndStatusIn(username, st, pageable)
                .map(c -> CardMapper.toDto(c, crypto.convertToEntityAttribute(c.getPanEncrypted())));
    }

    @Override
    public void block(Long id) {
        var c = getOr404(id);
        if (c.getStatus() == CardStatus.BLOCKED) throw new BadRequestException("Already blocked");
        if (c.getStatus() == CardStatus.EXPIRED) throw new BadRequestException("Expired card cannot be blocked");
        c.setStatus(CardStatus.BLOCKED);
        repo.save(c);
    }
    @Override
    public void activate(Long id) {
        var c = getOr404(id);
        if (c.getStatus() == CardStatus.ACTIVE) throw new BadRequestException("Already active");
        if (c.getExpiry().isBefore(java.time.LocalDate.now()))
            throw new BadRequestException("Expired card cannot be activated");
        c.setStatus(CardStatus.ACTIVE);
        repo.save(c);
    }
    @Override
    public void delete(Long id) {
        repo.delete(getOr404(id));
    }
    @Transactional
    @Override
    public TransferDto transfer(String ownerUsername, TransferRequest req) {
        if (req.fromCardId().equals(req.toCardId()))
            throw new BadRequestException("from == to");

        var from = getOr404(req.fromCardId());
        var to = getOr404(req.toCardId());

        if (!from.getOwner().equalsIgnoreCase(ownerUsername) || !to.getOwner().equalsIgnoreCase(ownerUsername))
            throw new BadRequestException("Cards must belong to the authenticated user");

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE)
            throw new BadRequestException("Both cards must be ACTIVE");

        var amount = req.amount().setScale(2, java.math.RoundingMode.HALF_UP);
        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Amount must be positive");

        if (from.getBalance().compareTo(amount) < 0)
            throw new BadRequestException("Insufficient funds");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        repo.save(from);
        repo.save(to);

        var t = new CardTransfer();
        t.setOwner(ownerUsername);
        t.setFromCardId(from.getId());
        t.setToCardId(to.getId());
        t.setAmount(amount);
        var saved = transferRepo.save(t);

        return new TransferDto(
                saved.getId(), saved.getOwner(),
                saved.getFromCardId(), saved.getToCardId(),
                saved.getAmount(), saved.getCreatedAt()
        );
    }

    private Card getOr404(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Card not found: " + id));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BadRequestException("Unauthenticated");
        }
        var principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof String s) {
            return s;
        }
        throw new BadRequestException("Unsupported principal type: " + principal.getClass());
    }

    @Override
    public Page<CardDto> listAll(String[] statuses, Pageable pageable) {
        var st = statuses == null || statuses.length == 0
                ? new CardStatus[]{CardStatus.ACTIVE, CardStatus.BLOCKED, CardStatus.EXPIRED}
                : java.util.Arrays.stream(statuses)
                .map(String::toUpperCase)
                .map(CardStatus::valueOf)
                .toArray(CardStatus[]::new);

        return repo.findByStatusIn(st, pageable)
                .map(c -> CardMapper.toDto(c, crypto.convertToEntityAttribute(c.getPanEncrypted())));
    }
}
