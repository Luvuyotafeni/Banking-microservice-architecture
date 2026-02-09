package com.banking.usermanagementservice.service.serviceImpl;

import com.banking.usermanagementservice.dto.BeneficiaryResponse;
import com.banking.usermanagementservice.dto.CreateBeneficiaryRequest;
import com.banking.usermanagementservice.dto.UpdateBeneficiaryRequest;
import com.banking.usermanagementservice.entity.Beneficiaries;
import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.exception.BeneficiaryAlreadyExistsException;
import com.banking.usermanagementservice.exception.BeneficiaryNotFoundException;
import com.banking.usermanagementservice.exception.ResourceNotFoundException;
import com.banking.usermanagementservice.repository.BeneficiariesRepository;
import com.banking.usermanagementservice.repository.UserRepository;
import com.banking.usermanagementservice.service.BeneficiariesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryServiceImpl implements BeneficiariesService {

    private final UserRepository userRepository;
    private final BeneficiariesRepository beneficiariesRepository;

    @Override
    public BeneficiaryResponse createBeneficiaryForUser(UUID userId, CreateBeneficiaryRequest request) {
        log.info("Creating new beneficiary for user {} with account id: {}", userId, request.getAccountId());

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with Id:" + userId));

        Beneficiaries existingBeneficiary = beneficiariesRepository.findByAccountId(request.getAccountId())
                .orElse(null);

        if (existingBeneficiary != null){
            if (!user.getBeneficiaries().contains(existingBeneficiary)){
                user.getBeneficiaries().add(existingBeneficiary);
                userRepository.save(user);
                log.info("Existing beneficiary {} associated with user {}", existingBeneficiary.getId(), userId);
            } else {
                throw new BeneficiaryAlreadyExistsException("You already have this beneficiary in you beneficiaries");

            }
            return mapToResponse(existingBeneficiary);
        }

        Beneficiaries beneficiary = Beneficiaries.builder()
                .accountId(request.getAccountId())
                .nickname(request.getNickname())
                .isActive(true)
                .build();

        Beneficiaries savedBeneficiary = beneficiariesRepository.save(beneficiary);

        user.getBeneficiaries().add(savedBeneficiary);
        userRepository.save(user);

        log.info("Beneficiary created and associated with user successfully with Id: {}", savedBeneficiary.getId());
        return mapToResponse(savedBeneficiary);
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiaryById(UUID beneficiaryId) {
        log.info("Fetching beneficiary with Id:{}", beneficiaryId);

        Beneficiaries beneficiary = beneficiariesRepository.findById(beneficiaryId)
                .orElseThrow(()-> new BeneficiaryNotFoundException("Beneficiary not found with beneficixary Id {}" + beneficiaryId));

        return mapToResponse(beneficiary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getAllBeneficiaries() {
        log.info("Fetching all beneficiaries");

        return beneficiariesRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getAllActiveBeneficiaries() {
        log.info("Fetching all active beneficiaries");

        return beneficiariesRepository.findByIsActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiariesByUserId(UUID userId) {
        log.info("Fetching beneficiaris for user Id {}", userId);

        if (!userRepository.existsById(userId)){
            throw new ResourceNotFoundException("User not found with Id: "+ userId);

        }

        return beneficiariesRepository.findAllByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getActiveBeneficiariesByUserId(UUID userId) {
        log.info("Fetching active beneficiaries for user with Id {}", userId);

        if (!userRepository.existsById(userId)){
            throw new ResourceNotFoundException("User with user id " + userId + "not found");
        }

        return beneficiariesRepository.findActiveByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BeneficiaryResponse updateBeneficiary(UUID beneficiaryId, UpdateBeneficiaryRequest request) {
        log.info("updating beneficiary with ID: {}", beneficiaryId);

        Beneficiaries beneficiaries = beneficiariesRepository.findById(beneficiaryId)
                .orElseThrow(()-> new  ResourceNotFoundException("Benefecitary with id:  "+beneficiaryId+ "not found"));

        if (request.getAccountId() != null && !request.getAccountId().isBlank()){
            if (!beneficiaries.getAccountId().equals(request.getAccountId())){
                throw new BeneficiaryAlreadyExistsException("Beneficiary with account Id"+ request.getAccountId()+ "already exists");
            }
            beneficiaries.setAccountId(request.getAccountId());
        }

        if (request.getNickname() != null){
            beneficiaries.setNickname(request.getNickname());
        }

        if (request.getIsActive() != null){
            beneficiaries.setActive(request.getIsActive());
        }

        Beneficiaries updatedBeneficiary = beneficiariesRepository.save(beneficiaries);
        log.info("Beneficiary updated successfully with Id: {}", updatedBeneficiary.getId());

        return mapToResponse(updatedBeneficiary);
    }

    @Override
    public void deleteBeneficiary(UUID beneficiaryId) {

        log.info("Soft deleting beneficiary with ID: {}", beneficiaryId);

        Beneficiaries beneficiary = beneficiariesRepository.findById(beneficiaryId)
                .orElseThrow(()-> new ResourceNotFoundException("Beneficiary with beneficiary Id" + beneficiaryId+"Not found"));

        beneficiary.setActive(false);
        beneficiariesRepository.save(beneficiary);
        log.info("Beneficiary soft deleted successfully with Id: {}", beneficiary);

    }

    @Override
    public void hardDeleteBeneficiary(UUID beneficiaryId) {

        log.info("Hard deleting beneficiary with id: {}", beneficiaryId);

        if (!beneficiariesRepository.existsById(beneficiaryId)){
            throw new ResourceNotFoundException("Beneficiary with Id" + beneficiaryId + "not found");
        }

        beneficiariesRepository.deleteById(beneficiaryId);
        log.info("Beneficiary hard deleted successfully with Id: {}", beneficiaryId);
    }

    @Override
    public void addBeneficiaryToUser(UUID userId, UUID beneficiaryId) {
        log.info("Adding beneficiary {} to user {}", beneficiaryId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User with user Id: "+ userId + "not found"));

        Beneficiaries beneficiaries = beneficiariesRepository.findById(beneficiaryId)
                .orElseThrow(()-> new ResourceNotFoundException("Beneficiary with beneficiary id: {}"+ beneficiaryId + "not found"));

        if (user.getBeneficiaries().contains(beneficiaries)){
            log.warn("Beneficiary with id {} is already associated with user {}", beneficiaries, user);

            throw new BeneficiaryAlreadyExistsException("This beneficiary is already associated with the user");
        }

        user.getBeneficiaries().add(beneficiaries);
        userRepository.save(user);

        log.info("beneficiary {} added to user {}  successfully", beneficiaryId, userId);

    }

    @Override
    public void removeBeneficiaryFromUser(UUID userId, UUID beneficiaryId) {
        log.info("Removing beneficiary {} from user {}", beneficiaryId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User with id {}"+ userId + "not found"));

        Beneficiaries beneficiary = beneficiariesRepository.findById(beneficiaryId)
                .orElseThrow(()-> new ResourceNotFoundException("User with id {}"+ userId + "not found"));

        if (!user.getBeneficiaries().contains(beneficiary)){
            throw new BeneficiaryNotFoundException("The beneficiary is not in your list");
        }

        user.getBeneficiaries().remove(beneficiary);
        userRepository.save(user);
        log.info("Beneficiary {} removed from user {}", beneficiaryId, userId);
    }

    @Override
    public boolean isBeneficiaryAssociatedWithUser(UUID userId, UUID beneficiaryId) {
        log.info("Checking if Beneficiary {} is associayed with user {}", beneficiaryId, userId);

        return beneficiariesRepository.existsByIdAndUserId(beneficiaryId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyBeneficiaryOwnership(UUID userId, UUID beneficiaryId) {

        log.info("Verifying beneficiary {} ownership for user {}", beneficiaryId, userId);

        if (!beneficiariesRepository.existsByIdAndUserId(beneficiaryId, userId)) {
            throw new ResourceNotFoundException("You do not have access to this beneficiary");
        }
    }

    @Override
    public UUID getUserIdByEmail(String email) {
        log.info("Fetching userId for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: "+ email + "not found"));

        return user.getId();
    }


    private BeneficiaryResponse mapToResponse(Beneficiaries beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .accountId(beneficiary.getAccountId())
                .nickname(beneficiary.getNickname())
                .isActive(beneficiary.isActive())
                .createdAt(beneficiary.getCreatedAt())
                .updatedAt(beneficiary.getUpdatedAt())
                .userCount(beneficiary.getUsers() != null ? beneficiary.getUsers().size() : 0)
                .build();
    }
}
