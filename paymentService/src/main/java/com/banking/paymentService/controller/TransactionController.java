package com.banking.paymentService.controller;

import com.banking.paymentService.dto.request.CreateTransactionRequest;
import com.banking.paymentService.dto.response.TransactionResponse;
import com.banking.paymentService.dto.response.TransactionSummaryResponse;
import com.banking.paymentService.enums.TransactionType;
import com.banking.paymentService.security.UserPrincipal;
import com.banking.paymentService.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
            ){
        log.info("REST request to create transaction for user: {}", principal.getUserId());
        TransactionResponse response = transactionService.createTransaction(
                request, principal.getUserId()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID transactionId,
            @AuthenticationPrincipal UserPrincipal principal
            ){
        log.info("Request to get transaction: {} for user: {}", transactionId, principal.getUserId());
        TransactionResponse response = transactionService.getTransactionById(
                transactionId, principal.getUserId()
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @PathVariable String reference,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        log.info("Request to get Transaction by reference: {} for user: {}", reference, principal.getUserId());
        TransactionResponse response = transactionService.getTransactionByReference(reference, principal.getUserId());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/my-transactions")
    public ResponseEntity<Page<TransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ){
        log.info("Request to get transaction for user: {}", principal.getUserId());

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TransactionResponse> response = transactionService.getUserTransactions(
                principal.getUserId(), pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-transactions/type/{type}")
    public ResponseEntity<Page<TransactionResponse>> getMyTransactionsByType(
            @PathVariable TransactionType type,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        log.info("Request to get {} transactions for user: {}", type, principal.getUserId());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "TransactionDate"));
        Page<TransactionResponse> responses = transactionService.getUserTransactionByType(
                principal.getUserId(), type, pageable
        );
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-transactions/date-range")
    public ResponseEntity<Page<TransactionResponse>> getMyTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        log.info("Request to get transactions for user: {} between {} and {}", principal.getUserId(), startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<TransactionResponse> response = transactionService.getUserTransactionByDateRange(
                principal.getUserId(), startDate, endDate, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-transactions/search")
    public ResponseEntity<Page<TransactionResponse>> searchMyTransactions(
            @RequestParam String searchTerm,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("REST request to search transactions for user: {} with term: {}",
                principal.getUserId(), searchTerm);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<TransactionResponse> response = transactionService.searchTransactions(
                principal.getUserId(), searchTerm, pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("REST request to get transaction summary for user: {}", principal.getUserId());
        TransactionSummaryResponse summary = transactionService.getTransactionSummary(
                principal.getUserId());
        return ResponseEntity.ok(summary);
    }


}



