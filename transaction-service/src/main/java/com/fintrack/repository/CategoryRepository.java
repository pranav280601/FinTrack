package com.fintrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.fintrack.model.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

}
