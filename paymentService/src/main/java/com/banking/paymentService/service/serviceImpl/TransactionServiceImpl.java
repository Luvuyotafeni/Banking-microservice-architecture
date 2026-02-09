package com.banking.paymentService.service.serviceImpl;

import com.banking.paymentService.Entity.Transaction;
import com.banking.paymentService.dto.request.CreateTransactionRequest;
import com.banking.paymentService.dto.request.UpdateTransactionStatusRequest;
import com.banking.paymentService.dto.response.TransactionResponse;
import com.banking.paymentService.dto.response.TransactionSummaryResponse;
import com.banking.paymentService.enums.TransactionStatus;
import com.banking.paymentService.enums.TransactionType;
import com.banking.paymentService.exceptions.TransactionNotFoundException;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionValidationService validationService;
    // TODO: Inject Kafka producers when enabled
    // private final TransactionEventProducer eventProducer;

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request, UUID userId) {
        log.info("Creating transaction for user: {}, type: {}, amount: {}",
                userId, request.getType(), request.getAmount());

        try{
            // Step 1: Validate the transaction
            validationService.validateTransaction(request, userId);


            // Step 2: Calculate fee
            BigDecimal fee = validationService.calculateFee(request.getType(), request.getAmount());
            BigDecimal totalAmount = request.getAmount().add(fee);

            // Step 3: Validate balance (for debit transactions)
            if (isDebitTransaction(request.getType())){
                validationService.validateBalance(request.getAccountId(), userId, totalAmount);
            }

            // Step 4: Create transaction entity
            Transaction transaction = buildTransaction(request, userId, fee, totalAmount);

            // Step 5: Save as pending
            transaction.setStatus(TransactionStatus.PENDING);
            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction created with ID: {} and refernce: {}", savedTransaction.getId(), savedTransaction.getReference());

            // Step 6: Process the transaction
            processTransaction(savedTransaction);

            return mapToResponse(savedTransaction);
        } catch (Exception e){
            log.error("Transaction validation failed: {}", e.getMessage());
            throw e;
        }
    }

    private void processTransaction(Transaction transaction){
        try{
            transaction.setStatus(TransactionStatus.PROCESSING);
            transactionRepository.save(transaction);

            // TODO: Kafka integration will happen here
            // Send event based on transaction type
            switch (transaction.getType()){
                case TRANSFER -> processTransfer(transaction);
                case WITHDRAWAL -> processWithdrawal(transaction);
                case DEPOSIT -> processDeposit(transaction);
                case BILL_PAYMENT -> processBillPayment(transaction);
            }

            // Mark as completed
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            // Update balance snapshots (will come from Kafka response)
            updateBalanceSnapshots(transaction);

            transactionRepository.save(transaction);
            log.info("Transaction {} completed successfully ", transaction.getReference());

            // TODO: Send transaction notification via Kafka
            // eventProducer.sendTransactionNotification(transaction);
        } catch (Exception e) {
            log.error("Transaction processing failed: ", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction processing failed", e);
        }
    }

    private void processTransfer(Transaction transaction){
        log.info("Processing transfer from {} to {}", transaction.getAccountId(), transaction.getDestinationAccountId());

        // TODO: Kafka message to banking service
        // 1. Send debit request for source account
        // 2. Send credit request for destination account
        // eventProducer.sendDebitAccountRequest(transaction);
        // eventProducer.sendCreditAccountRequest(transaction);

        // Simulated processing
        try{
            Thread.sleep(100);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void processWithdrawal(Transaction transaction){
        log.info("Processing withdrawal form account {}", transaction.getAccountId());

        // TODO: Kafka message to banking service to debit account
        // eventProducer.sendDebitAccountRequest(transaction);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void processDeposit(Transaction transaction){
        log.info("Processing deposit to account {}", transaction.getAccountId());

        // TODO: Kafka message to banking service to credit account
        // eventProducer.sendCreditAccountRequest(transaction);
        try{
            Thread.sleep(50);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void processBillPayment(Transaction transaction){
        log.info("Processing bill payment: {}", transaction.getDescription());
        // TODO: Kafka messages to banking service and bill payment provider
        // eventProducer.sendDebitAccountRequest(transaction);
        // eventProducer.sendBillPaymentRequest(transaction);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void updateBalanceSnapshots(Transaction transaction){
        // TODO: Get actual balance from banking service via Kafka
        // For now, simulate balance updates

        BigDecimal simulatedBalanceBefore = new BigDecimal("100000.00");
        BigDecimal simulatedBalanceAfter = isDebitTransaction(transaction.getType())
                ? simulatedBalanceBefore.subtract(transaction.getTotalAmount())
                : simulatedBalanceBefore.add(transaction.getAmount());

        transaction.setBalanceBefore(simulatedBalanceBefore);
        transaction.setBalanceAfter(simulatedBalanceAfter);
    }

    private boolean isDebitTransaction(TransactionType type){
        return type == TransactionType.TRANSFER
                || type == TransactionType.WITHDRAWAL
                || type == TransactionType.BILL_PAYMENT;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId, UUID userId) {
        log.info("Fetching transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(()-> new RuntimeException(
                        "Transaction not found with ID: "+ transactionId
                ));

        // Verify transaction belongs to user
        if (!transaction.getUserId().equals(userId)){
            throw new RuntimeException("Transaction not found");
        }
        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String reference, UUID userId) {
        log.info("Fetching transaction with reference: {} for user {}", reference, userId);

        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with reference: " + reference));

        // Verify transaction belongs to user
        if (!transaction.getUserId().equals(userId)) {
            throw new TransactionNotFoundException("Transaction not found");
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
    public Page<TransactionResponse> getUserTransactionByType(UUID userId, TransactionType type, Pageable pageable) {
        log.info("Fetching {} transactions for user: {}", type, userId);

        return transactionRepository.findByUserIdAndType(userId, type, pageable)
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
    public Page<TransactionResponse> searchTransactions(UUID userId, String searchTerm, Pageable pageable) {
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
    public TransactionResponse updateTransactionStatus(UUID transactionId, UpdateTransactionStatusRequest request) {
        log.info("Updating transaction {} status to {}", transactionId, request.getStatus());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with ID: " + transactionId));

        transaction.setStatus(request.getStatus());

        if (request.getStatus() == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
        } else if (request.getStatus() == TransactionStatus.FAILED) {
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason(request.getFailureReason());
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Override
    public void cancelTransaction(UUID transactionId) {

        log.info("Cancelling transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with ID: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending transactions can be cancelled");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
    }

    @Override
    public TransactionResponse reverseTransaction(UUID transactionId, String reason) {
        log.info("Reversing transaction: {}", transactionId);

        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with ID: " + transactionId));

        if (originalTransaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new RuntimeException(
                    "Only completed transactions can be reversed");
        }

        // Mark original as reversed
        originalTransaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(originalTransaction);

        // Create reversal transaction
        Transaction reversal = Transaction.builder()
                .userId(originalTransaction.getUserId())
                .accountId(originalTransaction.getAccountId())
                .type(originalTransaction.getType())
                .status(TransactionStatus.COMPLETED)
                .amount(originalTransaction.getAmount().negate())
                .fee(BigDecimal.ZERO)
                .description("REVERSAL: " + (reason != null ? reason : "Transaction reversed"))
                .destinationAccountId(originalTransaction.getDestinationAccountId())
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedReversal = transactionRepository.save(reversal);
        return mapToResponse(savedReversal);
    }

    @Override
    public List<TransactionResponse> getPendingTransactions() {
//        log.info("Fetching pending transactions");
//
//        return transactionRepository.findByStatus(TransactionStatus.PENDING, Pageable.unpaged())
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
        return null;
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
            log.warn("Marked transaction {} as failed due to timeout", transaction.getReference());
        });
    }
    private Transaction buildTransaction(
            CreateTransactionRequest request,
            UUID userId,
            BigDecimal fee,
            BigDecimal totalAmount){

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


    private TransactionResponse mapToResponse(Transaction t){
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
