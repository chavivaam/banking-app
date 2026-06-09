package com.banking.dto;

import com.banking.enums.Role;

public record UserDTO(Long userId, String username, Role role) {}
