package com.fintrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fintrack.model.Category;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Type is required")
    private Category.TransactionType type;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
