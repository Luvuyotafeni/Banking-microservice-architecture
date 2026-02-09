package com.banking.usermanagementservice.controller;

import com.banking.usermanagementservice.dto.ApiResponse;
import com.banking.usermanagementservice.dto.UserProfileUpdateRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @RequestAttribute("userId")UUID userId
            ){
        log.info("Fetching profile for user: {}", userId);

        UserResponse response = userProfileService.getUserProfole(userId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Profile retrieved successfully")
        );
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody UserProfileUpdateRequest request
            ){
        log.info("Updating profile for user: {}", userId);

        UserResponse response = userProfileService.updateUserProfile(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Profile updated Successfully")
        );
    }
}

