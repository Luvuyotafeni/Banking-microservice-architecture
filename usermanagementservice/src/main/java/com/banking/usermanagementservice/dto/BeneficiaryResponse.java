package com.banking.usermanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {

    private UUID id;

    private String accountId;

    private String nickname;

    private boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private int userCount;
}
