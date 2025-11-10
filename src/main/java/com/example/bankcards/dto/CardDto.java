package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CardDto(
        Long id,
        String maskedNumber,
        String owner,
        LocalDate expiry,
        CardStatus status,
        BigDecimal balance
) {}
