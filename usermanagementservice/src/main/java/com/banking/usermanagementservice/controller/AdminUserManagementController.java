package com.banking.usermanagementservice.controller;


import com.banking.usermanagementservice.dto.ApiResponse;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.dto.UserSuspensionRequest;
import com.banking.usermanagementservice.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementController {

    private final UserManagementService userManagementService;

    @PostMapping("/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> suspendUser(
            @Valid @RequestBody UserSuspensionRequest request,
            @RequestAttribute("userId")UUID adminId
            ){
        log.info("Admin {} processing for user {}", adminId, request.getUserId());

        UserResponse response = userManagementService.suspendUser(request, adminId);

        String message = request.getSuspend()
                ? "User suspended successfully"
                : "User unsuspended successfully";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllActiveUsers(){
        log.info("Fetching all active users");

        List<UserResponse> user = userManagementService.getAllActiveUsers();

        return ResponseEntity.ok(
                ApiResponse.success(user, user.size() + "active user found")
        );
    }

    @GetMapping("/suspended")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllSuspendedUsers(){
        log.info("Fetching all suspended users");

        List<UserResponse> users = userManagementService.getAllSuspendedUsers();

        return ResponseEntity.ok(
                ApiResponse.success(users, users.size() + "suspended users found")
        );

    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID userId,
            @RequestAttribute("userId") UUID adminId
    ){
        log.info("Admin {} deleting user {}", adminId, userId);

        userManagementService.deleteUser(userId, adminId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
