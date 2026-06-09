package com.banking.service.impl;

import com.banking.dto.TransactionDTO;
import com.banking.dto.TransactionRequest;
import com.banking.entity.HTransaction;
import com.banking.entity.HUser;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import com.banking.service.TransactionService;
import com.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public TransactionDTO processTransaction(TransactionRequest request) {
        HUser currentUser = userService.getCurrentUser();
        HTransaction transaction = HTransaction.builder()
                .userId(currentUser.getId())
                .amount(request.amount())
                .type(request.type())
                .description(request.description())
                .createdDate(LocalDateTime.now())
                .build();
        return toDTO(transactionRepository.save(transaction), currentUser.getUsername());
    }

    @Override
    public List<TransactionDTO> getAllTransactionsForUser(Long userId) {
        String username = userRepository.findById(userId)
                .map(HUser::getUsername)
                .orElse("Unknown");
        return transactionRepository.getAllTransactionByUserId(userId).stream()
                .map(t -> toDTO(t, username))
                .toList();
    }

    @Override
    public List<TransactionDTO> getAllUsersTransactions() {
        Map<Long, String> usernameMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(HUser::getId, HUser::getUsername));
        return transactionRepository.findAll().stream()
                .map(t -> toDTO(t, usernameMap.getOrDefault(t.getUserId(), "Unknown")))
                .toList();
    }

    private TransactionDTO toDTO(HTransaction t, String username) {
        return new TransactionDTO(
                t.getId(), t.getUserId(), username,
                t.getType(), t.getAmount(), t.getDescription(), t.getCreatedDate()
        );
    }
}
