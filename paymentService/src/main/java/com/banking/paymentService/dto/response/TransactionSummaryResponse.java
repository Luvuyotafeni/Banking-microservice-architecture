package com.banking.paymentService.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummaryResponse {

    private Long totalTransactions;
    private Long completedTransactions;
    private Long failedTransactions;
    private Long pendingTransactions;
    private BigDecimal totalAmount;
    private BigDecimal totalFees;
}
