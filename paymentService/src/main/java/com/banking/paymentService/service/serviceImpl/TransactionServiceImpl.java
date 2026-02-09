package com.banking.paymentService.service.serviceImpl;

import com.banking.paymentService.dto.request.CreateTransactionRequest;
import com.banking.paymentService.dto.request.UpdateTransactionStatusRequest;
import com.banking.paymentService.dto.response.TransactionResponse;
import com.banking.paymentService.dto.response.TransactionSummaryResponse;
import com.banking.paymentService.Entity.Transaction;
import com.banking.paymentService.enums.TransactionStatus;
import com.banking.paymentService.enums.TransactionType;
import com.banking.paymentService.message.TransactionEventProducer;
import com.banking.paymentService.repository.TransactionRepository;
import com.banking.paymentService.service.TransactionService;
import com.banking.paymentService.service.TransactionValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionValidationService validationService;
    private final TransactionEventProducer eventProducer;

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request, UUID userId) {
        log.info("Creating transaction for user: {}, type: {}, amount: {}",
                userId, request.getType(), request.getAmount());

        try {
            // Step 1: Validate the transaction
            validationService.validateTransaction(request, userId);

            // Step 2: Calculate fee
            BigDecimal fee = validationService.calculateFee(request.getType(), request.getAmount());
            BigDecimal totalAmount = request.getAmount().add(fee);

            // Step 3: Create transaction entity
            Transaction transaction = buildTransaction(request, userId, fee, totalAmount);

            // Step 4: Save as pending
            transaction.setStatus(TransactionStatus.PENDING);
            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction created with ID: {} and reference: {}",
                    savedTransaction.getId(), savedTransaction.getReference());

            // Step 5: Send balance check request via Kafka (for debit transactions)
            if (isDebitTransaction(request.getType())) {
                log.info("Sending balance check request via Kafka for transaction: {}",
                        savedTransaction.getReference());
                eventProducer.sendBalanceCheckRequest(savedTransaction);
            }

            // Step 6: Process the transaction asynchronously via Kafka
            processTransactionViaKafka(savedTransaction);

            return mapToResponse(savedTransaction);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Process transaction by sending Kafka events to Banking Service
     */
    private void processTransactionViaKafka(Transaction transaction) {
        try {
            // Update status to processing
            transaction.setStatus(TransactionStatus.PROCESSING);
            transactionRepository.save(transaction);

            log.info("Processing transaction {} via Kafka", transaction.getReference());

            // Send appropriate Kafka events based on transaction type
            switch (transaction.getType()) {
                case TRANSFER -> processTransferViaKafka(transaction);
                case WITHDRAWAL -> processWithdrawalViaKafka(transaction);
                case DEPOSIT -> processDepositViaKafka(transaction);
                case BILL_PAYMENT -> processBillPaymentViaKafka(transaction);
            }

            log.info("Kafka events sent for transaction: {}", transaction.getReference());

        } catch (Exception e) {
            log.error("Error processing transaction via Kafka: ", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason("Failed to send Kafka event: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction processing failed", e);
        }
    }

    /**
     * Process transfer by sending debit and credit requests via Kafka
     */
    private void processTransferViaKafka(Transaction transaction) {
        log.info("Processing transfer via Kafka from {} to {}",
                transaction.getAccountId(), transaction.getDestinationAccountId());

        // 1. Send debit request for source account
        eventProducer.sendDebitAccountRequest(transaction);
        log.info("Debit request sent for account: {}", transaction.getAccountId());

        // 2. Send credit request for destination account
        // Note: For transfers, we need to send a separate credit request
        eventProducer.sendCreditAccountRequest(transaction);
        log.info("Credit request sent for account: {}", transaction.getDestinationAccountId());
    }

    /**
     * Process withdrawal by sending debit request via Kafka
     */
    private void processWithdrawalViaKafka(Transaction transaction) {
        log.info("Processing withdrawal via Kafka from account {}", transaction.getAccountId());

        // Send debit request to Banking Service
        eventProducer.sendDebitAccountRequest(transaction);
        log.info("Debit request sent for withdrawal from account: {}", transaction.getAccountId());
    }

    /**
     * Process deposit by sending credit request via Kafka
     */
    private void processDepositViaKafka(Transaction transaction) {
        log.info("Processing deposit via Kafka to account {}", transaction.getAccountId());

        // Send credit request to Banking Service
        eventProducer.sendCreditAccountRequest(transaction);
        log.info("Credit request sent for deposit to account: {}", transaction.getAccountId());
    }

    /**
     * Process bill payment by sending debit request via Kafka
     */
    private void processBillPaymentViaKafka(Transaction transaction) {
        log.info("Processing bill payment via Kafka: {}", transaction.getDescription());

        // Send debit request to Banking Service
        eventProducer.sendDebitAccountRequest(transaction);
        log.info("Debit request sent for bill payment from account: {}", transaction.getAccountId());

        // TODO: Send event to external bill payment provider if needed
    }

    /**
     * Called by Kafka consumer when transaction is completed
     * This method will be invoked from the consumer after receiving success response
     */
    public void completeTransaction(String transactionReference, BigDecimal newBalance) {
        log.info("Completing transaction: {} with new balance: {}", transactionReference, newBalance);

        Transaction transaction = transactionRepository.findByReference(transactionReference)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with reference: " + transactionReference));

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setBalanceAfter(newBalance);
        transactionRepository.save(transaction);

        // Send notification via Kafka
        eventProducer.sendTransactionNotification(transaction);
        log.info("Transaction {} completed successfully and notification sent", transactionReference);
    }

    /**
     * Called by Kafka consumer when transaction fails
     */
    public void failTransaction(String transactionReference, String reason) {
        log.warn("Failing transaction: {} with reason: {}", transactionReference, reason);

        Transaction transaction = transactionRepository.findByReference(transactionReference)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with reference: " + transactionReference));

        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailedAt(LocalDateTime.now());
        transaction.setFailureReason(reason);
        transactionRepository.save(transaction);

        // Send notification about failed transaction
        eventProducer.sendTransactionNotification(transaction);
        log.info("Transaction {} marked as failed and notification sent", transactionReference);
    }

    private boolean isDebitTransaction(TransactionType type) {
        return type == TransactionType.TRANSFER
                || type == TransactionType.WITHDRAWAL
                || type == TransactionType.BILL_PAYMENT;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId, UUID userId) {
        log.info("Fetching transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with ID: " + transactionId));

        // Verify transaction belongs to user
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Transaction not found");
        }

        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String reference, UUID userId) {
        log.info("Fetching transaction with reference: {} for user {}", reference, userId);

        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with reference: " + reference));

        // Verify transaction belongs to user
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Transaction not found");
        }

        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        log.info("Fetching transactions for user: {}", userId);

        return transactionRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactionByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Fetching transactions for user: {} between {} and {}", userId, startDate, endDate);

        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactionByType(
            UUID userId, TransactionType type, Pageable pageable) {

        log.info("Fetching {} transactions for user: {}", type, userId);

        return transactionRepository.findByUserIdAndType(userId, type, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionResponse> getUserTransactionsByDateRange(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        log.info("Fetching transactions for user: {} between {} and {}", userId, startDate, endDate);

        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> searchTransactions(
            UUID userId, String searchTerm, Pageable pageable) {

        log.info("Searching transactions for user: {} with term: {}", userId, searchTerm);

        return transactionRepository.searchByUserId(userId, searchTerm, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getTransactionSummary(UUID userId) {
        log.info("Getting transaction summary for user: {}", userId);

        List<Transaction> userTransactions = transactionRepository
                .findByUserId(userId, Pageable.unpaged())
                .getContent();

        long total = userTransactions.size();
        long completed = userTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .count();
        long failed = userTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.FAILED)
                .count();
        long pending = userTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PENDING
                        || t.getStatus() == TransactionStatus.PROCESSING)
                .count();

        BigDecimal totalAmount = userTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = userTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .map(t -> t.getFee() != null ? t.getFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return TransactionSummaryResponse.builder()
                .totalTransactions(total)
                .completedTransactions(completed)
                .failedTransactions(failed)
                .pendingTransactions(pending)
                .totalAmount(totalAmount)
                .totalFees(totalFees)
                .build();
    }

    @Override
    public TransactionResponse updateTransactionStatus(
            UUID transactionId, UpdateTransactionStatusRequest request) {

        log.info("Updating transaction {} status to {}", transactionId, request.getStatus());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with ID: " + transactionId));

        transaction.setStatus(request.getStatus());

        if (request.getStatus() == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
            // Send notification
            eventProducer.sendTransactionNotification(transaction);
        } else if (request.getStatus() == TransactionStatus.FAILED) {
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason(request.getFailureReason());
            // Send notification
            eventProducer.sendTransactionNotification(transaction);
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Override
    public void cancelTransaction(UUID transactionId) {
        log.info("Cancelling transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with ID: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending transactions can be cancelled");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        // Send notification about cancellation
        eventProducer.sendTransactionNotification(transaction);
    }

    @Override
    public TransactionResponse reverseTransaction(UUID transactionId, String reason) {
        log.info("Reversing transaction: {}", transactionId);

        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with ID: " + transactionId));

        if (originalTransaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new RuntimeException(
                    "Only completed transactions can be reversed");
        }

        // Mark original as reversed
        originalTransaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(originalTransaction);

        // Send notification about reversal
        eventProducer.sendTransactionNotification(originalTransaction);

        // Create reversal transaction
        Transaction reversal = Transaction.builder()
                .userId(originalTransaction.getUserId())
                .accountId(originalTransaction.getAccountId())
                .type(originalTransaction.getType())
                .status(TransactionStatus.PENDING)
                .amount(originalTransaction.getAmount())
                .fee(BigDecimal.ZERO)
                .description("REVERSAL: " + (reason != null ? reason : "Transaction reversed"))
                .destinationAccountId(originalTransaction.getAccountId()) // Credit back to original account
                .build();

        Transaction savedReversal = transactionRepository.save(reversal);

        // Process reversal via Kafka (credit the original account)
        eventProducer.sendCreditAccountRequest(savedReversal);

        return mapToResponse(savedReversal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getPendingTransactions() {
        log.info("Fetching pending transactions");

        return transactionRepository.findByStatus(TransactionStatus.PENDING, Pageable.unpaged())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void processStaleTransactions() {
        log.info("Processing stale transactions");

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Transaction> staleTransactions = transactionRepository.findStaleTransactions(threshold);

        log.info("Found {} stale transactions", staleTransactions.size());

        staleTransactions.forEach(transaction -> {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason("Transaction timed out");
            transactionRepository.save(transaction);

            // Send notification about timeout
            eventProducer.sendTransactionNotification(transaction);

            log.warn("Marked transaction {} as failed due to timeout", transaction.getReference());
        });
    }

    private Transaction buildTransaction(
            CreateTransactionRequest request,
            UUID userId,
            BigDecimal fee,
            BigDecimal totalAmount) {

        return Transaction.builder()
                .userId(userId)
                .accountId(request.getAccountId())
                .beneficiaryId(request.getBeneficiaryId())
                .type(request.getType())
                .amount(request.getAmount())
                .fee(fee)
                .totalAmount(totalAmount)
                .description(request.getDescription())
                .destinationAccountId(request.getDestinationAccountId())
                .deviceType(request.getDeviceType())
                .deviceId(request.getDeviceId())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .location(request.getLocation())
                .build();
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .accountId(t.getAccountId())
                .beneficiaryId(t.getBeneficiaryId())
                .type(t.getType())
                .status(t.getStatus())
                .amount(t.getAmount())
                .fee(t.getFee())
                .totalAmount(t.getTotalAmount())
                .currency(t.getCurrency())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .reference(t.getReference())
                .destinationAccountId(t.getDestinationAccountId())
                .balanceBefore(t.getBalanceBefore())
                .balanceAfter(t.getBalanceAfter())
                .failureReason(t.getFailureReason())
                .completedAt(t.getCompletedAt())
                .failedAt(t.getFailedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}