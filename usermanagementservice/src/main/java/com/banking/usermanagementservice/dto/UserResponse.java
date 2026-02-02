package com.banking.usermanagementservice.dto;

import com.banking.usermanagementservice.enums.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String gender;
    private String country;
    private AddressResponse address;
    private ApprovalStatus approvalStatus;
    private LocalDateTime approvedAt;
    private Set<String> roles;
    private boolean isActive;
    private boolean isEmailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
