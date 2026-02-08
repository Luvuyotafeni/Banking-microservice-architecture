package com.banking.paymentService.Entity;


import com.banking.paymentService.enums.TransactionStatus;
import com.banking.paymentService.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_account_id", columnList = "accountId"),
        @Index(name = "idx_transaction_date", columnList = "transactionDate"),
        @Index(name = "idx_status", columnList = "status"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String accountId;

    @Column
    private UUID beneficiaryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "ZAR";

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(length = 500)
    private String description;

    @Column(length = 100, unique = true)
    private String reference;

    @Column(length = 100)
    private String deviceType;

    @Column(length = 50)
    private String deviceId;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String location;

    @Column(precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column
    private String destinationAccountId;

    @Column(length = 100)
    private String  failureReason;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime failedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        if (transactionDate == null){
            transactionDate = LocalDateTime.now();
        }
        if (reference == null){
            reference = generateReference();
        }

        if (currency == null){
            currency = "ZAR";
        }

        calculateTotalAmount();
    }

    @PreUpdate
    public void preUpdate(){
        calculateTotalAmount();
    }

    private void calculateTotalAmount(){
        BigDecimal feeAmount = fee != null ? fee : BigDecimal.ZERO;
        totalAmount = amount.add(feeAmount);
    }

    private String generateReference(){
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
    }
}
