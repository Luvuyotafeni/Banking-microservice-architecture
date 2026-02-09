package com.banking.usermanagementservice.service;

public interface EmailService {

    /**
     * Send account approval email with OTP
     * @param toEmail Recipient email
     * @param userName User's full name
     * @param otp One-time password
     */
    void sendApprovalEmail(String toEmail, String userName, String otp);

    /**
     * Send account rejection email
     * @param toEmail Recipient email
     * @param userName User's full name
     * @param reason Rejection reason
     */
    void sendRejectionEmail(String toEmail, String userName, String reason);

    /**
     * Send password reset email
     * @param toEmail Recipient email
     * @param userName User's full name
     * @param resetToken Password reset token
     */
    void sendPasswordResetEmail(String toEmail, String userName, String resetToken);

    /**
     * Send password expiration warning email
     * @param toEmail Recipient email
     * @param userName User's full name
     * @param daysRemaining Days until password expires
     */
    void sendPasswordExpirationWarning(String toEmail, String userName, int daysRemaining);
}
