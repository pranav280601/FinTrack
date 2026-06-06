package com.fintrack.analytics_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintrack.analytics_service.model.MonthlySummary;
import com.fintrack.analytics_service.repository.MonthlySummaryRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final MonthlySummaryRepository summaryRepository;

    @GetMapping("/summary")
    public ResponseEntity<List<MonthlySummary>> getSummaries(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
            summaryRepository.findByUserIdOrderByYearDescMonthDesc(
                Long.parseLong(userId)));
    }

    @GetMapping("/summary/{year}/{month}")
    public ResponseEntity<MonthlySummary> getMonthlySummary(
            @PathVariable int year,
            @PathVariable int month,
            @AuthenticationPrincipal String userId) {
        return summaryRepository
            .findByUserIdAndYearAndMonth(Long.parseLong(userId), year, month)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}