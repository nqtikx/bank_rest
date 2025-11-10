package com.example.bankcards.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateCardRequest(
        @NotBlank String pan,
        @NotBlank @Size(max = 100) String owner,
        @NotNull LocalDate expiry
) {}
