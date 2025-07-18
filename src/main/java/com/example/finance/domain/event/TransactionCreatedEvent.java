package com.example.finance.domain.event;

import com.example.finance.domain.model.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TransactionCreatedEvent extends DomainEvent{
    private Transaction transaction;
    private Long userId;
    private Long categoryId;

    public TransactionCreatedEvent(Transaction transaction) {
        initialize();
        this.transaction = transaction;
        this.userId = transaction.getUser().getId();
        this.categoryId = transaction.getCategory().getId();
    }
}
