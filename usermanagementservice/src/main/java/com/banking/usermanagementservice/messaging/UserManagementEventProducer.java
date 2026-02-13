package com.banking.usermanagementservice.messaging;

import com.banking.usermanagementservice.config.KafkaTopicConfig;
import com.banking.usermanagementservice.dto.BeneficiaryValidationResponse;
import com.banking.usermanagementservice.dto.UserValidationResponse;
import com.banking.usermanagementservice.dto.event.BeneficiaryAddedEvent;
import com.banking.usermanagementservice.dto.event.UserCreatedEvent;
import com.banking.usermanagementservice.dto.event.UserLoginEvent;
import com.banking.usermanagementservice.dto.event.UserSuspendedEvent;
import com.banking.usermanagementservice.entity.Beneficiaries;
import com.banking.usermanagementservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    /**
     * Publish event when user logs in
     */
    public void publishUserLoginEvent(UUID userId, String email, String fullName,
                                      Set<String> roles, boolean isFirstLogin) {
        UserLoginEvent event = UserLoginEvent.builder()
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .loginTime(LocalDateTime.now())
                .isFirstLogin(isFirstLogin)
                .build();

        log.info("Publishing user login event for user: {}", userId);
        kafkaTemplate.send(
                KafkaTopicConfig.USER_LOGIN_EVENT,
                userId.toString(),
                event
        );
    }

    /**
     * Publish event when user is suspended or unsuspended
     */
    public void publishUserSuspendedEvent(User user, boolean suspended, String reason, UUID suspendedBy) {
        UserSuspendedEvent event = UserSuspendedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .suspended(suspended)
                .reason(reason)
                .suspendedBy(suspendedBy)
                .suspendedAt(LocalDateTime.now())
                .build();

        log.info("Publishing user {} event for user: {}",
                suspended ? "suspended" : "unsuspended", user.getId());
        kafkaTemplate.send(
                KafkaTopicConfig.USER_SUSPENDED_EVENT,
                user.getId().toString(),
                event
        );
    }

    /**
     * Send user validation response back to Payment Service
     */
    public void sendUserValidationResponse(UserValidationResponse response) {
        log.info("Sending user validation response for requestId: {}, valid: {}",
                response.getRequestId(), response.isValid());

        kafkaTemplate.send(
                KafkaTopicConfig.USER_VALIDATION_RESPONSE,
                response.getRequestId(),
                response
        );
    }

    /**
     * Send beneficiary validation response back to Payment Service
     */
    public void sendBeneficiaryValidationResponse(BeneficiaryValidationResponse response) {
        log.info("Sending beneficiary validation response for requestId: {}, valid: {}",
                response.getRequestId(), response.isValid());

        kafkaTemplate.send(
                KafkaTopicConfig.BENEFICIARY_VALIDATION_RESPONSE,
                response.getRequestId(),
                response
        );
    }

    /**
     * Publish event when a new user is created
     */
    public void publishUserCreatedEvent(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRoles().toString())
                .createdAt(user.getCreatedAt())
                .build();

        log.info("Publishing user created event for user: {}", user.getId());
        kafkaTemplate.send(
                KafkaTopicConfig.USER_CREATED_EVENT,
                user.getId().toString(),
                event
        );
    }

    /**
     * Publish event when a user is updated
     */
    public void publishUserUpdatedEvent(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRoles().toString())
                .createdAt(user.getUpdatedAt())
                .build();

        log.info("Publishing user updated event for user: {}", user.getId());
        kafkaTemplate.send(
                KafkaTopicConfig.USER_UPDATED_EVENT,
                user.getId().toString(),
                event
        );
    }

    /**
     * Publish event when a beneficiary is added to a user
     */
    public void publishBeneficiaryAddedEvent(UUID userId, Beneficiaries beneficiary) {
        BeneficiaryAddedEvent event = BeneficiaryAddedEvent.builder()
                .beneficiaryId(beneficiary.getId())
                .userId(userId)
                .accountId(beneficiary.getAccountId())
                .nickname(beneficiary.getNickname())
                .addedAt(LocalDateTime.now())
                .build();

        log.info("Publishing beneficiary added event for user: {} and beneficiary: {}",
                userId, beneficiary.getId());
        kafkaTemplate.send(
                KafkaTopicConfig.BENEFICIARY_ADDED_EVENT,
                userId.toString(),
                event
        );
    }

    /**
     * Publish event when a beneficiary is removed from a user
     */
    public void publishBeneficiaryRemovedEvent(UUID userId, UUID beneficiaryId) {
        BeneficiaryAddedEvent event = BeneficiaryAddedEvent.builder()
                .beneficiaryId(beneficiaryId)
                .userId(userId)
                .addedAt(LocalDateTime.now())
                .build();

        log.info("Publishing beneficiary removed event for user: {} and beneficiary: {}",
                userId, beneficiaryId);
        kafkaTemplate.send(
                KafkaTopicConfig.BENEFICIARY_REMOVED_EVENT,
                userId.toString(),
                event
        );
    }
}
