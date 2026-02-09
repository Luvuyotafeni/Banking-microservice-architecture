package com.banking.usermanagementservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private LocalDateTime passwordCreatedAt;

    @Column(nullable = false)
    private LocalDateTime passwordExpiresAt;

    @Column
    private String currentOtp;

    @Column
    private LocalDateTime otpExpiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isFirstLogin = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean isLocked = false;

    @Column
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column
    private LocalDateTime lockedUntil;

    @Column LocalDateTime lastLoginAt;

    @Column
    private String passwordResetToken;

    @Column
    private LocalDateTime passwordResetTokenExpiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //Helper methods
    public boolean isPasswordExpired(){
        return LocalDateTime.now().isAfter(passwordCreatedAt);
    }

    public boolean isOtpValid(String otp){
        return currentOtp != null
                && currentOtp.equals(otp)
                && otpExpiresAt != null
                && LocalDateTime.now().isBefore(otpExpiresAt);
    }

    public boolean isAccountLocked(){
        return isLocked && lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public void incrementFailedAttemps(){
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5){
            this.isLocked= true;
            this.lockedUntil = LocalDateTime.now().plusHours(1);
        }
    }

    public void resetFailedAttemps(){
        this.failedLoginAttempts = 0;
        this.isLocked = false;
        this.lockedUntil = null;
    }
}
