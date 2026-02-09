package com.banking.paymentService.dto.request;

import lombok.*;


import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitAccountRequest {

    private String accountId;
    private UUID userId;
    private BigDecimal amount;
    private String transactionReference;
    private String description;
}
