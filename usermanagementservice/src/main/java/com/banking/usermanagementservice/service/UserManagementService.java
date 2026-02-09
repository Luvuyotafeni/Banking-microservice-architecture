package com.banking.usermanagementservice.service;

import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.dto.UserSuspensionRequest;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {

    UserResponse suspendUser(UserSuspensionRequest request, UUID adminId);

    List<UserResponse> getAllActiveUsers();


    List<UserResponse> getAllSuspendedUsers();

    void deleteUser(UUID userId, UUID adminId);
}
