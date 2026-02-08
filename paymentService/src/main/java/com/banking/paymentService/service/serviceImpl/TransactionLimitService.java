package com.banking.paymentService.service.serviceImpl;


import com.banking.paymentService.enums.TransactionStatus;
import com.banking.paymentService.enums.TransactionType;
import com.banking.paymentService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLimitService {

    private final TransactionRepository transactionRepository;

    public Long getTransactionCountSince(UUID userId, LocalDateTime since){
        return transactionRepository.countByUserIdAndStatusANdDateAfter(
                userId,
                TransactionStatus.COMPLETED,
                since
        );
    }

    public BigDecimal getTotalAmountSince(UUID userId, TransactionType type, LocalDateTime since){
        BigDecimal total = transactionRepository.sumCompletedAmountByUserIdAndTypeAndDateAfter(
                userId, type, since
        );
        return total != null ? total : BigDecimal.ZERO;
    }
}
