package com.banking.usermanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBeneficiaryRequest {

    @NotBlank(message = "Account Id is required")
    private String accountId;
}
