package com.banking.dto;

import com.banking.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @NotNull TransactionType type,
        @NotNull @Positive Double amount,
        @NotBlank String description
) {}
