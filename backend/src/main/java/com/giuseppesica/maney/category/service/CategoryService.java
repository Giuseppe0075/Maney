package com.giuseppesica.maney.category.service;

import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.model.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByUserAndId(Long userId, Long id) {
        return categoryRepository.findByUserIdAndId(userId, id);
    }

    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Category category) {
        categoryRepository.delete(category);
    }
}
