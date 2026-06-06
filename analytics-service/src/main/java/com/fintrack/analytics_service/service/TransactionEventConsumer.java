package com.fintrack.analytics_service.service;

import java.math.BigDecimal;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fintrack.analytics_service.event.TransactionCreatedEvent;
import com.fintrack.analytics_service.model.MonthlySummary;
import com.fintrack.analytics_service.repository.MonthlySummaryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final MonthlySummaryRepository summaryRepository;

    @KafkaListener(topics = "transaction-created", groupId = "analytics-group")
    @Transactional
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Received transaction event for userId: {}, amount: {}",
            event.getUserId(), event.getAmount());

        int year = event.getDate().getYear();
        int month = event.getDate().getMonthValue();

        // Find existing summary or create new one
        MonthlySummary summary = summaryRepository
            .findByUserIdAndYearAndMonth(event.getUserId(), year, month)
            .orElseGet(() -> createNewSummary(event.getUserId(), year, month));

        // Update based on transaction type
        if (event.getType().name().equals("INCOME")) {
            summary.setTotalIncome(
                summary.getTotalIncome().add(event.getAmount()));
        } else {
            summary.setTotalExpense(
                summary.getTotalExpense().add(event.getAmount()));
        }

        summary.setNetSavings(
            summary.getTotalIncome().subtract(summary.getTotalExpense()));
        summary.setTransactionCount(summary.getTransactionCount() + 1);

        summaryRepository.save(summary);
        log.info("Updated monthly summary for userId: {}, {}/{}", 
            event.getUserId(), month, year);
    }

    private MonthlySummary createNewSummary(Long userId, int year, int month) {
        MonthlySummary summary = new MonthlySummary();
        summary.setUserId(userId);
        summary.setYear(year);
        summary.setMonth(month);
        summary.setTotalIncome(BigDecimal.ZERO);
        summary.setTotalExpense(BigDecimal.ZERO);
        summary.setNetSavings(BigDecimal.ZERO);
        summary.setTransactionCount(0);
        return summary;
    }
}
