package com.banking.paymentService.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitAccountRequest {

    private String accountId;
    private UUID userId;
    private BigDecimal requiredAmount;
    private String transactionReference;
    private String description;
}
