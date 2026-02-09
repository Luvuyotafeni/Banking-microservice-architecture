package com.banking.paymentService.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceCheckResponse {

    private String accountId;
    private String transactionReference;
    private boolean sufficientBalance;
    private BigDecimal currentBalance;
    private BigDecimal avalableBalance;
    private String message;
}
