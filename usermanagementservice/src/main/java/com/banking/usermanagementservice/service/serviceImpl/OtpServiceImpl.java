package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.entity.UserCredentials;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.repository.UserCredentialsRepository;
import com.banking.usermanagementservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final UserCredentialsRepository credentialsRepository;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiration-minutes:15}")
    private int otpExpirationMinutes;

    private static final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public String generateOtp(UUID userId) {
        log.info("Generating OTP for user: {}", userId);

        UserCredentials credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User credentials not found"));

        String otp = generateRandomOtp();

        credentials.setCurrentOtp(otp);
        credentials.setOtpExpiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes));

        credentialsRepository.save(credentials);

        log.info("OTP generated successfully for user: {}", userId);
        return otp;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateOtp(UUID userId, String otp) {
        log.info("Validating OTP for user: {}", userId);

        UserCredentials credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User credentials not found"));

        boolean isValid = credentials.isOtpValid(otp);
        if(!isValid){
            log.warn("OTP Validation failed for user: {}", userId);

        }
        return isValid;
    }

    @Override
    @Transactional
    public void invalidateOtp(UUID userId) {
        log.info("Invalidating OTP for user: {}", userId);

        UserCredentials credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User credentials not found"));

        credentials.setCurrentOtp(null);
        credentials.setOtpExpiresAt(null);

        credentialsRepository.save(credentials);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOtpExpired(UUID userId) {
        UserCredentials credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User credentials not found"));

        return credentials.getOtpExpiresAt() == null
                || LocalDateTime.now().isAfter(credentials.getOtpExpiresAt());
    }

    private String generateRandomOtp(){
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++){
            otp.append(random.nextInt(10));
        }
        return  otp.toString();
    }
}
