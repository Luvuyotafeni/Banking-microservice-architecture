package com.banking.usermanagementservice.controller;

import com.banking.usermanagementservice.dto.ApiResponse;
import com.banking.usermanagementservice.dto.AuthenticationResponse;
import com.banking.usermanagementservice.dto.LoginRequest;
import com.banking.usermanagementservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
