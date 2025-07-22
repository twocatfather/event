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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final OutboxService outboxService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 새로운 거래를 생성하고 , 이벤트를 발행한다.
     * 사용자와 카테고리를 검증하고, 거래를 저장한 뒤에 아웃박스 패턴을 통해서 이벤트를 발행하는 메소드
     */
    @Transactional
    public Transaction createTransaction(Long userId, Long categoryId, BigDecimal amount,
                                         String description, Transaction.TransactionType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with Id: " + userId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with Id: " + categoryId));

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
    }

    private User validateUser(Long userId) {
        try {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with Id: " + userId));
        } catch (Exception e) {
            log.error("User validation failed", e);
            throw new RuntimeException("User validation failed", e);
        }
    }

    private Category validateCategory(Long categoryId) {
        try {
            return categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with Id: " + categoryId));
        } catch (Exception e) {
            log.error("Category validation failed", e);
            throw new RuntimeException("Category validation failed", e);
        }
    }

    private void processTransaction(Transaction transaction) {
        try {
            log.info("Processing transaction: {}", transaction.getId());
        }catch (Exception e) {
            log.error("Transaction processing failed", e);
            compensateTransactionCreation(transaction);
            throw new RuntimeException("TransactionProcessing failed", e);
        }
    }

    public void compensateTransactionCreation(Transaction transaction) {
        try {
            transactionRepository.delete(transaction);
        } catch (Exception e) {
            log.error("Compensation failed for transaction: {}", transaction.getId(), e);
        }
    }

    /**
     *  코레오 그래피 방식 -> 각 단계가 다음 단계를 직접 호출하는 형식이며, 실패 시 보상 트랜잭션을 실행해야한다.
     *  CompletableFuture 비동기적으로 처리할 것입니다.
     */
    public CompletableFuture<Transaction> createTransactionWithSaga(Long userId, Long categoryId, BigDecimal amount,
                                                                    String description, Transaction.TransactionType type) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting transaction saga with choreography pattern");

            User user = validateUser(userId);

            Category category = validateCategory(categoryId);

            Transaction transaction = null;

            try {
                transaction = createTransaction(userId, categoryId, amount, description, type);

                processTransaction(transaction);

                return transaction;
            } catch (Exception e) {
                if (transaction != null) {
                    compensateTransactionCreation(transaction);
                }
                throw new RuntimeException("Failed to complete transaction saga", e);
            }
        });
    }
}
