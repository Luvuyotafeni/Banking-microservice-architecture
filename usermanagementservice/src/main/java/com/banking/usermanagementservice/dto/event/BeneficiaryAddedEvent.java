package com.banking.usermanagementservice.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryAddedEvent {
    private UUID beneficiaryId;
    private UUID userId;
    private String accountId;
    private String nickname;
    private LocalDateTime addedAt;
}
