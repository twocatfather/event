package com.example.finance.application.service;

import com.example.finance.domain.event.TransactionCreatedEvent;
import com.example.finance.domain.model.Category;
import com.example.finance.domain.model.Transaction;
import com.example.finance.domain.model.User;
import com.example.finance.domain.repository.CategoryRepository;
import com.example.finance.domain.repository.TransactionRepository;
import com.example.finance.domain.repository.UserRepository;
import com.example.finance.infrastructure.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrationService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final OutboxService outboxService;
    private final TransactionService transactionService;

    /**
     *  중앙 조정자 -> 각 단계를 순차적으로 실행시키고, 실패 시 보상 트랜잭션을 실행 시킨다.
     *
     */
    public CompletableFuture<Transaction> executeTransactionSaga(
            Long userId, Long categoryId, BigDecimal amount, String description, Transaction.TransactionType type
    ) {
        return validateUserAsync(userId)
                .thenCompose(user -> validateCategoryAsync(categoryId)
                        .thenCompose(category -> createTransactionAsync(user, category, amount, description, type)
                                .thenCompose(this::processTransactionAsync)
                        )
                ).
                exceptionally(ex -> {
                    log.error("Transaction saga failed", ex);
                    throw new RuntimeException("Transaction saga failed", ex);
                });
    }

    @Async
    public CompletableFuture<Transaction> processTransactionAsync(Transaction transaction) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("processing ing...");
                return transaction;
            } catch (Exception e) {

                try {
                    transactionService.compensateTransactionCreation(transaction);
                }catch (Exception compensationEx) {
                    log.error("Compensation failed for transaction: {}", transaction.getId(), compensationEx);
                }

                throw new RuntimeException("Transaction processing failed", e);
            }
        });
    }

    @Async
    public CompletableFuture<User> validateUserAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
           try {
               return userRepository.findById(userId)
                       .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
           } catch (Exception e) {
               throw new RuntimeException("User validation failed", e);
           }
        });
    }

    // 카테고리 validateCategoryAsync public
    @Async
    public CompletableFuture<Category> validateCategoryAsync(Long categoryId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
            } catch (Exception e) {
                throw new RuntimeException("Category validation failed", e);
            }
        });
    }

    // 트랜잭션을 생성하는것 createTransactionAsync public
    @Async
    @Transactional
    public CompletableFuture<Transaction> createTransactionAsync(
            User user, Category category, BigDecimal amount, String description, Transaction.TransactionType type
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Transaction transaction = Transaction.builder()
                        .user(user)
                        .category(category)
                        .amount(amount)
                        .description(description)
                        .type(type)
                        .transactionDate(LocalDateTime.now())
                        .build();

                transaction = transactionRepository.save(transaction);

                TransactionCreatedEvent event = new TransactionCreatedEvent(transaction);
                outboxService.storedEvent(event);

                return transaction;
            } catch (Exception e) {
                throw new RuntimeException("Transaction creation failed", e);
            }
        });
    }
}
