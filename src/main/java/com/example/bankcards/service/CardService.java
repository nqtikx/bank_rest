package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardDto create(CreateCardRequest req);
    CardDto createForOwner(String ownerUsername, CreateCardRequest req);
    Page<CardDto> listByOwner(String owner, String[] statuses, Pageable pageable);
    void block(Long id);
    void activate(Long id);
    void delete(Long id);
    TransferDto transfer(String ownerUsername, TransferRequest req);
}
