package com.teya.ledger.controller;

import com.teya.ledger.model.AccountBalance;
import com.teya.ledger.model.Transaction;
import com.teya.ledger.model.TransactionRequest;
import com.teya.ledger.model.TransactionType;
import com.teya.ledger.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerControllerTest {

    public static final String ACC_123 = "acc1234567";
    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private LedgerController ledgerController;

    private TransactionRequest validRequest;
    private Transaction sampleTransaction;
    private AccountBalance sampleBalance;
    private List<Transaction> sampleTransactions;

    @BeforeEach
    void setUp() {
        validRequest = new TransactionRequest(BigDecimal.valueOf(100.50), "Test transaction");
        sampleTransaction = new Transaction(ACC_123, BigDecimal.valueOf(100.50), TransactionType.DEPOSIT, "Test transaction");
        sampleBalance = new AccountBalance(ACC_123, BigDecimal.valueOf(500.00));
        sampleTransactions = Arrays.asList(
                new Transaction(ACC_123, BigDecimal.valueOf(100.00), TransactionType.DEPOSIT, "Initial deposit"),
                new Transaction(ACC_123, BigDecimal.valueOf(50.00), TransactionType.WITHDRAWAL, "ATM withdrawal")
        );
    }

    @Test
    @DisplayName("POST /deposit - Should return 201 CREATED with transaction when deposit is successful")
    void createDeposit_ValidRequest_ReturnsCreatedWithTransaction() {
        when(ledgerService.deposit(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(sampleTransaction);

        ResponseEntity<Transaction> response = ledgerController.createDeposit(ACC_123, validRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleTransaction, response.getBody());
        verify(ledgerService).deposit(ACC_123, validRequest.amount(), validRequest.description());
    }

    @Test
    @DisplayName("POST /deposit - Should throw IllegalArgumentException when account is invalid")
    void createDeposit_InvalidAccount_ThrowsIllegalArgumentException() {
        when(ledgerService.deposit(anyString(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid account"));

        assertThrows(IllegalArgumentException.class, () -> {
            ledgerController.createDeposit(ACC_123, validRequest);
        });
    }

    @Test
    @DisplayName("POST /withdraw - Should return 201 CREATED with transaction when withdrawal is successful")
    void createWithdrawal_ValidRequest_ReturnsCreatedWithTransaction() {
        when(ledgerService.withdraw(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(sampleTransaction);

        ResponseEntity<Transaction> response = ledgerController.createWithdrawal(ACC_123, validRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleTransaction, response.getBody());
        verify(ledgerService).withdraw(ACC_123, validRequest.amount(), validRequest.description());
    }

    @Test
    @DisplayName("POST /withdraw - Should throw IllegalStateException when insufficient funds")
    void createWithdrawal_InsufficientFunds_ThrowsIllegalStateException() {
        when(ledgerService.withdraw(anyString(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalStateException("Insufficient funds"));

        assertThrows(IllegalStateException.class, () -> {
            ledgerController.createWithdrawal(ACC_123, validRequest);
        });
    }

    @Test
    @DisplayName("GET /balance/{accountId} - Should return 200 OK with account balance for valid account")
    void getAccountBalance_ValidAccount_ReturnsOkWithBalance() {
        when(ledgerService.getBalance(anyString())).thenReturn(sampleBalance);

        ResponseEntity<AccountBalance> response = ledgerController.getAccountBalance(ACC_123);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleBalance, response.getBody());
        verify(ledgerService).getBalance(ACC_123);
    }

    @Test
    @DisplayName("GET /transactions/{accountId} - Should return 200 OK with transactions for valid account")
    void getAccountTransactions_ValidAccount_ReturnsOkWithTransactions() {
        when(ledgerService.getTransactionHistory(anyString())).thenReturn(sampleTransactions);

        ResponseEntity<List<Transaction>> response = ledgerController.getAccountTransactions(ACC_123);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(sampleTransactions, response.getBody());
        verify(ledgerService).getTransactionHistory(ACC_123);
    }

    @Test
    @DisplayName("GET /transactions - Should return 200 OK with all transactions")
    void getAllTransactions_ReturnsOkWithAllTransactions() {
        when(ledgerService.getAllTransactions()).thenReturn(sampleTransactions);

        ResponseEntity<List<Transaction>> response = ledgerController.getAllTransactions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(sampleTransactions, response.getBody());
        verify(ledgerService).getAllTransactions();
    }
}