package com.banking.paymentService.service;

import com.banking.paymentService.dto.request.CreateTransactionRequest;
import com.banking.paymentService.dto.request.UpdateTransactionStatusRequest;
import com.banking.paymentService.dto.response.TransactionResponse;
import com.banking.paymentService.dto.response.TransactionSummaryResponse;
import com.banking.paymentService.enums.TransactionType;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request, UUID userId);

    TransactionResponse getTransactionById(UUID transactionId, UUID userId);

    TransactionResponse getTransactionByReference(String reference, UUID userId);

    Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable);

    Page<TransactionResponse> getUserTransactionByType(UUID userId, TransactionType type, Pageable pageable);

    Page<TransactionResponse> getUserTransactionByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<TransactionResponse> searchTransactions(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable
    );

    TransactionSummaryResponse getTransactionSummary(UUID userId);

    Page<TransactionResponse> updateTransactionStatus(
            UUID transactionId, UpdateTransactionStatusRequest request
    );

    void cancelTransaction(UUID transactionId);

    TransactionResponse reverseTransaction(UUID transactionId, String reason);

    List<TransactionResponse> getPendingTransactions();

    void processStaleTransactions();
}
