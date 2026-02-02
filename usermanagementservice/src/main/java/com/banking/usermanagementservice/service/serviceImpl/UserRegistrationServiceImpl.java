package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.UserRegistrationRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.service.UserRegistrationService;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {
    @Override
    public UserResponse registerUser(UserRegistrationRequest request) {
        return null;
    }

    @Override
    public boolean emailExists(String email) {
        return false;
    }

    @Override
    public boolean idNumberExists(String idNumber) {
        return false;
    }
}
