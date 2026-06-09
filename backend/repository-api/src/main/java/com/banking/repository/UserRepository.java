package com.banking.repository;

import com.banking.entity.HUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<HUser, Long> {
    @Query("SELECT u FROM HUser u WHERE u.username = :username")
    Optional<HUser> getUserByUsername(@Param("username") String username);
}
