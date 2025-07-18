package com.example.finance.domain.event;

import com.example.finance.domain.model.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TransactionUpdatedEvent extends DomainEvent{
    private Transaction transaction;
    private Long userId;
    private Long categoryId;
    private Transaction.TransactionType oldType;
    private Transaction.TransactionType newType;

    public TransactionUpdatedEvent(Transaction transaction, Transaction.TransactionType oldType) {
        initialize();
        this.transaction = transaction;
        this.userId = transaction.getUser().getId();
        this.categoryId = transaction.getCategory().getId();
        this.oldType = oldType;
        this.newType = transaction.getType();
    }
}
