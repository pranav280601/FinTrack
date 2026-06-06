package com.fintrack.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fintrack.dto.TransactionRequest;
import com.fintrack.dto.TransactionResponse;
import com.fintrack.event.TransactionCreatedEvent;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaProducerService kafkaProducerService;

    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized category access");
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setType(request.getType());
        transaction.setUserId(userId);
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Publish event - fire and forget

        TransactionCreatedEvent event = new TransactionCreatedEvent(
            savedTransaction.getId(),
            userId,
            savedTransaction.getAmount(),
            savedTransaction.getType(),
            category.getName(),
            savedTransaction.getDate()
        );

        kafkaProducerService.publishTransactionCreated(event);

        return toResponse(savedTransaction);
    }

    public List<TransactionResponse> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public TransactionResponse getTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return toResponse(transaction);
    }

    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        transactionRepository.delete(transaction);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
            t.getId(),
            t.getAmount(),
            t.getDescription(),
            t.getDate(),
            t.getType(),
            t.getCategory().getName(),
            t.getCreatedAt().toLocalDate()
        );
    }
}