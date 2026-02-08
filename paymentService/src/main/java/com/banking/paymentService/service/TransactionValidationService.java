package com.banking.paymentService.service;


import com.banking.paymentService.dto.request.CreateTransactionRequest;
import com.banking.paymentService.enums.TransactionType;
import com.banking.paymentService.service.serviceImpl.TransactionLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionValidationService {

    private final TransactionLimitService transactionLimitService;

    @Value("${payment.limits.max-transfer-amount}")
    private BigDecimal maxTransferAmount;

    @Value("${payment.limits.max-withdrawal-amount}")
    private BigDecimal maxWithdrawalAmount;

    @Value("${payment.limits.min-amount}")
    private BigDecimal minAmount;

    @Value("${payment.limits.max-daily-transactions}")
    private int maxDailyTransactions;

    public void validateTransaction(CreateTransactionRequest request, UUID userId){
        log.info("Validating transaction for user: {}, type: {}, amount: {}",
                userId, request.getType(), request.getAmount());

        validateAmount(request.getAmount());
        validateTransactionType(request);
        validateTransactionLimits(request);
        validateDailyTransactionCount(userId);
    }

    private void validateAmount(BigDecimal amount){
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Transaction amount must be greater than zero");
        }

        if (amount.compareTo(minAmount) < 0){
            throw new RuntimeException(
                    "Transaction amount must be at least" + minAmount);
        }
    }

    private void validateTransactionType(CreateTransactionRequest request){

        if (request.getType() == TransactionType.TRANSFER){
            if (request.getBeneficiaryId() == null){
                throw new RuntimeException(
                        "Beneficiary ID is required for transfers"
                );
            }
            if (request.getDestinationAccountId() == null || request.getDestinationAccountId().isBlank()){
                throw new RuntimeException(
                        "Destination account ID is required for transfers"
                );
            }
        }

        if (request.getType() == TransactionType.BILL_PAYMENT){
            if (request.getDescription() == null || request.getDescription().isBlank()){
                throw new RuntimeException(
                        "Description is required for bill payments"
                );
            }
        }
    }

    private void validateTransactionLimits(CreateTransactionRequest request){
        BigDecimal amount = request.getAmount();

        if (request.getType() == TransactionType.TRANSFER && amount.compareTo(maxTransferAmount) > 0){
            throw new RuntimeException(
                    "Transfer amount cannot exceed" + maxTransferAmount
            );
        }

        if (request.getType() == TransactionType.WITHDRAWAL && amount.compareTo(maxTransferAmount) > 0){
            throw new RuntimeException(
                    "Withdrawal amount cannot exceed" + maxWithdrawalAmount
            );
        }
    }

    private void validateDailyTransactionCount(UUID userId){

        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long todayCount = transactionLimitService.getTransactionCountSince(userId, startOfDay);

        if (todayCount >= maxDailyTransactions) {
            throw new RuntimeException(
                    "Daily transaction limit of "+ maxDailyTransactions + "exceed"
            );
        }
    }

    public void validateBalance(String accountId, UUID userId, BigDecimal requiredAmount) {
        log.info("Validating balance for account: {}, required amount: {}",
                accountId, requiredAmount);

        BigDecimal simulatedBalance = new BigDecimal("100000.00");

        if (simulatedBalance.compareTo(requiredAmount) < 0){
            throw new RuntimeException(
                    "Insufficient balance. Available: " + simulatedBalance + ", Required: "+ requiredAmount
            );
        }
    }

    public BigDecimal calculateFee(TransactionType type, BigDecimal amount){
        return switch (type){
            case TRANSFER -> {
                BigDecimal transferFee = amount.multiply(new BigDecimal("0.005"));
                yield transferFee.min(new BigDecimal("25.000"));
            }
            case WITHDRAWAL -> new BigDecimal("2.00");
            case BILL_PAYMENT -> new BigDecimal("1.00");
            case DEPOSIT -> BigDecimal.ZERO;
        };
    }

}
