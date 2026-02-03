package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {


    private  final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendApprovalEmail(String toEmail, String userName, String otp) {
        log.info("sending approval email to: {}", toEmail);

        String subject = "Your banking account had been approved!";
        String body = buildApprovalEmailBody(userName,otp);

        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendRejectionEmail(String toEmail, String userName, String reason) {
        log.info("Sending rejection email to: {}", toEmail);

        String subject = "Banking Account registration update";
        String body = buildRejectionEmailBody(userName, reason);

        sendEmail(toEmail, subject, body);

    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        log.info("Sending password reset email to: {}", toEmail);

        String subject = " Password reset request";
        String body = buildPasswordRestEmailBody(userName, resetToken);

        sendEmail(toEmail, subject, body);

    }

    @Override
    public void sendPasswordExpirationWarning(String toEmail, String userName, int daysRemaining) {

        log.info("Sending password expiration warning to: {}", toEmail);

        String subject = "Password Expiration Notice";
        String body = buildPasswordExpirationEmailBody(userName, daysRemaining);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildApprovalEmailBody(String userName, String otp){
        return String.format("""
                <!DOCTYPE html>
                            <html>
                            <head>
                                <style>
                                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                                    .otp { font-size: 32px; font-weight: bold; color: #4CAF50; text-align: center;\s
                                           padding: 20px; background-color: #fff; border: 2px dashed #4CAF50; margin: 20px 0; }
                                    .warning { color: #d32f2f; font-weight: bold; }
                                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>Welcome to Our Banking System!</h1>
                                    </div>
                                    <div class="content">
                                        <p>Dear %s,</p>
                
                                        <p>Congratulations! Your account has been approved by our admin team.</p>
                
                                        <p>To complete your registration and set up your account, please use the following One-Time Password (OTP):</p>
                
                                        <div class="otp">%s</div>
                
                                        <p class="warning">⚠️ Important Security Information:</p>
                                        <ul>
                                            <li>This OTP is valid for 15 minutes only</li>
                                            <li>Use this OTP for your first login</li>
                                            <li>After login, you will be required to create a new permanent password</li>
                                            <li>Your password will expire every 90 days for security purposes</li>
                                            <li>Never share your OTP or password with anyone</li>
                                        </ul>
                
                                        <p>If you did not request this account, please contact our support team immediately.</p>
                
                                        <p>Best regards,<br>Banking System Team</p>
                                    </div>
                                    <div class="footer">
                                        <p>This is an automated email. Please do not reply.</p>
                                        <p>&copy; 2026 Banking System. All rights reserved.</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                """, userName, otp
        );
    }

    private String buildRejectionEmailBody(String userName, String reason){
        return String.format("""
                <!DOCTYPE html>
                            <html>
                            <head>
                                <style>
                                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                    .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                                    .reason { background-color: #fff; padding: 15px; border-left: 4px solid #f44336; margin: 15px 0; }
                                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>Account Registration Update</h1>
                                    </div>
                                    <div class="content">
                                        <p>Dear %s,</p>
                
                                        <p>We regret to inform you that your account registration has not been approved at this time.</p>
                
                                        <div class="reason">
                                            <strong>Reason:</strong> %s
                                        </div>
                
                                        <p>If you believe this decision was made in error or you have questions, please contact our support team.</p>
                
                                        <p>Thank you for your interest in our banking services.</p>
                
                                        <p>Best regards,<br>Banking System Team</p>
                                    </div>
                                    <div class="footer">
                                        <p>This is an automated email. Please do not reply.</p>
                                        <p>&copy; 2026 Banking System. All rights reserved.</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                """, userName, reason
        );
    }

    private String buildPasswordRestEmailBody(String userName, String resetToken){
        return String.format("""
                <!DOCTYPE html>
                            <html>
                            <head>
                                <style>
                                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                                    .token { font-size: 24px; font-weight: bold; color: #2196F3; text-align: center;\s
                                            padding: 20px; background-color: #fff; border: 2px solid #2196F3; margin: 20px 0;\s
                                            word-break: break-all; }
                                    .warning { color: #d32f2f; font-weight: bold; }
                                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>Password Reset Request</h1>
                                    </div>
                                    <div class="content">
                                        <p>Dear %s,</p>
                
                                        <p>We received a request to reset your password. Use the following token to reset your password:</p>
                
                                        <div class="token">%s</div>
                
                                        <p class="warning">⚠️ Security Notice:</p>
                                        <ul>
                                            <li>This reset token is valid for 1 hour only</li>
                                            <li>If you didn't request this reset, please ignore this email</li>
                                            <li>Never share this token with anyone</li>
                                        </ul>
                
                                        <p>Best regards,<br>Banking System Team</p>
                                    </div>
                                    <div class="footer">
                                        <p>This is an automated email. Please do not reply.</p>
                                        <p>&copy; 2026 Banking System. All rights reserved.</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                """, userName, resetToken
        );
    }

    private String buildPasswordExpirationEmailBody(String userName, int daysRemaining){
        return String.format("""
                <!DOCTYPE html>
                            <html>
                            <head>
                                <style>
                                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                    .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                                    .days { font-size: 48px; font-weight: bold; color: #FF9800; text-align: center; margin: 20px 0; }
                                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>Password Expiration Notice</h1>
                                    </div>
                                    <div class="content">
                                        <p>Dear %s,</p>
                
                                        <p>This is a reminder that your password will expire in:</p>
                
                                        <div class="days">%d days</div>
                
                                        <p>To maintain the security of your account, please change your password before it expires.</p>
                
                                        <p>You can change your password by logging into your account and navigating to the account settings.</p>
                
                                        <p>Best regards,<br>Banking System Team</p>
                                    </div>
                                    <div class="footer">
                                        <p>This is an automated email. Please do not reply.</p>
                                        <p>&copy; 2026 Banking System. All rights reserved.</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                """, userName, daysRemaining
        );
    }
}
