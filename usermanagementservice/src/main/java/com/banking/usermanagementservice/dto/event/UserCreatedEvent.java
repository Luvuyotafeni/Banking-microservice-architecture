package com.banking.usermanagementservice.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private LocalDateTime createdAt;
}
