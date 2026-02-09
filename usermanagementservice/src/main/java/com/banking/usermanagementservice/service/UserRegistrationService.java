package com.banking.usermanagementservice.service;

import com.banking.usermanagementservice.dto.UserRegistrationRequest;
import com.banking.usermanagementservice.dto.UserResponse;

public interface UserRegistrationService {
    /**
     * Register a new user (customer) in the system
     * @param request User registration details
     * @return UserResponse with created user information
     */
    UserResponse registerUser(UserRegistrationRequest request);

    /**
     * Check if email already exists
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    boolean emailExists(String email);

    /**
     * Check if ID number already exists
     * @param idNumber ID number to check
     * @return true if exists, false otherwise
     */
    boolean idNumberExists(String idNumber);
}
