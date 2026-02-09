package com.banking.usermanagementservice.service;

import com.banking.usermanagementservice.dto.BeneficiaryResponse;
import com.banking.usermanagementservice.dto.CreateBeneficiaryRequest;
import com.banking.usermanagementservice.dto.UpdateBeneficiaryRequest;

import java.util.List;
import java.util.UUID;

public interface BeneficiariesService {

    BeneficiaryResponse createBeneficiaryForUser(UUID userId, CreateBeneficiaryRequest request);

    BeneficiaryResponse getBeneficiaryById(UUID beneficiaryId);

    List<BeneficiaryResponse> getAllBeneficiaries();

    List<BeneficiaryResponse> getAllActiveBeneficiaries();

    List<BeneficiaryResponse> getBeneficiariesByUserId(UUID userId);

    List<BeneficiaryResponse> getActiveBeneficiariesByUserId(UUID userId);

    BeneficiaryResponse updateBeneficiary(UUID beneficiaryId, UpdateBeneficiaryRequest request);

    void deleteBeneficiary(UUID beneficiaryId);

    void hardDeleteBeneficiary(UUID beneficiaryId);

    void addBeneficiaryToUser(UUID userId, UUID beneficiaryId);

    void removeBeneficiaryFromUser(UUID userId, UUID beneficiaryId);

    boolean isBeneficiaryAssociatedWithUser(UUID userId, UUID beneficiaryId);

    void verifyBeneficiaryOwnership(UUID userId, UUID beneficiaryId);

    UUID getUserIdByEmail(String email);


}
