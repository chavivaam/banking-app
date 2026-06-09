package com.banking.dto;

import com.banking.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String userName,
        @NotBlank @Size(min = 6) String password,
        @NotNull Role role
) {}
