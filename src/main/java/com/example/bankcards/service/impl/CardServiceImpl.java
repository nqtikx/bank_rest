package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardTransferRepository;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CryptoConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.CardTransfer;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class CardServiceImpl implements com.example.bankcards.service.CardService {
    private final CardRepository repo;
    private final CardTransferRepository transferRepo;
    private final CryptoConverter crypto = new CryptoConverter();

    public CardServiceImpl(CardRepository repo, CardTransferRepository transferRepo) {
        this.repo = repo;
        this.transferRepo = transferRepo;
    }

    @Override
    public CardDto create(CreateCardRequest req) {
        var card = new Card();
        card.setPanEncrypted(crypto.convertToDatabaseColumn(req.pan()));
        card.setOwner(req.owner());
        card.setExpiry(req.expiry());
        card.setStatus(CardStatus.ACTIVE);
        var saved = repo.save(card);
        var pan = crypto.convertToEntityAttribute(saved.getPanEncrypted());
        return CardMapper.toDto(saved, pan);
    }

    @Override
    public Page<CardDto> listByOwner(String owner, String[] statuses, Pageable pageable) {
        var st = statuses == null || statuses.length == 0
                ? new CardStatus[]{CardStatus.ACTIVE, CardStatus.BLOCKED, CardStatus.EXPIRED}
                : java.util.Arrays.stream(statuses).map(String::toUpperCase).map(CardStatus::valueOf).toArray(CardStatus[]::new);

        return repo.findByOwnerIgnoreCaseAndStatusIn(owner, st, pageable)
                .map(c -> CardMapper.toDto(c, crypto.convertToEntityAttribute(c.getPanEncrypted())));
    }
    private Card getOr404(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Card not found: " + id));
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
        var c = getOr404(id);
        repo.delete(c);
    }

    @Transactional
    @Override
    public TransferDto transfer(TransferRequest req) {
        if (req.fromCardId().equals(req.toCardId()))
            throw new BadRequestException("from == to");

        var from = getOr404(req.fromCardId());
        var to = getOr404(req.toCardId());

        // оба принадлежат одному владельцу (пока без Security, owner приходит в запросе)
        if (!from.getOwner().equalsIgnoreCase(req.owner()) || !to.getOwner().equalsIgnoreCase(req.owner()))
            throw new BadRequestException("Cards must belong to the same owner");

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE)
            throw new BadRequestException("Both cards must be ACTIVE");

        var amount = req.amount().setScale(2, java.math.RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Amount must be positive");

        if (from.getBalance().compareTo(amount) < 0)
            throw new BadRequestException("Insufficient funds");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        repo.save(from);
        repo.save(to);

        var t = new CardTransfer();
        t.setOwner(req.owner());
        t.setFromCardId(from.getId());
        t.setToCardId(to.getId());
        t.setAmount(amount);
        var saved = transferRepo.save(t);

        return new TransferDto(
                saved.getId(), saved.getOwner(), saved.getFromCardId(),
                saved.getToCardId(), saved.getAmount(), saved.getCreatedAt()
        );
    }
}

