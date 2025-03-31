package com.teya.ledger.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @JsonProperty("amount")
        BigDecimal amount,

        @NotNull(message = "Description is required")
        @Size(min = 1, max = 100, message = "Description must be between 1 and 50 characters")
        @JsonProperty("description")
        String description) {

    @JsonCreator
    public TransactionRequest(
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("description") String description) {
        this.amount = amount;
        this.description = description;
    }
}