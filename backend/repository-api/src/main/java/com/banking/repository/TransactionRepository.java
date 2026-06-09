package com.banking.repository;

import com.banking.entity.HTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<HTransaction, Long> {
    @Query("SELECT t FROM HTransaction t WHERE t.userId = :userId ORDER BY t.createdDate DESC")
    List<HTransaction> getAllTransactionByUserId(@Param("userId") Long userId);
}
