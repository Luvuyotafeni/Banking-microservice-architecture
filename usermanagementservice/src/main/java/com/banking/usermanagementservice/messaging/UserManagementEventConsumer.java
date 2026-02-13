package com.banking.usermanagementservice.messaging;

import com.banking.usermanagementservice.config.KafkaTopicConfig;
import com.banking.usermanagementservice.dto.BeneficiaryValidationRequest;
import com.banking.usermanagementservice.dto.BeneficiaryValidationResponse;
import com.banking.usermanagementservice.dto.UserValidationRequest;
import com.banking.usermanagementservice.dto.UserValidationResponse;
import com.banking.usermanagementservice.entity.Beneficiaries;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.repository.BeneficiariesRepository;
import com.banking.usermanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementEventConsumer {

    private final UserRepository userRepository;
    private final BeneficiariesRepository beneficiariesRepository;
    private final UserManagementEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    /**
     * Listen for user validation requests from Payment Service
     */
    @KafkaListener(topics = KafkaTopicConfig.USER_VALIDATION_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserValidationRequest(String message) {
        try {
            UserValidationRequest request = objectMapper.readValue(
                    message, UserValidationRequest.class);

            log.info("Received user validation request for userId: {} with requestId: {}",
                    request.getUserId(), request.getRequestId());

            // Validate user
            Optional<User> userOpt = userRepository.findById(request.getUserId());

            UserValidationResponse response;
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response = UserValidationResponse.builder()
                        .userId(user.getId())
                        .requestId(request.getRequestId())
                        .valid(true)
                        .active(user.isActive())
                        .email(user.getEmail())
                        .message("User found and validated")
                        .build();

                log.info("User {} is valid and active: {}", user.getId(), user.isActive());
            } else {
                response = UserValidationResponse.builder()
                        .userId(request.getUserId())
                        .requestId(request.getRequestId())
                        .valid(false)
                        .active(false)
                        .message("User not found")
                        .build();

                log.warn("User {} not found", request.getUserId());
            }

            // Send response back
            eventProducer.sendUserValidationResponse(response);

        } catch (Exception e) {
            log.error("Error processing user validation request: ", e);
        }
    }

    /**
     * Listen for beneficiary validation requests from Payment Service
     */
    @KafkaListener(topics = KafkaTopicConfig.BENEFICIARY_VALIDATION_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBeneficiaryValidationRequest(String message) {
        try {
            BeneficiaryValidationRequest request = objectMapper.readValue(
                    message, BeneficiaryValidationRequest.class);

            log.info("Received beneficiary validation request for beneficiaryId: {} and userId: {} with requestId: {}",
                    request.getBeneficiaryId(), request.getUserId(), request.getRequestId());

            // Validate beneficiary
            Optional<Beneficiaries> beneficiaryOpt = beneficiariesRepository
                    .findById(request.getBeneficiaryId());

            BeneficiaryValidationResponse response;
            if (beneficiaryOpt.isPresent()) {
                Beneficiaries beneficiary = beneficiaryOpt.get();

                // Check if beneficiary belongs to user
                boolean belongsToUser = beneficiariesRepository
                        .existsByIdAndUserId(request.getBeneficiaryId(), request.getUserId());

                response = BeneficiaryValidationResponse.builder()
                        .beneficiaryId(beneficiary.getId())
                        .requestId(request.getRequestId())
                        .valid(true)
                        .active(beneficiary.isActive())
                        .belongsToUser(belongsToUser)
                        .accountId(beneficiary.getAccountId())
                        .message(belongsToUser ? "Beneficiary found and belongs to user" :
                                "Beneficiary found but does not belong to user")
                        .build();

                log.info("Beneficiary {} is valid, active: {}, belongs to user: {}",
                        beneficiary.getId(), beneficiary.isActive(), belongsToUser);
            } else {
                response = BeneficiaryValidationResponse.builder()
                        .beneficiaryId(request.getBeneficiaryId())
                        .requestId(request.getRequestId())
                        .valid(false)
                        .active(false)
                        .belongsToUser(false)
                        .message("Beneficiary not found")
                        .build();

                log.warn("Beneficiary {} not found", request.getBeneficiaryId());
            }

            // Send response back
            eventProducer.sendBeneficiaryValidationResponse(response);

        } catch (Exception e) {
            log.error("Error processing beneficiary validation request: ", e);
        }
    }
}
