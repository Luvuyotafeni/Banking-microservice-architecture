package com.banking.usermanagementservice.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginEvent {

    private UUID userId;
    private String email;
    private String fullName;
    private Set<String> roles;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private boolean isFirstLogin;
}
