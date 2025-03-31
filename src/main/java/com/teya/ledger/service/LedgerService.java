package com.teya.ledger.service;

import com.teya.ledger.model.AccountBalance;
import com.teya.ledger.model.Transaction;
import com.teya.ledger.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LedgerService {
    private static final Logger logger = LoggerFactory.getLogger(LedgerService.class);

    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, BigDecimal> accountBalances = new ConcurrentHashMap<>();

    /**
     * Deposits amount to specified account
     * @throws IllegalArgumentException if amount is not positive
     * @return created transaction record
     */
    public synchronized Transaction deposit(String accountId, BigDecimal amount, String description) {
        validateAmount(amount, "Deposit");
        Transaction transaction = createTransaction(accountId, amount, TransactionType.DEPOSIT, description);
        updateAccountBalance(accountId, amount, Operation.ADD);
        logTransactionSuccess("Deposit", accountId, amount, transaction);
        return transaction;
    }

    /**
     * Withdraws amount from specified account
     * @throws IllegalArgumentException if amount is not positive
     * @throws IllegalStateException if account has insufficient funds
     * @return created transaction record
     */
    public synchronized Transaction withdraw(String accountId, BigDecimal amount, String description) {
        validateAmount(amount, "Withdrawal");
        BigDecimal currentBalance = getCurrentBalance(accountId);
        validateSufficientFunds(accountId, amount, currentBalance);
        Transaction transaction = createTransaction(accountId, amount, TransactionType.WITHDRAWAL, description);
        updateAccountBalance(accountId, amount, Operation.SUBTRACT);
        logTransactionSuccess("Withdrawal", accountId, amount, transaction);
        return transaction;
    }

    /**
     * Gets current balance for specified account
     * @return AccountBalance object containing account ID and balance
     */
    public synchronized AccountBalance getBalance(String accountId) {
        logger.debug("Retrieving balance for account {}", accountId);

        BigDecimal balance = accountBalances.getOrDefault(accountId, BigDecimal.ZERO);

        logger.debug("Balance retrieved - Account: {}, Balance: {}", accountId, balance);
        return new AccountBalance(accountId, balance);
    }

    /**
     * Gets transaction history for specified account
     * @return List of transactions for the account (empty if none)
     */
    public synchronized List<Transaction> getTransactionHistory(String accountId) {
        logger.debug("Retrieving transaction history for account {}", accountId);

        List<Transaction> history = transactions.stream()
                .filter(t -> t.accountId().equals(accountId))
                .collect(Collectors.toList());

        logger.debug("Retrieved {} transactions for account {}", history.size(), accountId);
        return history;
    }

    /**
     * Gets all transactions across all accounts
     * @return List of all transactions in the system
     */
    public synchronized List<Transaction> getAllTransactions() {
        logger.debug("Retrieving all transactions");

        List<Transaction> allTransactions = new ArrayList<>(transactions);

        logger.debug("Retrieved {} total transactions", allTransactions.size());
        return allTransactions;
    }

    private enum Operation {
        ADD, SUBTRACT
    }

    private void validateAmount(BigDecimal amount, String operationType) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid {} amount {}", operationType, amount);
            throw new IllegalArgumentException(operationType + " amount must be positive");
        }
    }

    private BigDecimal getCurrentBalance(String accountId) {
        return accountBalances.getOrDefault(accountId, BigDecimal.ZERO);
    }

    private void validateSufficientFunds(String accountId, BigDecimal amount, BigDecimal currentBalance) {
        if (currentBalance.compareTo(amount) < 0) {
            logger.warn("Insufficient funds - Account: {}, Requested: {}, Available: {}",
                    accountId, amount, currentBalance);
            throw new IllegalStateException("Insufficient funds");
        }
    }

    private Transaction createTransaction(String accountId, BigDecimal amount,
                                          TransactionType type, String description) {
        Transaction transaction = new Transaction(accountId, amount, type, description);
        transactions.add(transaction);
        return transaction;
    }

    private void updateAccountBalance(String accountId, BigDecimal amount, Operation operation) {
        BigDecimal currentBalance = getCurrentBalance(accountId);
        BigDecimal newBalance = operation == Operation.ADD
                ? currentBalance.add(amount)
                : currentBalance.subtract(amount);
        accountBalances.put(accountId, newBalance);
    }

    private void logTransactionSuccess(String operationType, String accountId,
                                       BigDecimal amount, Transaction transaction) {
        BigDecimal newBalance = getCurrentBalance(accountId);
        logger.info("{} successful - Account: {}, Amount: {}, New Balance: {}, Transaction ID: {}",
                operationType, accountId, amount, newBalance, transaction.id());
    }
}