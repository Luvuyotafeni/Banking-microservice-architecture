package com.banking.paymentService.message;

import com.banking.paymentService.Entity.Transaction;
import com.banking.paymentService.config.KafkaTopicConfig;
import com.banking.paymentService.dto.event.AccountOperationResponse;
import com.banking.paymentService.dto.response.BalanceCheckResponse;
import com.banking.paymentService.enums.TransactionStatus;
import com.banking.paymentService.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.BALANCE_CHECK_RESPONSE, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBalanceCheckResponse(String message){
        try{
            BalanceCheckResponse response = objectMapper.readValue(message, BalanceCheckResponse.class);

            Optional<Transaction> transactionOpt = transactionRepository.findByReference(response.getTransactionReference());
            if (transactionOpt.isPresent()){
                Transaction transaction = transactionOpt.get();

                if (!response.isSufficientBalance()){
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setFailedAt(LocalDateTime.now());
                    transaction.setFailureReason("Insufficient balance: " + response.getMessage());
                    transactionRepository.save(transaction);
                } else {
                    transaction.setBalanceBefore(response.getCurrentBalance());
                    transactionRepository.save(transaction);

                }
            }
        } catch (Exception e){
            throw new RuntimeException("error processing balance"+ e);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.ACCOUNT_OPERATION_RESPONSE, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAccountOperationResponse(String message){
        try {
            AccountOperationResponse response = objectMapper.readValue(
                    message, AccountOperationResponse.class
            );

            Optional<Transaction> transactionOpt = transactionRepository.findByReference(response.getTransactionReference());

            if (transactionOpt.isPresent()){
                Transaction transaction = transactionOpt.get();

                if (response.isSuccess()){
                    transaction.setBalanceAfter(response.getNewBalance());
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transaction.setCompletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                } else {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setFailedAt(LocalDateTime.now());
                    transaction.setFailureReason("Account operation failed:" + response.getMessage());
                    transactionRepository.save(transaction);
                }
            }
        } catch (Exception e){

            throw new RuntimeException("Error processing amount operation response: ", e);
        }
    }
}
