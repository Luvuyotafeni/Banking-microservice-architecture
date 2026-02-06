package com.banking.usermanagementservice.controller;

import com.banking.usermanagementservice.dto.BeneficiaryResponse;
import com.banking.usermanagementservice.dto.CreateBeneficiaryRequest;
import com.banking.usermanagementservice.service.BeneficiariesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/beneficiaries")
@RequiredArgsConstructor
@Slf4j
public class BeneficiariesController {

    private final BeneficiariesService beneficiariesService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BeneficiaryResponse>> getAllBeneficiaries(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ){
        log.info("Rest request to get all the beneficiaries activeOnly: {}",activeOnly);
        List<BeneficiaryResponse> responses = activeOnly
                ? beneficiariesService.getAllActiveBeneficiaries()
                : beneficiariesService.getAllBeneficiaries();

        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{beneficiaryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BeneficiaryResponse>getBeneficiary(
            @PathVariable UUID beneficiaryId
            ){
        log.info("Rqust to get beneficiary: {}", beneficiaryId);
        BeneficiaryResponse response = beneficiariesService.getBeneficiaryById(beneficiaryId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{beneficiaryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BeneficiaryResponse> deleteBeneficiary(
            @PathVariable UUID beneficiaryId,
            @RequestParam(required = false, defaultValue = "false") boolean hard
    ){
        log.info("Request to delete beneficiary: {}, hard delete: {}", beneficiaryId, hard);

        if (hard){
            beneficiariesService.hardDeleteBeneficiary(beneficiaryId);
        } else {
            beneficiariesService.deleteBeneficiary(beneficiaryId);
        }

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BeneficiaryResponse>> getUserBeneficiaries(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ){
        log.info("Request to get beneficiaries for user: {}, activeOnly: {}", userId, activeOnly);

        List<BeneficiaryResponse> responses = activeOnly
                ? beneficiariesService.getActiveBeneficiariesByUserId(userId)
                : beneficiariesService.getBeneficiariesByUserId(userId);

        return ResponseEntity.ok(responses);
    }

//user controllers(will be removed from this comntroller though
    @PostMapping("/my-beneficiaries")
    public ResponseEntity<BeneficiaryResponse> createBeneficiaryForCurrentUser(
            @Valid @RequestBody CreateBeneficiaryRequest request
            ){
        UUID currentUserId = getCurrentUserId();

        log.info("Request from {} to create beneficiary", currentUserId);

        BeneficiaryResponse  response = beneficiariesService.createBeneficiaryForUser(currentUserId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my-beneficiaries")
    public ResponseEntity<List<BeneficiaryResponse>> getMyBeneficiaries(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ){
        UUID currentUserId = getCurrentUserId();
        log.info("Request from user {} to get their beneficiaies, actievOnly: {}", currentUserId, activeOnly );

        List<BeneficiaryResponse> responses = activeOnly
                ? beneficiariesService.getActiveBeneficiariesByUserId(currentUserId)
                :beneficiariesService.getBeneficiariesByUserId(currentUserId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-beneficiaries/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> getMyBeneficiary(
            @PathVariable UUID beneficiaryId
    ){
        UUID currentUserId = getCurrentUserId();

        log.info("Request from User {} to get beneficiary {}", currentUserId, beneficiaryId);

        beneficiariesService.verifyBeneficiaryOwnership(currentUserId, beneficiaryId);

        BeneficiaryResponse response = beneficiariesService.getBeneficiaryById(beneficiaryId);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/my-beneficiaries/{beneficiaryId}")
    public ResponseEntity<Void> removeMyBeneficiary(@PathVariable UUID beneficiaryId){
        UUID currentUserId = getCurrentUserId();

        log.info("Request from user {} to remove benefici: {}", currentUserId, beneficiaryId );

        beneficiariesService.removeBeneficiaryFromUser(currentUserId, beneficiaryId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/my-beneficiaries/add0-existing/{beneificiaryId}")
    public ResponseEntity<Void> addExistingBeneficiaryToMe(@PathVariable UUID beneficiaryId){

        UUID currentUserId = getCurrentUserId();

        log.info("Request from user {} to add existing beneficiary: {} to their beneficiaries", currentUserId, beneficiaryId);

        beneficiariesService.addBeneficiaryToUser(currentUserId, beneficiaryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("my-beneficiaries/check/{beneficiaryId}")
    public ResponseEntity<Boolean> checkMyBeneficiaryAssociation(@PathVariable UUID beneficiaryId){
        UUID currentUserId =getCurrentUserId();

        log.info("Request from user {} to check associateion with beneficiary: {}", currentUserId, beneficiaryId);

        boolean isAssociated = beneficiariesService.isBeneficiaryAssociatedWithUser(
                currentUserId, beneficiaryId
        );
        return ResponseEntity.ok(isAssociated);
    }



    //helper method to get current user id
    private UUID getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return beneficiariesService.getUserIdByEmail(email);
    }
}
