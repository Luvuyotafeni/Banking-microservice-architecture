package com.banking.paymentService.message;

import com.banking.paymentService.Entity.Transaction;
import com.banking.paymentService.config.KafkaConfig;
import com.banking.paymentService.config.KafkaTopicConfig;
import com.banking.paymentService.dto.event.TransactionNotification;
import com.banking.paymentService.dto.request.BalanceCheckRequest;
import com.banking.paymentService.dto.request.DebitAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class TransactionEventProducer {

    private final KafkaTemplate<String, Object > kafkaTemplate;

    public void sendBalanceCheckRequest(Transaction transaction){
        BalanceCheckRequest request = BalanceCheckRequest.builder()
                .accountId(transaction.getAccountId())
                .userId(transaction.getUserId())
                .requiredAmount(transaction.getAmount())
                .transactionReference(transaction.getReference())
                .build();
        kafkaTemplate.send(KafkaTopicConfig.BALANCE_CHECK_REQUEST,
                transaction.getReference(), request);
    }

    public void sendDebitAccountRequest(Transaction transaction){
        DebitAccountRequest request = DebitAccountRequest.builder()
                .accountId(transaction.getAccountId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .transactionReference(transaction.getReference())
                .description(transaction.getDescription())
                .build();

        kafkaTemplate.send(KafkaTopicConfig.CREDIT_ACCOUNT_REQUEST, transaction.getReference(), request);


    }

    public void sendTransactionNotification(Transaction transaction){
        TransactionNotification  notification = TransactionNotification.builder()
                .transactionId(transaction.getId())
                .userId(transaction.getUserId())
                .accountId(transaction.getAccountId())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .reference(transaction.getReference())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(KafkaTopicConfig.TRANSACTION_NOTIFICATION, transaction.getUserId().toString(), notification);
    }
}
