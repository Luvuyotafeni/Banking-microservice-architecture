package com.banking.usermanagementservice.service;

import java.util.UUID;

public interface OtpService {
    /**
     * Generate and store OTP for a user
     * @param userId User ID
     * @return Generated OTP
     */
    String generateOtp(UUID userId);

    /**
     * Validate OTP for a user
     * @param userId User ID
     * @param otp OTP to validate
     * @return true if valid, false otherwise
     */
    boolean validateOtp(UUID userId, String otp);

    /**
     * Invalidate/clear OTP for a user
     * @param userId User ID
     */
    void invalidateOtp(UUID userId);

    /**
     * Check if OTP is expired
     * @param userId User ID
     * @return true if expired, false otherwise
     */
    boolean isOtpExpired(UUID userId);
}
