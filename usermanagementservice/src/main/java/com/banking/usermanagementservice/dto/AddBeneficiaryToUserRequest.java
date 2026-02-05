package com.banking.usermanagementservice.dto;

import jakarta.validation.constraints.NotBlank;

public class AddBeneficiaryToUserRequest {

    @NotBlank(message = "Beneficiary ID is required")
    private String beneficiaryId;
}
