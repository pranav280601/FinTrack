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

import com.fintrack.dto.CategoryRequest;
import com.fintrack.model.Category;
import com.fintrack.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> create(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
            categoryService.createCategory(request, Long.parseLong(userId)));
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
            categoryService.getUserCategories(Long.parseLong(userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId) {
        categoryService.deleteCategory(id, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}