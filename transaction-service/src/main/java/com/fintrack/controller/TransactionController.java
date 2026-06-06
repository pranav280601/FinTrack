package com.fintrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintrack.dto.TransactionRequest;
import com.fintrack.dto.TransactionResponse;
import com.fintrack.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
            transactionService.createTransaction(request, Long.parseLong(userId)));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
            transactionService.getUserTransactions(Long.parseLong(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getOne(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
            transactionService.getTransaction(id, Long.parseLong(userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId) {
        transactionService.deleteTransaction(id, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}