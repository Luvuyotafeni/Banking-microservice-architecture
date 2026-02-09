package com.banking.usermanagementservice.repository;

import com.banking.usermanagementservice.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, UUID> {

    Optional<UserCredentials> findByUserId(UUID userId);

    @Query("SELECT uc FROM UserCredentials uc WHERE uc.passwordResetToken = :token AND uc.passwordResetTokenExpiresAt > CURRENT_TIMESTAMP")
    Optional<UserCredentials> findByValidPasswordResetToken(@Param("token") String token);

    boolean existsByUserId(UUID userId);
}
