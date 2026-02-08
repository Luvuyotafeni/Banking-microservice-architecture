package com.banking.paymentService.dto.response;

import com.banking.paymentService.enums.TransactionStatus;
import com.banking.paymentService.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private UUID id;

    private UUID userId;
    private String accountId;
    private UUID beneficiaryId;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime transactionDate;
    private String description;
    private String reference;
    private String destinationAccountId;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String failureReason;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
