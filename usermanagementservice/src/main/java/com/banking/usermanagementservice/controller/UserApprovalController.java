package com.banking.usermanagementservice.controller;

import com.banking.usermanagementservice.dto.ApiResponse;
import com.banking.usermanagementservice.dto.UserApprovalRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.service.UserApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/approvals")
@RequiredArgsConstructor
@Slf4j
public class UserApprovalController {

    private final UserApprovalService userApprovalService;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<UserResponse>> processApproval(
            @Valid @RequestBody UserApprovalRequest request,
            @RequestAttribute("userId")UUID adminUserId
            ) {
        log.info("Admin {}  processing approval for user {}", adminUserId, request.getUserId());

        UserResponse response = userApprovalService.processApproval(request, adminUserId);

        String message = request.getApproved()
                ? "User approved successfully. Approval email sent with OTP"
                : "User registration rejected. Notification email sent.";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPendingApprovals(){
        log.info("Fetching all pending approvals");

        List<UserResponse> pendingUsers = userApprovalService.getPendingApprovals();

        return ResponseEntity.ok(
                ApiResponse.success(
                        pendingUsers,
                        "Retrieved "+ pendingUsers.size() + "pending approvals"
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserForApproval(@PathVariable UUID userId) {
        log.info("Fetching user {} for approval review", userId);

        UserResponse user = userApprovalService.getUserForApproval(userId);

        return ResponseEntity.ok(
                ApiResponse.success(user, "User details retrieved successfully")
        );
    }
}
