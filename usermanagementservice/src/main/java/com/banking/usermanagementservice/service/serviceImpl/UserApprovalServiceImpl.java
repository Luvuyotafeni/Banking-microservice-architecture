package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.UserApprovalRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.mapper.UserMapper;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.UserApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApprovalServiceImpl implements UserApprovalService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse processApproval(UserApprovalRequest request, UUID approvedBy) {
        return null;
    }

    @Override
    public List<UserResponse> getPendingApprovals() {
        return List.of();
    }

    @Override
    public UserResponse getUserForApproval(UUID userId) {
        return null;
    }
}
