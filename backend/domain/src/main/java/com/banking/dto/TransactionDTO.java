package com.banking.dto;

import com.banking.enums.TransactionType;

import java.time.LocalDateTime;

// createdDate added to expose the date column the frontend transaction table displays
public record TransactionDTO(
        Long id,
        Long userId,
        String username,
        TransactionType type,
        Double amount,
        String description,
        LocalDateTime createdDate
) {}
