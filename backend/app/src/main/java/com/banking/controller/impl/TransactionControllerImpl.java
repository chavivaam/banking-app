package com.banking.controller.impl;

import com.banking.controller.TransactionController;
import com.banking.dto.TransactionDTO;
import com.banking.dto.TransactionRequest;
import com.banking.entity.HUser;
import com.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {

    private final TransactionService transactionService;

    @Override
    public TransactionDTO create(TransactionRequest request) {
        return transactionService.processTransaction(request);
    }

    @Override
    public List<TransactionDTO> getAllTransactions(@AuthenticationPrincipal HUser principal) {
        return transactionService.getAllTransactionsForUser(principal.getId());
    }

    @Override
    public List<TransactionDTO> getAllUsersTransactions(@AuthenticationPrincipal HUser principal) {
        return transactionService.getAllUsersTransactions();
    }
}
