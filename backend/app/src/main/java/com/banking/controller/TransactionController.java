package com.banking.controller;

import com.banking.dto.TransactionDTO;
import com.banking.dto.TransactionRequest;
import com.banking.entity.HUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

public interface TransactionController {

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    TransactionDTO create(@Valid @RequestBody TransactionRequest request);

    @GetMapping("/getAllTransactions")
    List<TransactionDTO> getAllTransactions(@AuthenticationPrincipal HUser principal);

    @GetMapping("/getAllUsersTransactions")
    List<TransactionDTO> getAllUsersTransactions(@AuthenticationPrincipal HUser principal);
}
