package com.banking.usermanagementservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryValidationResponse {
    private UUID beneficiaryId;
    private String requestId;
    private boolean valid;
    private boolean active;
    private boolean belongsToUser;
    private String accountId;
    private String message;
}
