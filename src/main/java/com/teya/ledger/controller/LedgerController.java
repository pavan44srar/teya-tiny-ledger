package com.teya.ledger.controller;

import com.teya.ledger.model.AccountBalance;
import com.teya.ledger.model.Transaction;
import com.teya.ledger.model.TransactionRequest;
import com.teya.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account Ledger API", description = "REST API for account transactions and balances")
public class LedgerController {

    private static final Logger logger = LoggerFactory.getLogger(LedgerController.class);
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    // Deposit to specific account
    @PostMapping("/{accountId}/deposits")
    @Operation(summary = "Create a deposit", description = "Add money to a specific account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid deposit amount")
    })
    public ResponseEntity<Transaction> createDeposit(
            @Parameter(description = "Account ID", required = true, example = "acc1234567")
            @PathVariable("accountId") @Size(min = 10, max = 10) String accountId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deposit details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransactionRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"amount\": 100.50, \"description\": \"Initial deposit\"}"
                            )
                    )
            )
            @Valid @RequestBody TransactionRequest request) {

        logger.info("Creating deposit for account {}", accountId);
        Transaction transaction = ledgerService.deposit(
                accountId,
                request.amount(),
                request.description()
        );
        return ResponseEntity
                .created(URI.create("/api/v1/accounts/" + accountId + "/transactions/" + transaction.id()))
                .body(transaction);
    }

    // Withdrawal from specific account
    @PostMapping("/{accountId}/withdrawals")
    @Operation(summary = "Create a withdrawal", description = "Withdraw money from a specific account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Withdrawal created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid withdrawal amount"),
            @ApiResponse(responseCode = "409", description = "Insufficient funds")
    })
    public ResponseEntity<Transaction> createWithdrawal(
            @Parameter(description = "Account ID", required = true, example = "acc1234567")
            @PathVariable("accountId") @Size(min = 10, max = 10) String accountId,
            @Valid @RequestBody TransactionRequest request) {

        logger.info("Creating withdrawal for account {}", accountId);
        Transaction transaction = ledgerService.withdraw(
                accountId,
                request.amount(),
                request.description()
        );
        return ResponseEntity
                .created(URI.create("/api/v1/accounts/" + accountId + "/transactions/" + transaction.id()))
                .body(transaction);
    }

    // Get account balance
    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Retrieve current balance for an account")
    @ApiResponse(responseCode = "200", description = "Balance retrieved successfully")
    public ResponseEntity<AccountBalance> getAccountBalance(
            @Parameter(description = "Account ID", required = true, example = "acc1234567")
            @PathVariable("accountId") @Size(min = 10, max = 10) String accountId) {

        logger.debug("Retrieving balance for account {}", accountId);
        return ResponseEntity.ok(ledgerService.getBalance(accountId));
    }

    // Get transaction history for account
    @GetMapping("/{accountId}/transactions")
    @Operation(summary = "Get account transactions", description = "Retrieve transaction history for an account")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<Transaction>> getAccountTransactions(
            @Parameter(description = "Account ID", required = true, example = "acc1234567")
            @PathVariable("accountId") @Size(min = 10, max = 10) String accountId) {

        logger.debug("Retrieving transactions for account {}", accountId);
        return ResponseEntity.ok(ledgerService.getTransactionHistory(accountId));
    }

    // Get all transactions across accounts (admin endpoint)
    @GetMapping("/transactions")
    @Operation(summary = "Get all transactions", description = "Retrieve all transactions across all accounts (admin)")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<Transaction>> getAllTransactions() {

        logger.debug("Retrieving all transactions");
        return ResponseEntity.ok(ledgerService.getAllTransactions());
    }
}