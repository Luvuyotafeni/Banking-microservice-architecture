# Banking Microservices - How The System Works

## Table of Contents
1. [System Overview](#system-overview)
2. [The Three Services](#the-three-services)
3. [How Services Communicate](#how-services-communicate)
4. [Complete User Journey](#complete-user-journey)
5. [Transaction Flow Explained](#transaction-flow-explained)
6. [What Each Service Does](#what-each-service-does)
7. [Kafka Topics Explained](#kafka-topics-explained)
8. [Expected Behavior](#expected-behavior)

---

## System Overview

This is a **banking system** built with **3 independent microservices** that work together:

```
┌─────────────────────────────────────────────────────────┐
│                      YOUR SYSTEM                         │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │    User      │  │   Payment    │  │   Banking    │ │
│  │ Management   │  │   Service    │  │   Service    │ │
│  │              │  │              │  │              │ │
│  │ Port 8081    │  │ Port 8082    │  │ Port 8083    │ │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘ │
│         │                 │                  │          │
│         └─────────────────┼──────────────────┘          │
│                           │                             │
│                    ┌──────▼──────┐                      │
│                    │    Kafka    │                      │
│                    │  (Messenger)│                      │
│                    └─────────────┘                      │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

**Think of it like this:**
- **User Management** = The front desk (handles people, logins, beneficiaries)
- **Banking Service** = The vault (manages accounts, holds money)
- **Payment Service** = The transaction processor (moves money between accounts)
- **Kafka** = The internal phone system (services talk to each other)

---

## The Three Services

### 1️⃣ User Management Service (Port 8081)

**Responsible For:**
- User registration
- Login and authentication
- Creating JWT tokens
- Managing beneficiaries (people you can send money to)
- User approval by admin

**Database:** `user_management_db`
- Stores: users, credentials, beneficiaries

**What It Expects:**
- Users to register with email and ID number
- Admin to approve new users
- Users to log in with password or OTP

**What It Provides:**
- JWT tokens for authentication
- Validates if users exist (when asked by Payment Service)
- Validates if beneficiaries belong to users

---

### 2️⃣ Banking Service (Port 8083)

**Responsible For:**
- Creating bank accounts
- Storing account balances
- Processing debits (taking money out)
- Processing credits (putting money in)
- Checking if accounts have enough money

**Database:** `banking_db`
- Stores: accounts, balances

**What It Expects:**
- Users to create accounts with initial deposit
- Requests from Payment Service to check balance
- Requests from Payment Service to debit/credit accounts

**What It Provides:**
- Auto-generated account numbers
- Real-time balance information
- Confirms when money is debited or credited

---

### 3️⃣ Payment Service (Port 8082)

**Responsible For:**
- Creating transactions
- Calculating fees
- Coordinating money transfers
- Keeping transaction history
- Validating transactions before processing

**Database:** `payment_db`
- Stores: transactions, references, statuses

**What It Expects:**
- Users to be logged in (JWT token)
- Valid account numbers
- Sufficient balance (asks Banking Service)

**What It Provides:**
- Transaction references (TXN12345...)
- Transaction status (PENDING, PROCESSING, COMPLETED, FAILED)
- Transaction history

---

## How Services Communicate

### Direct Communication (REST API)
Users talk to services using HTTP requests:

```
User → User Management: POST /api/v1/auth/login
User → Banking Service: POST /api/v1/accounts
User → Payment Service: POST /api/v1/transactions
```

### Internal Communication (Kafka)
Services talk to each other using Kafka messages:

```
Payment Service → Kafka → Banking Service: "Does account X have R500?"
Banking Service → Kafka → Payment Service: "Yes, balance is R10,000"
```

**Why Kafka?**
- **Asynchronous**: Services don't wait for each other
- **Reliable**: Messages are never lost
- **Scalable**: Can handle millions of messages

---

## Complete User Journey

### Step 1: User Registration
```
1. User fills registration form
   ↓
2. POST to User Management Service
   ↓
3. User created with status = PENDING
   ↓
4. Kafka message sent: "user-created-event"
```

### Step 2: Admin Approval
```
1. Admin logs in
   ↓
2. Reviews pending user
   ↓
3. Clicks "Approve"
   ↓
4. User status = APPROVED, active = true
   ↓
5. OTP generated and sent to user's email
   ↓
6. Kafka message sent: "user-updated-event"
```

### Step 3: First Login
```
1. User enters email + OTP
   ↓
2. User Management validates OTP
   ↓
3. JWT token created and returned
   ↓
4. User must change password
   ↓
5. Kafka message sent: "user-login-event"
```

### Step 4: Create Bank Account
```
1. User sends request with JWT token
   ↓
2. Banking Service validates JWT
   ↓
3. Account created with auto-generated number
   ↓
4. Initial deposit credited to account
   ↓
5. Kafka message sent: "account-created-event"
```

### Step 5: Add Beneficiary
```
1. User provides account number + nickname
   ↓
2. User Management checks if beneficiary exists
   ↓
3. Links beneficiary to user
   ↓
4. Kafka message sent: "beneficiary-added-event"
```

### Step 6: Make Transfer
```
1. User creates transfer request
   ↓
2. Payment Service starts processing (explained in detail below)
   ↓
3. Money moved from account A to account B
   ↓
4. Transaction marked COMPLETED
```

---

## Transaction Flow Explained

### What Happens When You Transfer R500?

**User Request:**
```json
POST /api/v1/transactions
{
  "accountId": "6217123456781234",
  "destinationAccountId": "6217987654321234",
  "type": "TRANSFER",
  "amount": 500,
  "beneficiaryId": "uuid-here"
}
```

**The Journey:**

#### Phase 1: Payment Service Receives Request
```
1. Payment Service receives request
2. Extracts user ID from JWT token
3. Validates:
   ✓ Amount is positive
   ✓ Amount ≤ R100,000 (max transfer)
   ✓ User hasn't exceeded daily limit (50 transactions)
   ✓ Transfer has beneficiary ID
4. Calculates fee: R500 × 0.5% = R2.50
5. Total amount: R502.50
6. Creates transaction in database:
   - Status: PENDING
   - Reference: TXN1771001967878C18
   - Amount: R500
   - Fee: R2.50
   - Total: R502.50
```

#### Phase 2: Validate User (via Kafka)
```
Payment Service → Kafka:
   Topic: user-validation-request
   Message: { userId: "uuid", requestId: "req123" }
   
User Management ← Kafka (receives message):
   - Checks if user exists
   - Checks if user is active
   
User Management → Kafka:
   Topic: user-validation-response
   Message: { valid: true, active: true }
   
Payment Service ← Kafka (receives response):
   ✓ User is valid, continue processing
```

#### Phase 3: Validate Beneficiary (via Kafka)
```
Payment Service → Kafka:
   Topic: beneficiary-validation-request
   Message: { beneficiaryId: "uuid", userId: "uuid" }
   
User Management ← Kafka:
   - Checks if beneficiary exists
   - Checks if beneficiary belongs to this user
   
User Management → Kafka:
   Topic: beneficiary-validation-response
   Message: { valid: true, belongsToUser: true }
   
Payment Service ← Kafka:
   ✓ Beneficiary is valid, continue processing
```

#### Phase 4: Check Balance (via Kafka)
```
Payment Service → Kafka:
   Topic: balance-check-request
   Message: { 
     accountId: "6217123456781234",
     requiredAmount: 502.50,
     transactionReference: "TXN1771001967878C18"
   }
   
Banking Service ← Kafka (receives message):
   - Finds account 6217123456781234
   - Checks balance: R10,000
   - Checks if R10,000 ≥ R502.50: YES ✓
   
Banking Service → Kafka:
   Topic: balance-check-response
   Message: {
     transactionReference: "TXN1771001967878C18",
     sufficientBalance: true,
     currentBalance: 10000,
     availableBalance: 10000
   }
   
Payment Service ← Kafka:
   ✓ Sufficient balance confirmed
   Updates transaction: Status = PROCESSING
```

#### Phase 5: Debit Source Account (via Kafka)
```
Payment Service → Kafka:
   Topic: debit-account-request
   Message: {
     accountId: "6217123456781234",
     amount: 502.50,
     transactionReference: "TXN1771001967878C18"
   }
   
Banking Service ← Kafka:
   - Locks account 6217123456781234 (prevents other transactions)
   - Current balance: R10,000
   - Deducts: R502.50
   - New balance: R9,497.50
   - Saves to database
   - Unlocks account
   
Banking Service → Kafka:
   Topic: account-operation-response
   Message: {
     transactionReference: "TXN1771001967878C18",
     success: true,
     newBalance: 9497.50,
     message: "Debit successful"
   }
   
Payment Service ← Kafka:
   ✓ Debit successful
   Updates transaction: balanceBefore = 10000
```

#### Phase 6: Credit Destination Account (via Kafka)
```
Payment Service → Kafka:
   Topic: credit-account-request
   Message: {
     accountId: "6217987654321234",
     amount: 500,
     transactionReference: "TXN1771001967878C18"
   }
   
Banking Service ← Kafka:
   - Locks account 6217987654321234
   - Current balance: R5,000
   - Adds: R500
   - New balance: R5,500
   - Saves to database
   - Unlocks account
   
Banking Service → Kafka:
   Topic: account-operation-response
   Message: {
     transactionReference: "TXN1771001967878C18",
     success: true,
     newBalance: 5500,
     message: "Credit successful"
   }
   
Payment Service ← Kafka:
   ✓ Credit successful
   Updates transaction:
   - Status = COMPLETED
   - balanceAfter = 9497.50
   - completedAt = 2024-02-13 10:15:23
```

#### Phase 7: Send Notification
```
Payment Service → Kafka:
   Topic: transaction-notification
   Message: {
     transactionId: "uuid",
     userId: "uuid",
     type: "TRANSFER",
     status: "COMPLETED",
     amount: 500,
     reference: "TXN1771001967878C18"
   }
   
All Services ← Kafka:
   (Any service can listen to this for analytics, notifications, etc.)
```

**Final Result:**
- Source Account: R10,000 → R9,497.50 (lost R502.50)
- Destination Account: R5,000 → R5,500 (gained R500)
- Fee collected: R2.50
- Transaction status: COMPLETED
- Time taken: ~500 milliseconds

---

## What Each Service Does

### User Management Service

**Publishes (Sends) These Events:**
| Event | When | Contains |
|-------|------|----------|
| user-created-event | User registers | userId, email, name |
| user-updated-event | User approved | userId, approvalStatus |
| user-login-event | User logs in | userId, email, loginTime |
| user-suspended-event | User suspended | userId, suspended, reason |
| beneficiary-added-event | Beneficiary added | beneficiaryId, accountId |
| beneficiary-removed-event | Beneficiary removed | beneficiaryId |

**Consumes (Receives) These Events:**
| Event | From Service | Action |
|-------|--------------|--------|
| user-validation-request | Payment Service | Check if user exists |
| beneficiary-validation-request | Payment Service | Check if beneficiary valid |

**Responds With:**
| Response | To Service | Contains |
|----------|-----------|----------|
| user-validation-response | Payment Service | valid: true/false |
| beneficiary-validation-response | Payment Service | valid: true/false, belongsToUser |

---

### Banking Service

**Publishes (Sends) These Events:**
| Event | When | Contains |
|-------|------|----------|
| account-created-event | Account created | accountId, accountNumber, initialBalance |
| account-updated-event | Account modified | accountId, accountNumber |
| account-closed-event | Account closed | accountId, closureReason |
| balance-check-response | Balance checked | sufficient: true/false, balance |
| account-operation-response | Debit/Credit done | success: true/false, newBalance |
| account-validation-response | Account validated | valid: true/false |

**Consumes (Receives) These Events:**
| Event | From Service | Action |
|-------|--------------|--------|
| balance-check-request | Payment Service | Check if account has enough money |
| debit-account-request | Payment Service | Take money out of account |
| credit-account-request | Payment Service | Put money into account |
| account-validation-request | Payment Service | Check if account exists |

---

### Payment Service

**Publishes (Sends) These Events:**
| Event | When | Contains |
|-------|------|----------|
| user-validation-request | Before processing | userId |
| beneficiary-validation-request | For transfers | beneficiaryId, userId |
| balance-check-request | Before debit | accountId, requiredAmount |
| debit-account-request | To withdraw money | accountId, amount |
| credit-account-request | To deposit money | accountId, amount |
| account-validation-request | To validate account | accountId, userId |
| transaction-notification | Transaction done | transactionId, status, amount |

**Consumes (Receives) These Events:**
| Event | From Service | Action |
|-------|--------------|--------|
| user-validation-response | User Management | Proceed or fail transaction |
| beneficiary-validation-response | User Management | Proceed or fail transaction |
| balance-check-response | Banking Service | Proceed or fail transaction |
| account-operation-response | Banking Service | Update transaction status |
| account-validation-response | Banking Service | Proceed or fail transaction |

---

## Kafka Topics Explained

### What is a Kafka Topic?
Think of a topic as a **mailbox** or **message board** where services leave messages for each other.

### Request → Response Pattern

**Example 1: Checking Balance**
```
Request Topic:  balance-check-request
    ↓
    Payment Service puts message: "Does account X have R500?"
    ↓
    Banking Service picks up message
    ↓
Response Topic: balance-check-response
    ↓
    Banking Service puts message: "Yes, balance is R10,000"
    ↓
    Payment Service picks up message
```

**Example 2: Validating User**
```
Request Topic:  user-validation-request
    ↓
    Payment Service: "Is user ABC valid?"
    ↓
    User Management picks up
    ↓
Response Topic: user-validation-response
    ↓
    User Management: "Yes, user is active"
    ↓
    Payment Service picks up
```

### Event Pattern (One-Way Notification)

```
Event Topic: transaction-notification
    ↓
    Payment Service: "Transaction XYZ completed!"
    ↓
    Any interested service picks up (Analytics, Notifications, etc.)
```

---

## Expected Behavior

### Successful Transfer

**Timeline:**
```
0ms    - User clicks "Transfer R500"
10ms   - Payment Service creates transaction (PENDING)
50ms   - User validated via Kafka ✓
100ms  - Beneficiary validated via Kafka ✓
150ms  - Balance checked via Kafka ✓
200ms  - Transaction updated to PROCESSING
250ms  - Debit request sent via Kafka
350ms  - Banking Service debits account ✓
400ms  - Debit response received
450ms  - Credit request sent via Kafka
550ms  - Banking Service credits account ✓
600ms  - Credit response received
650ms  - Transaction updated to COMPLETED
700ms  - Notification sent via Kafka
```

**Total Time: ~700 milliseconds**

**Database State After:**
```sql
-- Transaction
status: COMPLETED
amount: 500.00
fee: 2.50
total_amount: 502.50
balance_before: 10000.00
balance_after: 9497.50
completed_at: 2024-02-13 10:15:23

-- Source Account
balance: 9497.50 (was 10000.00)

-- Destination Account  
balance: 5500.00 (was 5000.00)
```

### Failed Transfer (Insufficient Balance)

**Timeline:**
```
0ms    - User clicks "Transfer R15,000"
10ms   - Payment Service creates transaction (PENDING)
50ms   - User validated ✓
100ms  - Beneficiary validated ✓
150ms  - Balance checked: R10,000 < R15,002.50 ✗
200ms  - Transaction updated to FAILED
250ms  - Notification sent
```

**Database State After:**
```sql
-- Transaction
status: FAILED
failed_at: 2024-02-13 10:15:23
failure_reason: "Insufficient balance: Available: 10000, Required: 15002.50"

-- Accounts
balance: UNCHANGED (no debit occurred)
```

### Stuck Transaction (Kafka Consumer Not Running)

**Timeline:**
```
0ms    - User clicks "Transfer R500"
10ms   - Payment Service creates transaction (PENDING)
50ms   - User validated ✓
100ms  - Beneficiary validated ✓
150ms  - Balance check request sent to Kafka
???    - NO RESPONSE (Banking Service not consuming)
15min  - Automatic timeout processor marks as FAILED
```

**How to Fix:**
1. Ensure Banking Service is running
2. Ensure `@EnableKafka` is in KafkaConfig.java
3. Restart Banking Service
4. Check logs for "Kafka listener containers started"

---

## Summary: Service Responsibilities

| Service | Creates | Validates | Stores | Responds To |
|---------|---------|-----------|--------|-------------|
| **User Management** | Users, Beneficiaries, JWT tokens | User login, OTP | user_management_db | User/beneficiary validation requests |
| **Banking** | Accounts, Account numbers | Account ownership | banking_db, balances | Balance checks, debit/credit requests |
| **Payment** | Transactions, References | Transaction rules, limits | payment_db | Nothing (it initiates requests) |

**Golden Rule:**
- User Management knows about **PEOPLE**
- Banking Service knows about **MONEY**
- Payment Service knows about **TRANSACTIONS**

Each service does ONE job well and asks others for help via Kafka when needed.