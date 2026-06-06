package com.fintrack.dto;

import com.fintrack.model.Category;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required")
    private Category.TransactionType type;
}
