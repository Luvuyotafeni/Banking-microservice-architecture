package com.banking.paymentService.config;

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

    //Topic names
    public static final String BALANCE_CHECK_REQUEST = "balance-check-request";
    public static final String BALANCE_CHECK_RESPONSE = "balance-check-balance";
    public static final String DEBIT_ACCOUNT_REQUEST = "debit-account-request";
    public static final String CREDIT_ACCOUNT_REQUEST = "credit-account-request";
    public static final String ACCOUNT_OPERATION_RESPONSE = "account-operation-response";
    public static final String TRANSACTION_NOTIFICATION = "transaction-notification";
    public static final String USER_VALIDATION_REQUEST = "user-validation-request";
    public static final String USER_VALIDATION_RESPONSE = "user-validation-response";
    public static final String BENEFICIARY_VALIDATION_REQUEST = "beneficiary-validation-request";
    public static final String BENEFICIARY_VALIDATION_RESPONSE = "beneficiary-validation-response";

    @Bean
    public KafkaAdmin kafkaAdmin(){
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic balanceCheckRequestTopic(){
        return new NewTopic(BALANCE_CHECK_REQUEST, 3, (short) 1);
    }

    @Bean
    public NewTopic balanceCheckResponseTopic(){
        return new NewTopic(BALANCE_CHECK_RESPONSE, 3, (short) 1);
    }

    @Bean
    public NewTopic creditAccountRequestTopic(){
        return new NewTopic(CREDIT_ACCOUNT_REQUEST,3, (short) 1);
    }

    @Bean
    public NewTopic accountOperationResponseTopic(){
        return new NewTopic(ACCOUNT_OPERATION_RESPONSE, 3, (short) 1);
    }

    @Bean
    public NewTopic transactionNotificationTopic(){
        return new NewTopic(TRANSACTION_NOTIFICATION, 3, (short) 1);
    }

    @Bean
    public NewTopic userValidationRequestTopic(){
        return new NewTopic(USER_VALIDATION_REQUEST, 3, (short) 1);
    }

    @Bean
    public NewTopic userValidationResponseTopic(){
        return new NewTopic(USER_VALIDATION_RESPONSE, 3, (short) 1);
    }

    @Bean
    public NewTopic beneficiaryValidationRequestTopics(){
        return new NewTopic(BENEFICIARY_VALIDATION_REQUEST, 3, (short) 1);
    }

    @Bean
    public NewTopic beneficiaryValidationResponseTopic(){
        return new NewTopic(BENEFICIARY_VALIDATION_RESPONSE, 3, (short) 1);
    }

}
