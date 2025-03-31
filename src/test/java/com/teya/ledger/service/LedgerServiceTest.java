package com.teya.ledger.service;

import com.teya.ledger.model.AccountBalance;
import com.teya.ledger.model.Transaction;
import com.teya.ledger.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService();
    }

    @Test
    @DisplayName("Deposit - Should successfully deposit positive amount")
    void deposit_positiveAmount_shouldSucceed() {
        Transaction transaction = ledgerService.deposit("acc1", BigDecimal.valueOf(100), "Initial deposit");

        assertNotNull(transaction);
        assertEquals("acc1", transaction.accountId());
        assertEquals(BigDecimal.valueOf(100), transaction.amount());
        assertEquals(TransactionType.DEPOSIT, transaction.type());

        AccountBalance balance = ledgerService.getBalance("acc1");
        assertEquals(BigDecimal.valueOf(100), balance.balance());
    }

    @Test
    @DisplayName("Deposit - Should throw exception for zero amount")
    void deposit_zeroAmount_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ledgerService.deposit("acc1", BigDecimal.ZERO, "Invalid deposit"));

        assertEquals("Deposit amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Deposit - Should throw exception for negative amount")
    void deposit_negativeAmount_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ledgerService.deposit("acc1", BigDecimal.valueOf(-50), "Invalid deposit"));

        assertEquals("Deposit amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Withdraw - Should successfully withdraw when sufficient funds exist")
    void withdraw_withSufficientFunds_shouldSucceed() {
        ledgerService.deposit("acc1", BigDecimal.valueOf(200), "Initial deposit");
        Transaction withdrawal = ledgerService.withdraw("acc1", BigDecimal.valueOf(100), "Withdrawal");

        assertNotNull(withdrawal);
        assertEquals("acc1", withdrawal.accountId());
        assertEquals(BigDecimal.valueOf(100), withdrawal.amount());
        assertEquals(TransactionType.WITHDRAWAL, withdrawal.type());

        AccountBalance balance = ledgerService.getBalance("acc1");
        assertEquals(BigDecimal.valueOf(100), balance.balance());
    }

    @Test
    @DisplayName("Withdraw - Should throw exception when insufficient funds")
    void withdraw_insufficientFunds_shouldThrowException() {
        ledgerService.deposit("acc1", BigDecimal.valueOf(50), "Initial deposit");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ledgerService.withdraw("acc1", BigDecimal.valueOf(100), "Overdraw attempt"));

        assertEquals("Insufficient funds", exception.getMessage());
    }

    @Test
    @DisplayName("Withdraw - Should throw exception for zero amount")
    void withdraw_zeroAmount_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ledgerService.withdraw("acc1", BigDecimal.ZERO, "Invalid withdrawal"));

        assertEquals("Withdrawal amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Withdraw - Should throw exception for negative amount")
    void withdraw_negativeAmount_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ledgerService.withdraw("acc1", BigDecimal.valueOf(-10), "Invalid withdrawal"));

        assertEquals("Withdrawal amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("GetBalance - Should return zero for new account")
    void getBalance_newAccount_shouldReturnZero() {
        AccountBalance balance = ledgerService.getBalance("newAcc");

        assertEquals("newAcc", balance.accountId());
        assertEquals(BigDecimal.ZERO, balance.balance());
    }

    @Test
    @DisplayName("GetBalance - Should return correct balance after transactions")
    void getBalance_afterTransactions_shouldReturnCorrectAmount() {
        ledgerService.deposit("acc1", BigDecimal.valueOf(300), "Deposit 1");
        ledgerService.withdraw("acc1", BigDecimal.valueOf(100), "Withdrawal 1");
        ledgerService.deposit("acc1", BigDecimal.valueOf(50), "Deposit 2");

        AccountBalance balance = ledgerService.getBalance("acc1");
        assertEquals(BigDecimal.valueOf(250), balance.balance());
    }

    @Test
    @DisplayName("GetTransactionHistory - Should return empty list for new account")
    void getTransactionHistory_newAccount_shouldReturnEmptyList() {
        List<Transaction> history = ledgerService.getTransactionHistory("newAcc");

        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("GetTransactionHistory - Should return only transactions for specified account")
    void getTransactionHistory_shouldFilterByAccount() {
        ledgerService.deposit("acc1", BigDecimal.valueOf(100), "Deposit to acc1");
        ledgerService.deposit("acc2", BigDecimal.valueOf(200), "Deposit to acc2");
        ledgerService.withdraw("acc1", BigDecimal.valueOf(50), "Withdrawal from acc1");

        List<Transaction> acc1History = ledgerService.getTransactionHistory("acc1");
        assertEquals(2, acc1History.size());
        assertTrue(acc1History.stream().allMatch(t -> t.accountId().equals("acc1")));
    }

    @Test
    @DisplayName("GetAllTransactions - Should return all transactions across accounts")
    void getAllTransactions_shouldReturnAllTransactions() {
        ledgerService.deposit("acc1", BigDecimal.valueOf(100), "Deposit 1");
        ledgerService.deposit("acc2", BigDecimal.valueOf(200), "Deposit 2");
        ledgerService.withdraw("acc1", BigDecimal.valueOf(50), "Withdrawal");

        List<Transaction> allTransactions = ledgerService.getAllTransactions();
        assertEquals(3, allTransactions.size());
    }
}