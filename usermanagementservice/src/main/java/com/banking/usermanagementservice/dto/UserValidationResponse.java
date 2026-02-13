package com.banking.usermanagementservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserValidationResponse {
    private UUID userId;
    private String requestId;
    private boolean valid;
    private boolean active;
    private String email;
    private String message;
}
