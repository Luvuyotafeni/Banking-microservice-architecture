package com.banking.usermanagementservice.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSuspendedEvent {

    private UUID userId;
    private String email;
    private String fullName;
    private boolean suspended;
    private String reason;
    private UUID suspendedBy;
    private LocalDateTime suspendedAt;
}
