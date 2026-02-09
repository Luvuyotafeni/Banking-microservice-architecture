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
public class UserSuspensionRequest {

    @NotNull(message = "User Id is required")
    private UUID userId;

    @NotNull(message = "Suspension status is required")
    private Boolean suspend;

    @NotBlank(message = "Reason is required")
    private String reason;
}
