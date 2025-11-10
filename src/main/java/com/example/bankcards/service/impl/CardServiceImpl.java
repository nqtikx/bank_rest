package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CryptoConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CardServiceImpl implements com.example.bankcards.service.CardService {
    private final CardRepository repo;
    private final CryptoConverter crypto = new CryptoConverter();

    public CardServiceImpl(CardRepository repo) {
        this.repo = repo;
    }

    @Override
    public CardDto create(CreateCardRequest req) {
        var card = new Card();
        card.setPanEncrypted(crypto.convertToDatabaseColumn(req.pan()));
        card.setOwner(req.owner());
        card.setExpiry(req.expiry());
        card.setStatus(CardStatus.ACTIVE);
        var saved = repo.save(card);
        // для маски — расшифровываем локально, PAN наружу не возвращаем
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
}
