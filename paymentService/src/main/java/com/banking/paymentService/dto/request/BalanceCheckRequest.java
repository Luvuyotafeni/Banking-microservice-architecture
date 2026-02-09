package com.banking.paymentService.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.Normalized;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceCheckRequest {

    private String accountId;
    private UUID userId;
    private BigDecimal requiredAmount;
    private String transactionReference;
}
