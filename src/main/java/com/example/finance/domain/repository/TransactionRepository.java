package com.example.finance.domain.repository;

import com.example.finance.domain.model.Transaction;
import com.example.finance.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<User> findByUser(User user);
}
