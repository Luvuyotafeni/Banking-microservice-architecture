package com.banking.usermanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApprovalRequest {

    @NotNull(message = "User Id is required")
    private UUID userId;

    @NotNull(message = "Approval decision is required")
    private Boolean approved;

    @NotBlank(message = "Reason is required")
    private String reason;
}
