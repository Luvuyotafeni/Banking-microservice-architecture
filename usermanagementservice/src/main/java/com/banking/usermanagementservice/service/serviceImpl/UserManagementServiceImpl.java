package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.dto.UserSuspensionRequest;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.enums.ApprovalStatus;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.mapper.UserMapper;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse suspendUser(UserSuspensionRequest request, UUID adminId) {
        log.info("Processing suspension request for user: {} by admin: {}", request.getUserId(), adminId
        );

        User user = userRepository.findByIdAndNotDeleted(request.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if (request.getSuspend()){
            user.setSuspended(true);
            user.setSuspensionReason(request.getReason());
            user.setSuspendedAt(LocalDateTime.now());
            user.setActive(false);

            log.info("User {} suspended by admin: {}", user.getId(), adminId);

        } else {
            user.setSuspended(false);
            user.setSuspensionReason(null);
            user.setSuspendedAt(null);
            user.setActive(true);

            log.info("user {} unsuspended by admin: {}", user.getId(), adminId);
        }

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);


    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        log.info("fetching all active users");

        List<User> activeUser = userRepository.findAll().stream()
                .filter(user -> !user.isDeleted() && user.isActive() && !user.isSuspended())
                .collect(Collectors.toList());

        return activeUser.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllSuspendedUsers() {
        log.info("Fetching all suspended users");

        List<User> suspendedUsers = userRepository.findAll().stream()
                .filter(user -> !user.isDeleted() && user.isSuspended())
                .collect(Collectors.toList());

        return suspendedUsers.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId, UUID adminId) {

        log.info("Permanently deleting user: {} by admin {}", userId, adminId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);

        userRepository.save(user);
        log.info("User {} soft deleted by admin: {}", userId, adminId);
    }
}
