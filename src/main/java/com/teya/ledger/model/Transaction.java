package com.teya.ledger.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(String id,
                          String accountId,
                          BigDecimal amount,
                          TransactionType type,
                          LocalDateTime timestamp,
                          String description
) {

    public Transaction(String accountId, BigDecimal amount, TransactionType type, String description) {
        this(UUID.randomUUID().toString(), accountId, amount, type, LocalDateTime.now(), description);
    }
}
