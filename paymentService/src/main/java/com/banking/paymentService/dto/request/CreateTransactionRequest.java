package com.banking.paymentService.dto.request;

import com.banking.paymentService.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {

    @NotBlank(message = "Account Id is required")
    private String accountId;

    private UUID beneficiaryId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 50, message = "Descriptiom must not exceed 50 characters")
    private String description;

    private String destinationAccountId;

    private String deviceType;

    private String deviceId;

    private String ipAddress;

    private String userAgent;

    private String location;


}
