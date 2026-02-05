package com.banking.usermanagementservice.controller;

import com.banking.usermanagementservice.dto.*;
import com.banking.usermanagementservice.service.AuthenticationService;
import io.jsonwebtoken.security.Password;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * standard log in*/
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        AuthenticationResponse response = authenticationService.login(request);

        String message = response.isPasswordExpired()
                ? "Login successful. Your password has expired. Please change it."
                : "Login successful";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    /**
     * login with otp*/
    @PostMapping("/login/otp")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> loginWithOtp(
            @Valid @RequestBody OtpLoginRequest request
    ) {
        log.info(" Otp Login request received for email: {}", request.getEmail());

        AuthenticationResponse response = authenticationService.loginWithOtp(request);


        return ResponseEntity.ok(ApiResponse.success(response,"OTP validated successfully. Please change your password to continue"));

    }

    /**
     * Change password after first login (requires authentication)*/

    @PostMapping("/password/first-change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> changePasswordFirstLogin(
            @RequestAttribute("userId")UUID userId,
            @Valid @RequestBody PasswordChangeRequest request
            ) {
        log.info("First login password change for user: {}", userId);

        AuthenticationResponse response = authenticationService.changePasswordFirstLogin(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Password changed successfully. You can now use your account."
                )
        );
    }

    /**
     * Change password for existing users (requires authentication)*/
    @PostMapping("/password/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestAttribute("userId") UUID userId,
            @RequestParam String currentPassword,
            @Valid @RequestBody PasswordChangeRequest request
            ){
        log.info("Password change request for user: {}", userId);
        authenticationService.changePassword(userId, currentPassword, request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully.")
        );
    }

    /**
     * Request password reset (public endpoint)*/

    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiResponse<Void>> requestedPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ){
        log.info("Password reset requested for email: {}", request.getEmail());

        authenticationService.requestPasswordReset(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "If the email exists, a password reset link has been sent. Please your inbox"
                )
        );
    }

    /**
     * Confirm password reset with toke (public endpoint)*/
    @PostMapping ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ){
        log.info("Password reset confirmartion with token");

        authenticationService.confirmPasswordReset(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully. you can now log in with your new password.")
        );
    }

    /**
     * Refresh access token using refresh token*/

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshToken request
    ){
        log.info("Token refresh requested");

        AuthenticationResponse response = authenticationService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response, "Token refreshed successfully"
                )
        );
    }

    /**
     * Logout (requires authentication)*/
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestAttribute("userId") UUID userId
    ){
        log.info("logout request for user: {}", userId);

        authenticationService.logout(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully")
        );
    }

}
