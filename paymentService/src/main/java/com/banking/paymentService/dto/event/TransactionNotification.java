package com.banking.paymentService.dto.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionNotification {
    private UUID transactionId;
    private UUID userId;
    private String accountId;
    private String type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String reference;
    private LocalDateTime timestamp;
}
