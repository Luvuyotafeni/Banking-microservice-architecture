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
                </head>
                <body>
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
                </head>
                <body>
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
                </head>
                <body>
                </body>
                </html>
                """, userName, resetToken
        );
    }
}
