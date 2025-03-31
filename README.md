
# Teya Ledger API

A simple API for managing financial transactions in a ledger system. This application provides the following features:

- Record money movements (deposits and withdrawals)
- View current balance
- View transaction history

## Technology Stack

- Java 17
- Spring Boot 3.1.0
- Gradle

## Assumptions

1. All transactions are performed in-memory without persistence
2. No authentication or authorization is implemented
3. Account IDs are passed with each request, no separate account creation endpoint
4. Concurrent access is handled with synchronized methods but not full ACID compliance
5. Amounts are represented using BigDecimal for precision in financial calculations

## Building and Running

### Prerequisites

- Java 17 or higher
- Gradle (or use the included Gradle wrapper)

### Steps to Run

1. Clone the repository
2. Navigate to the project root directory
3. Build the project:

```bash  
./gradlew build 
```

4. Run the application:

```bash 
./gradlew bootRun
``` 

The application will start on port 8080.

### [Swagger](http://localhost:8080/swagger-ui/index.html#)

## API Usage Examples

### Record a Deposit

### Linux/Mac
```bash  
curl -X POST 'http://localhost:8080/api/v1/accounts/acc1234567/deposits' -H 'accept: */*' -H 'Content-Type: application/json' -d '{"amount": 100.5, "description": "Initial deposit"}'
```
### Windows
```bash  
curl.exe -X POST "http://localhost:8080/api/v1/accounts/acc1234567/deposits" -H "accept: */*" -H "Content-Type: application/json" -d '{\"amount\": 100.5, \"description\": \"Initial deposit\"}'
```

### Record a Withdrawal

### Linux/Mac
```bash 
curl --location 'http://localhost:8080/api/v1/accounts/acc1234567/withdrawals' --header 'accept: application/json' --header 'Content-Type: application/json' --data '{"amount": 2.0, "description": "ATM withdrawal", "
``` 

### Windows
```bash  
curl.exe --location "http://localhost:8080/api/v1/accounts/acc1234567/withdrawals" --header "accept: application/json" --header "Content-Type: application/json" --data '{\"amount\": 2.0, \"description\": \"ATM withdrawal\", \"currency\": \"USD\"}'
```

### View Current Balance

### Linux/Mac
```bash 
curl -X 'GET' 'http://localhost:8080/api/v1/accounts/acc1234567/balance' -H 'accept: */*'
```
### Windows
```bash 
curl.exe -X GET "http://localhost:8080/api/v1/accounts/acc1234567/balance" -H "accept: */*" 
```

### View Transaction History for an Account

### Linux/Mac
```bash 
curl -X 'GET' 'http://localhost:8080/api/v1/accounts/acc1234567/transactions' -H 'accept: */*'
```

### Windows
```bash
curl.exe -X GET "http://localhost:8080/api/v1/accounts/acc1234567/transactions" -H "accept: */*"
```

### View All Transactions

### Linux/Mac
```bash 
curl -X 'GET' 'http://localhost:8080/api/v1/accounts/transactions' -H 'accept: */*'
```

### Windows
```bash
curl.exe -X GET "http://localhost:8080/api/v1/accounts/transactions" -H "accept: */*"
```

## API Endpoints

| Method | Endpoint | Description |  
|--------|----------|-------------|  
| POST | /api/v1/accounts/{accountId}/deposit | Record a deposit |  
| POST | /api/v1/accounts/{accountId}/withdraw | Record a withdrawal |  
| GET | /api/v1/accounts/{accountId}/balance | Get current balance |  
| GET | /api/v1/accounts/{accountId}/transactions/{accountId} | Get transaction history for an account |  
| GET | /api/v1/accounts/transactions | Get all transactions |  

## Error Handling

The API returns appropriate HTTP status codes:

- 200 OK: Operation successful
- 400 Bad Request: Invalid input (e.g., negative amount)
- 409 Conflict: Business rule violation (e.g., insufficient funds)
- 500 Internal Server Error: Unexpected errors


# LedgerService Improvements

1. **Database Integration**  
   Replace in-memory storage with a proper database (PostgreSQL/MySQL) for persistent data and ACID compliance.

2. **Enhanced Transaction Model**  
   Add transaction timestamps, status tracking, and unique reference IDs for better auditability.

3. **Optimized Concurrency**  
   Implement database-level locking or optimistic concurrency control instead of `synchronized` methods.

4. **Comprehensive Validation**  
   Add account existence checks, minimum/maximum amount limits, and currency validation.