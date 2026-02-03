package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.*;
import com.banking.usermanagementservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    @Override
    public AuthenticationResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public AuthenticationResponse loginWithOtp(OtpLoginRequest request) {
        return null;
    }

    @Override
    public AuthenticationResponse changePasswordFirstLogin(UUID userId, PasswordChangeRequest request) {
        return null;
    }

    @Override
    public void changePassword(UUID userId, String currentPassword, PasswordChangeRequest request) {

    }

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {

    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {

    }

    @Override
    public AuthenticationResponse refreshToken(RefreshToken request) {
        return null;
    }

    @Override
    public void logout(UUID userId) {

    }
}
