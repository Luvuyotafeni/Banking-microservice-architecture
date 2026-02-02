package com.banking.usermanagementservice.service;

import com.banking.usermanagementservice.dto.UserApprovalRequest;
import com.banking.usermanagementservice.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserApprovalService {

    /**
     * Approve or reject a pending user registration
     * @param request Approval request with decision
     * @param approvedBy UUID of the admin approving
     * @return UserResponse with updated user information
     */
    UserResponse processApproval(UserApprovalRequest request, UUID approvedBy);

    /**
     * Get all pending user registrations
     * @return List of users pending approval
     */
    List<UserResponse> getPendingApprovals();

    /**
     * Get user by ID for approval review
     * @param userId User ID
     * @return UserResponse
     */
    UserResponse getUserForApproval(UUID userId);
}
