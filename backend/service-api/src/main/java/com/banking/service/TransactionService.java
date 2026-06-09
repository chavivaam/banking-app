package com.banking.service;

import com.banking.dto.TransactionDTO;
import com.banking.dto.TransactionRequest;

import java.util.List;

public interface TransactionService {
    TransactionDTO processTransaction(TransactionRequest request);
    List<TransactionDTO> getAllTransactionsForUser(Long userId);
    List<TransactionDTO> getAllUsersTransactions();
}
