package com.banking.paymentService.controller;


import com.banking.paymentService.dto.request.UpdateTransactionStatusRequest;
import com.banking.paymentService.dto.response.TransactionResponse;
import com.banking.paymentService.service.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminTransactionController {

    private final TransactionService transactionService;

    private static final Logger log = LoggerFactory.getLogger(AdminTransactionController.class);

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{transactionId}/status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable UUID transactionId,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {

        log.info("ADMIN: REST request to update transaction {} status to {}",
                transactionId, request.getStatus());
        TransactionResponse response = transactionService.updateTransactionStatus(
                transactionId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<Void> cancelTransaction(@PathVariable UUID transactionId) {
        log.info("ADMIN: REST request to cancel transaction: {}", transactionId);
        transactionService.cancelTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{transactionId}/reverse")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable UUID transactionId,
            @RequestParam(required = false) String reason) {

        log.info("ADMIN: REST request to reverse transaction: {}", transactionId);
        TransactionResponse response = transactionService.reverseTransaction(transactionId, reason);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<TransactionResponse>> getPendingTransactions() {
        log.info("ADMIN: REST request to get all pending transactions");
        List<TransactionResponse> response = transactionService.getPendingTransactions();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/process-stale")
    public ResponseEntity<Void> processStaleTransactions() {
        log.info("ADMIN: REST request to process stale transactions");
        transactionService.processStaleTransactions();
        return ResponseEntity.ok().build();
    }
}
