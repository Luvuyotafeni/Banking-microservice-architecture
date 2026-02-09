package com.banking.usermanagementservice.service;


import com.banking.usermanagementservice.dto.*;

import java.util.UUID;

public interface AuthenticationService {

    /**
     * Authenticate user with email and password
     * @param request Login credentials
     * @return Authentication response with tokens
     */
    AuthenticationResponse login(LoginRequest request);

    /**
     * First login with OTP (after approval)
     * @param request OTP login credentials
     * @return Authentication response (requires password change)
     */
    AuthenticationResponse loginWithOtp(OtpLoginRequest request);

    /**
     * Change password after first login
     * @param userId User ID
     * @param request New password details
     * @return Authentication response with new tokens
     */
    AuthenticationResponse changePasswordFirstLogin(UUID userId, PasswordChangeRequest request);

    /**
     * Change password for existing user
     * @param userId User ID
     * @param currentPassword Current password
     * @param request New password details
     */
    void changePassword(UUID userId, String currentPassword, PasswordChangeRequest request);

    /**
     * Request password reset
     * @param request Email for password reset
     */
    void requestPasswordReset(PasswordResetRequest request);

    /**
     * Confirm password reset with token
     * @param request Reset token and new password
     */
    void confirmPasswordReset(PasswordResetConfirmRequest request);

    /**
     * Refresh access token using refresh token
     * @param request Refresh token
     * @return New authentication response
     */
    AuthenticationResponse refreshToken(RefreshToken request);

    /**
     * Logout user (invalidate tokens)
     * @param userId User ID
     */
    void logout(UUID userId);
}
