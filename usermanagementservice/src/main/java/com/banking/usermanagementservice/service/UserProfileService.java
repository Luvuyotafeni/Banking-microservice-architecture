package com.banking.usermanagementservice.service;

import com.banking.usermanagementservice.dto.UserProfileUpdateRequest;
import com.banking.usermanagementservice.dto.UserResponse;

import java.util.UUID;

public interface UserProfileService {

    UserResponse getUserProfole(UUID userId);

    UserResponse updateUserProfile(UUID userId, UserProfileUpdateRequest request);
}
