package com.banking.paymentService.repository;

import com.banking.paymentService.Entity.Transaction;
import com.banking.paymentService.enums.TransactionStatus;
import com.banking.paymentService.enums.TransactionType;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    List<Transaction> findByUserIdAndStatus(UUID userId, TransactionStatus status);

    Page<Transaction> findByAccountId(String accountId, Pageable pageable);

    Optional<Transaction> findByReference(String reference);

    Page<Transaction> findByUserIdAndType(UUID userId, TransactionType type, Pageable pageable);

    // FIX 1: Changed == to = and added space before ORDER BY
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // FIX 2: Fixed typo finBy -> findBy
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    List<Transaction> findByStatusAndCreatedAtBefore(
            TransactionStatus status,
            LocalDateTime dateTime
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId " +
            "AND t.status = :status AND t.transactionDate >= :startDate")
    Long countByUserIdAndStatusANdDateAfter(
            @Param("userId") UUID userId,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId "+
            "AND t.status = 'COMPLETED' AND t.type = :type "+
            "AND t.transactionDate >= :startDate")
    BigDecimal sumCompletedAmountByUserIdAndTypeAndDateAfter(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate
    );

    // FIX 3: Removed the extra '(' before LOWER and ensured spaces
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND " +
            "(LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Transaction> searchByUserId(
            @Param("userId") UUID userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    // FIX 4: Added alias 't' after 'Transaction'
    @Query("SELECT t FROM Transaction t WHERE t.status IN ('PENDING', 'PROCESSING') " +
            "AND t.createdAt < :threshold")
    List<Transaction> findStaleTransactions(@Param("threshold") LocalDateTime threshold);
}
