package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.*;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.entity.UserCredentials;
import com.banking.usermanagementservice.exception.InvalidOperationException;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.repository.UserCredentialsRepository;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.security.CustomUserDetailsService;
import com.banking.usermanagementservice.security.JwtTokenProvider;
import com.banking.usermanagementservice.service.AuthenticationService;
import com.banking.usermanagementservice.service.EmailService;
import com.banking.usermanagementservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final OtpService otpService;
    private final EmailService emailService;


    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.security.password.expiration-days}")
    private int passwordExpirationDays;


    @Override
    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Find user
        User user = userRepository.findByEmailAndNotDeleted(request.getEmail().toLowerCase())
                .orElseThrow(() -> new InvalidOperationException("Invalid credentials"));

        // Check if user is active and approved
        if (!user.isActive()) {
            throw new InvalidOperationException("Account is not active. Please contact support.");
        }

        // Get credentials
        UserCredentials credentials = credentialsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidOperationException("Invalid credentials"));

        // Check if account is locked
        if (credentials.isAccountLocked()) {
            throw new InvalidOperationException(
                    "Account is locked due to too many failed attempts. Please try again later."
            );
        }

        // Check if first login (should use OTP)
        if (credentials.isFirstLogin()) {
            throw new InvalidOperationException(
                    "This is your first login. Please use the OTP sent to your email."
            );
        }

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), credentials.getPasswordHash())) {
            credentials.incrementFailedAttemps();
            credentialsRepository.save(credentials);
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new InvalidOperationException("Invalid credentials");
        }

        // Check if password is expired
        boolean passwordExpired = credentials.isPasswordExpired();

        // Reset failed attempts on successful login
        credentials.resetFailedAttemps();
        credentials.setLastLoginAt(LocalDateTime.now());
        credentialsRepository.save(credentials);

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails, user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, user.getId());

        log.info("User {} logged in successfully", request.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .isFirstLogin(false)
                .requiresPasswordChange(passwordExpired)
                .passwordExpired(passwordExpired)
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse loginWithOtp(OtpLoginRequest request) {
        log.info("OTP login attempt for email: {}", request.getEmail());

        // Find user
        User user = userRepository.findByEmailAndNotDeleted(request.getEmail().toLowerCase())
                .orElseThrow(() -> new InvalidOperationException("Invalid credentials"));

        // Check if user is active
        if (!user.isActive()) {
            throw new InvalidOperationException("Account is not active");
        }

        // Get credentials
        UserCredentials credentials = credentialsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidOperationException("Invalid credentials"));

        // Validate OTP
        if (!otpService.validateOtp(user.getId(), request.getOtp())) {
            log.warn("Invalid OTP for email: {}", request.getEmail());
            throw new InvalidOperationException("Invalid or expired OTP");
        }

        // Invalidate OTP after successful validation
        otpService.invalidateOtp(user.getId());

        // Update last login
        credentials.setLastLoginAt(LocalDateTime.now());
        credentialsRepository.save(credentials);

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails, user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, user.getId());

        log.info("User {} logged in with OTP successfully", request.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .isFirstLogin(true)
                .requiresPasswordChange(true)
                .passwordExpired(false)
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse changePasswordFirstLogin(UUID userId, PasswordChangeRequest request) {
        log.info("First login password change for user: {}", userId);

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }

        // Get user
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get credentials
        UserCredentials credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User credentials not found"));

        // Check if this is indeed first login
        if (!credentials.isFirstLogin()) {
            throw new InvalidOperationException("This is not a first login");
        }

        // Hash and set new password
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        credentials.setPasswordHash(hashedPassword);
        credentials.setPasswordCreatedAt(LocalDateTime.now());
        credentials.setPasswordExpiresAt(LocalDateTime.now().plusDays(passwordExpirationDays));
        credentials.setFirstLogin(false);
        credentials.resetFailedAttemps();

        credentialsRepository.save(credentials);

        // Generate new tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails, user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, user.getId());

        log.info("Password changed successfully for first login user: {}", userId);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .isFirstLogin(false)
                .requiresPasswordChange(false)
                .passwordExpired(false)
                .build();
    }

    @Override
    public void changePassword(UUID userId, String currentPassword, PasswordChangeRequest request) {

        log.info("Password change for user: {}", userId);

        //validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new InvalidOperationException("Password do not match");
        }

        //Get credentials
        UserCredentials userCredentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User credentials not found"));

        //validation of current password

        if (!passwordEncoder.matches(currentPassword, userCredentials.getPasswordHash())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        //check if new password is same as old
        if (passwordEncoder.matches(request.getNewPassword(), userCredentials.getPasswordHash())){
            throw new InvalidOperationException("New password nust be different from current password");
        }

        //Hash and set new password

        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        userCredentials.setPasswordHash(hashedPassword);
        userCredentials.setPasswordCreatedAt(LocalDateTime.now());
        userCredentials.setPasswordExpiresAt(LocalDateTime.now().plusDays(passwordExpirationDays));

        credentialsRepository.save(userCredentials);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {

        log.info("Password reset requested for email: {}", request.getEmail());

        //find user (don't reveal if user exists for security
        User user = userRepository.findByEmailAndNotDeleted(request.getEmail().toLowerCase())
                .orElse(null);

        if (user == null){
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        //Get creds
        UserCredentials credentials = credentialsRepository.findByUserId(user.getId())
                .orElse(null);

        if (credentials == null){
            log.warn("No credentials found for user: {}", user.getId());
            return;
        }

        //Generate reset token
        String resetToken = UUID.randomUUID().toString();
        credentials.setPasswordResetToken(resetToken);
        credentials.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));

        credentialsRepository.save(credentials);

        emailService.sendPasswordResetEmail(user.getEmail(),user.getFullName(), resetToken);

        log.info("Password reset email sent to: {}", request.getEmail());

    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {

        log.info("Password reset confirmation with token");

        //validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new InvalidOperationException("Password do not match");
        }

        // Find credentials by valid token
        UserCredentials credentials = credentialsRepository.findByValidPasswordResetToken(request.getToken())
                .orElseThrow(()-> new InvalidOperationException("Invalid or expired reset token"));

        //Hash and set new password
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        credentials.setPasswordHash(hashedPassword);
        credentials.setPasswordCreatedAt(LocalDateTime.now());
        credentials.setPasswordExpiresAt(LocalDateTime.now().plusDays(passwordExpirationDays));
        credentials.setPasswordResetToken(null);
        credentials.setPasswordResetTokenExpiresAt(null);
        credentials.resetFailedAttemps();

        credentialsRepository.save(credentials);

        log.info("Password reset successfully for user: {}", credentials.getUserId());
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshToken request) {
        log.info("Token refresh requested");

        try {
            String refreshToken = request.getRefreshToken();

            if (!jwtTokenProvider.isRefreshToken(refreshToken)){
                throw new InvalidOperationException("Invalid refresh token");
            }

            if (jwtTokenProvider.isTokenExpired(refreshToken)){
                throw new InvalidOperationException("Refresh token has expired");
            }

            String email = jwtTokenProvider.extractUsername(refreshToken);
            UUID userId = jwtTokenProvider.extractUserId(refreshToken);

            User user = userRepository.findByIdAndNotDeleted(userId)
                    .orElseThrow(()-> new InvalidOperationException("User not found"));

            if (!user.isActive()){
                throw new InvalidOperationException("User account is not active.");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            String newAccessToken = jwtTokenProvider.generateToken(userDetails, userId);

            log.info("Token refreshed successfully for user: {}", email);

            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullname(user.getFullName())
                    .roles(user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toSet()))
                    .build();
        } catch (Exception e){
            log.error("Token refresh failed", e);
            throw new InvalidOperationException("Invalid refresh token");
        }
    }

    @Override
    public void logout(UUID userId) {

    }
}
