package com.example.finance.domain.event;

import com.example.finance.domain.model.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TransactionDeletedEvent extends DomainEvent {
    private Long transactionId;
    private Long userId;
    private Long categoryId;
    private Transaction.TransactionType type;


    public TransactionDeletedEvent(Transaction transaction) {
        initialize();
        this.transactionId = transaction.getId();
        this.userId = transaction.getUser().getId();
        this.categoryId = transaction.getCategory().getId();
        this.type = transaction.getType();
    }
}
