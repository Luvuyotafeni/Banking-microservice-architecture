package com.banking.usermanagementservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Validation Topics (consumed from Payment Service)
    public static final String USER_VALIDATION_REQUEST = "user-validation-request";
    public static final String USER_VALIDATION_RESPONSE = "user-validation-response";
    public static final String BENEFICIARY_VALIDATION_REQUEST = "beneficiary-validation-request";
    public static final String BENEFICIARY_VALIDATION_RESPONSE = "beneficiary-validation-response";

    // Event Topics (published by User Management)
    public static final String USER_CREATED_EVENT = "user-created-event";
    public static final String USER_UPDATED_EVENT = "user-updated-event";
    public static final String USER_LOGIN_EVENT = "user-login-event";
    public static final String USER_SUSPENDED_EVENT = "user-suspended-event";
    public static final String BENEFICIARY_ADDED_EVENT = "beneficiary-added-event";
    public static final String BENEFICIARY_REMOVED_EVENT = "beneficiary-removed-event";

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // Validation Topics
    @Bean
    public NewTopic userValidationRequestTopic() {
        return new NewTopic(USER_VALIDATION_REQUEST, 3, (short) 1);
    }

    @Bean
    public NewTopic userValidationResponseTopic() {
        return new NewTopic(USER_VALIDATION_RESPONSE, 3, (short) 1);
    }

    @Bean
    public NewTopic beneficiaryValidationRequestTopic() {
        return new NewTopic(BENEFICIARY_VALIDATION_REQUEST, 3, (short) 1);
    }

    @Bean
    public NewTopic beneficiaryValidationResponseTopic() {
        return new NewTopic(BENEFICIARY_VALIDATION_RESPONSE, 3, (short) 1);
    }

    // Event Topics
    @Bean
    public NewTopic userCreatedEventTopic() {
        return new NewTopic(USER_CREATED_EVENT, 3, (short) 1);
    }

    @Bean
    public NewTopic userUpdatedEventTopic() {
        return new NewTopic(USER_UPDATED_EVENT, 3, (short) 1);
    }

    @Bean
    public NewTopic userLoginEventTopic() {
        return new NewTopic(USER_LOGIN_EVENT, 3, (short) 1);
    }

    @Bean
    public NewTopic userSuspendedEventTopic() {
        return new NewTopic(USER_SUSPENDED_EVENT, 3, (short) 1);
    }

    @Bean
    public NewTopic beneficiaryAddedEventTopic() {
        return new NewTopic(BENEFICIARY_ADDED_EVENT, 3, (short) 1);
    }

    @Bean
    public NewTopic beneficiaryRemovedEventTopic() {
        return new NewTopic(BENEFICIARY_REMOVED_EVENT, 3, (short) 1);
    }
}
