package com.banking.paymentService.dto.event;

import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOperationResponse {

    private String accountId;
    private String transactionReference;
    private boolean success;
    private BigDecimal newBalance;
    private String message;
    private LocalDateTime timeStamp;

}
