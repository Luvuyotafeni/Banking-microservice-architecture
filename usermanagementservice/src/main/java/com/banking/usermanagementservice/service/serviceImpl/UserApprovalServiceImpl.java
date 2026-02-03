package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.UserApprovalRequest;
import com.banking.usermanagementservice.dto.UserResponse;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.enums.ApprovalStatus;
import com.banking.usermanagementservice.exception.InvalidOperationException;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.mapper.UserMapper;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.EmailService;
import com.banking.usermanagementservice.service.OtpService;
import com.banking.usermanagementservice.service.UserApprovalService;
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
public class UserApprovalServiceImpl implements UserApprovalService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final OtpService otpService;

    @Override
    @Transactional
    public UserResponse processApproval(UserApprovalRequest request, UUID approvedBy) {
        log.info("Processing approval for user Id: {}", request.getUserId());

        User user = userRepository.findByIdAndNotDeleted((request.getUserId()))
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        //check if user is in pending status
        if (user.getApprovalStatus() != ApprovalStatus.PENDING){
            throw new InvalidOperationException("User approval status is already: "+ user.getApprovalStatus());
        }

        if (request.getApproved()){
            user.setApprovalStatus(ApprovalStatus.APPROVED);
            user.setApprovedAt(LocalDateTime.now());
            user.setApprovedBy(approvedBy);
            user.isActive();

            //generation of the otp
            String otp = otpService.generateOtp(user.getId());

            //save user
            User approvedUser = userRepository.save(user);

            //send approval email with the otp

            emailService.sendApprovalEmail(
                    approvedUser.getEmail(),
                    approvedUser.getFullName(),
                    otp
            );

            log.info("User approved successfully: {}", user.getId());
            return userMapper.toUserResponse(approvedUser);

        } else {
            user.setApprovalStatus(ApprovalStatus.REJECTED);
            user.setApprovedAt(LocalDateTime.now());
            user.setApprovedBy(approvedBy);

            //save user
            User rejectedUser = userRepository.save(user);

            //send rejection email
            emailService.sendRejectionEmail(
                    rejectedUser.getEmail(),
                    rejectedUser.getFullName(),
                    request.getReason()
            );

            log.info("User rejected: {}", user.getId());
            return userMapper.toUserResponse(rejectedUser);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getPendingApprovals() {
        log.info("Fetching all pending user approvals");

        List<User> pendingUsers = userRepository.findByApprovalStatus(ApprovalStatus.PENDING);

        return pendingUsers.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserForApproval(UUID userId) {
        log.info("Fetching user for approval: {}", userId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        return userMapper.toUserResponse(user);

    }
}
