package com.example.bankcards.dto;

import java.math.BigDecimal;

public record TransferDto(
        Long id,
        String owner,
        Long fromCardId,
        Long toCardId,
        BigDecimal amount,
        java.time.LocalDateTime createdAt
) {}
