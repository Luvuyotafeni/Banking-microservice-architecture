package com.banking.usermanagementservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryValidationRequest {
    private UUID beneficiaryId;
    private UUID userId;
    private String requestId;
}
