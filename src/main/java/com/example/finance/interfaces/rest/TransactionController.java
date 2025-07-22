package com.example.finance.interfaces.rest;

import com.example.finance.application.service.SagaOrchestrationService;
import com.example.finance.application.service.TransactionService;
import com.example.finance.domain.model.Transaction;
import com.example.finance.domain.repository.TransactionRepository;
import com.example.finance.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final SagaOrchestrationService sagaOrchestrationService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam Transaction.TransactionType type) {

        Transaction transaction = transactionService.createTransaction(
                userId, categoryId, amount, description, type);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * 코레오그래피 방식의 사가 패턴을 사용하여 트랜잭션을 생성합니다.
     * 각 단계가 다음 단계를 직접 호출하는 방식입니다.
     */
    @PostMapping("/saga/choreography")
    public CompletableFuture<ResponseEntity<Transaction>> createTransactionWithChoreographySaga(
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam Transaction.TransactionType type) {

        return transactionService.createTransactionWithSaga(userId, categoryId, amount, description, type)
                .thenApply(transaction -> ResponseEntity.status(HttpStatus.CREATED).body(transaction))
                .exceptionally(ex -> {
                    log.error("Error creating transaction with choreography saga", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * 오케스트레이션 방식의 사가 패턴을 사용하여 트랜잭션을 생성합니다.
     * 중앙 조정자가 각 단계를 조정하는 방식입니다.
     */
    @PostMapping("/saga/orchestration")
    public CompletableFuture<ResponseEntity<Transaction>> createTransactionWithOrchestrationSaga(
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam Transaction.TransactionType type) {

        return sagaOrchestrationService.executeTransactionSaga(userId, categoryId, amount, description, type)
                .thenApply(transaction -> ResponseEntity.status(HttpStatus.CREATED).body(transaction))
                .exceptionally(ex -> {
                    log.error("Error creating transaction with orchestration saga", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * 기존 사가 엔드포인트를 유지하여 하위 호환성을 보장합니다.
     * 코레오그래피 방식의 사가 패턴을 사용합니다.
     */
    @PostMapping("/saga")
    public CompletableFuture<ResponseEntity<Transaction>> createTransactionWithSaga(
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam Transaction.TransactionType type) {

        return createTransactionWithChoreographySaga(userId, categoryId, amount, description, type);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(transactionRepository.findByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
