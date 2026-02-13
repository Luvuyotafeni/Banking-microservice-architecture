package com.banking.usermanagementservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserValidationRequest {

    private UUID userId;
    private String requestId;
}
