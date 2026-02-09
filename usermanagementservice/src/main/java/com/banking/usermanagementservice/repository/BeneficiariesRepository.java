package com.banking.usermanagementservice.repository;

import com.banking.usermanagementservice.entity.Beneficiaries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiariesRepository extends JpaRepository<Beneficiaries, UUID> {

    Optional<Beneficiaries> findByAccountId(String accountId);

    List<Beneficiaries> findByIsActive(boolean isActive);

    @Query("SELECT b FROM Beneficiaries b JOIN b.users u WHERE u.id = :userId AND b.isActive = true")
    List<Beneficiaries> findActiveByUserId(@Param("userOd") UUID userId);

    @Query("SELECT b FROM Beneficiaries b JOIN b.users u WHERE u.id = :userId")
    List<Beneficiaries> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Beneficiaries b JOIN b.users u WHERE b.id = :beneficiaryId AND u.id = :userId")
    boolean existsByIdAndUserId(@Param("beneficiaryId") UUID beneficiaryId, @Param("userId") UUID userId);

    boolean existsByAccountId(String accountId);

}
