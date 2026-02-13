package com.banking.usermanagementservice.controller;

import com.banking.usermanagementservice.dto.ApiResponse;
import com.banking.usermanagementservice.dto.UserRegistrationRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationController {


    private final UserRegistrationService userRegistrationService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser (
            @Valid @RequestBody UserRegistrationRequest request
            ) {
        log.info("Received registration request for email: {}", request.getEmail());

        UserResponse response = userRegistrationService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Registration successful. Your account is pending for approval."
                ));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email){
        log.info("Checking if email exists: {}", email);

        boolean exists = userRegistrationService.emailExists(email);

        return ResponseEntity.ok(
                ApiResponse.success(exists, "Email availability checked")
        );
    }

    @GetMapping("/check-id/{idNumber}")
    public ResponseEntity<ApiResponse<Boolean>> checkIdNumberExists(@PathVariable String idNumber){
        log.info("Checking if Id number exists");

        boolean exists = userRegistrationService.idNumberExists(idNumber);
        return ResponseEntity.ok(
                ApiResponse.success(exists, "Id number availability checked")
        );
    }
}
