package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.service.EmailService;

public class EmailServiceImpl implements EmailService {
    @Override
    public void sendApprovalEmail(String toEmail, String userName, String otp) {

    }

    @Override
    public void sendRejectionEmail(String toEmail, String userName, String reason) {

    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {

    }

    @Override
    public void sendPasswordExpirationWarning(String toEmail, String userName, int daysRemaining) {

    }
}
