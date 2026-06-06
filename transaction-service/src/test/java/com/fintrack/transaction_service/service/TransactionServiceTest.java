package com.fintrack.transaction_service.service;

import com.fintrack.dto.*;
import com.fintrack.exception.*;
import com.fintrack.model.*;
import com.fintrack.repository.*;
import com.fintrack.service.KafkaProducerService;
import com.fintrack.service.TransactionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private TransactionService transactionService;

    private Category salaryCategory;
    private Transaction savedTransaction;
    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        salaryCategory = new Category();
        salaryCategory.setId(1L);
        salaryCategory.setName("Salary");
        salaryCategory.setType(Category.TransactionType.INCOME);
        salaryCategory.setUserId(1L);

        request = new TransactionRequest();
        request.setAmount(new BigDecimal("50000.00"));
        request.setDescription("May salary");
        request.setDate(LocalDate.of(2026, 5, 1));
        request.setType(Category.TransactionType.INCOME);
        request.setCategoryId(1L);

        savedTransaction = new Transaction();
        savedTransaction.setId(1L);
        savedTransaction.setAmount(new BigDecimal("50000.00"));
        savedTransaction.setDescription("May salary");
        savedTransaction.setDate(LocalDate.of(2026, 5, 1));
        savedTransaction.setType(Category.TransactionType.INCOME);
        savedTransaction.setUserId(1L);
        savedTransaction.setCategory(salaryCategory);
        savedTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create transaction and publish Kafka event")
    void createTransaction_Success() {
        // Arrange
        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(salaryCategory));
        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(savedTransaction);
        doNothing().when(kafkaProducerService)
            .publishTransactionCreated(any());

        // Act
        TransactionResponse response =
            transactionService.createTransaction(request, 1L);

        // Assert
        assertThat(response.getAmount())
            .isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.getCategoryName()).isEqualTo("Salary");
        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaProducerService).publishTransactionCreated(any());
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void createTransaction_CategoryNotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.findById(99L))
            .thenReturn(Optional.empty());

        request.setCategoryId(99L);

        // Act & Assert
        assertThatThrownBy(() ->
            transactionService.createTransaction(request, 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Category not found");

        verify(transactionRepository, never()).save(any());
        verify(kafkaProducerService, never()).publishTransactionCreated(any());
    }

    @Test
    @DisplayName("Should throw exception when user tries to use another user's category")
    void createTransaction_UnauthorizedCategory_ThrowsException() {
        // Arrange — category belongs to userId 2, not 1
        salaryCategory.setUserId(2L);
        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(salaryCategory));

        // Act & Assert
        assertThatThrownBy(() ->
            transactionService.createTransaction(request, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Unauthorized");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return all transactions for user ordered by date")
    void getUserTransactions_ReturnsUserTransactions() {
        // Arrange
        when(transactionRepository.findByUserIdOrderByDateDesc(1L))
            .thenReturn(List.of(savedTransaction));

        // Act
        List<TransactionResponse> result =
            transactionService.getUserTransactions(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount())
            .isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("Should throw exception when deleting another user's transaction")
    void deleteTransaction_UnauthorizedUser_ThrowsException() {
        // Arrange — transaction belongs to userId 2
        savedTransaction.setUserId(2L);
        when(transactionRepository.findById(1L))
            .thenReturn(Optional.of(savedTransaction));

        // Act & Assert
        assertThatThrownBy(() ->
            transactionService.deleteTransaction(1L, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Unauthorized");

        verify(transactionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should not publish Kafka event if transaction save fails")
    void createTransaction_SaveFails_NoKafkaEvent() {
        // Arrange
        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(salaryCategory));
        when(transactionRepository.save(any()))
            .thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        assertThatThrownBy(() ->
            transactionService.createTransaction(request, 1L))
            .isInstanceOf(RuntimeException.class);

        verify(kafkaProducerService, never()).publishTransactionCreated(any());
    }
}