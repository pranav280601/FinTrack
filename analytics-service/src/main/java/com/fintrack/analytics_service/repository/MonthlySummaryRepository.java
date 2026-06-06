package com.fintrack.analytics_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintrack.analytics_service.model.MonthlySummary;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {
    Optional<MonthlySummary> findByUserIdAndYearAndMonth(Long userId, int month, int year);
    List<MonthlySummary> findByUserIdOrderByYearDescMonthDesc(Long userId);
}
