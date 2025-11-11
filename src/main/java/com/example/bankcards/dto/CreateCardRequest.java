package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Future;
import java.time.LocalDate;

public record CreateCardRequest(
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "PAN must be 16 digits")
        String pan,
        @Future(message = "expiry must be in the future")
        LocalDate expiry
) {}
