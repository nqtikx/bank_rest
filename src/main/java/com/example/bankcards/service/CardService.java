package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardDto create(CreateCardRequest req);
    Page<CardDto> listByOwner(String owner, String[] statuses, Pageable pageable);
}
