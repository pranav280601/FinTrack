package com.fintrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.fintrack.model.Category;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private Category.TransactionType type;
    private String categoryName;
    private LocalDate createdAt;
}