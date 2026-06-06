package com.fintrack.analytics_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.fintrack.analytics_service.model.Category;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private Long transactionId;
    private Long userId;
    private BigDecimal amount;
    private Category.TransactionType type;
    private String categoryName;
    private LocalDate date;
}
