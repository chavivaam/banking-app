package com.banking.controller;

import com.banking.dto.CreateUserRequest;
import com.banking.dto.UserDTO;
import com.banking.entity.HUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

public interface UserController {

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    UserDTO create(@Valid @RequestBody CreateUserRequest request);

    @GetMapping("/getAllUsers")
    List<UserDTO> getAllUsers();

    @GetMapping("/me")
    UserDTO me(@AuthenticationPrincipal HUser principal);
}
