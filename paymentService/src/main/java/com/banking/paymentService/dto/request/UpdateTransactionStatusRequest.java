package com.banking.paymentService.dto.request;

import com.banking.paymentService.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTransactionStatusRequest {

    @NotNull(message = "Status is required")
    private TransactionStatus status;

    private String failureReason;
}
