package com.example.finance.application.listener;

import com.example.finance.domain.event.TransactionCreatedEvent;
import com.example.finance.domain.event.TransactionDeletedEvent;
import com.example.finance.domain.event.TransactionUpdatedEvent;
import com.example.finance.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        Transaction transaction = event.getTransaction();
        log.info("Transaction created: ID={}, Amount={}, Type={}, Category={}",
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory().getName());


    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionUpdated(TransactionUpdatedEvent event) {
        Transaction transaction = event.getTransaction();
        log.info("Transaction updated: ID={}, Amount={}, Type={}, Category={}",
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory().getName());


    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        log.info("Transaction deleted: ID={}, Type={}, Category ID={}",
                event.getTransactionId(),
                event.getType(),
                event.getCategoryId());


    }

    private void analyzeConsumptionPattern(Transaction transaction) {
        log.info("Analyzing consumption pattern for transaction: {}", transaction.getId());

        if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
            log.info("Expense in category: {}, amount: {}",
                    transaction.getCategory().getName(),
                    transaction.getAmount());
        }
    }
}
