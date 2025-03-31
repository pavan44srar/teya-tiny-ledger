package com.teya.ledger.model;

import java.math.BigDecimal;

public record AccountBalance(String accountId, BigDecimal balance) {
}
