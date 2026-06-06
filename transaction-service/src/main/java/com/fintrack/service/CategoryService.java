package com.fintrack.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintrack.dto.CategoryRequest;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Category;
import com.fintrack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category createCategory(CategoryRequest request, Long userId) {
        if (categoryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new RuntimeException("Category already exists: " + request.getName());
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUserId(userId);
        return categoryRepository.save(category);
    }

    public List<Category> getUserCategories(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    public void deleteCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        

        // IDOR - Insecure direct object reference : allows users to delete categories that only belongs to them
        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        categoryRepository.delete(category);
    }
}
