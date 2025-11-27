package com.giuseppesica.maney.category.service;

import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.model.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Category entity operations.
 * Handles business logic for category management including CRUD operations.
 * All write operations are transactional, read operations use read-only transactions.
 *
 * @author Giuseppe Sica
 * @version 1.0
 * @since 2025-11-27
 */
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param categoryRepository Repository for category data access
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Finds a category by its ID.
     * Uses read-only transaction for performance optimization.
     *
     * @param id ID of the category to find
     * @return Optional containing the category if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Finds a category by user ID and category ID.
     * Ensures the category belongs to the specified user.
     * Uses read-only transaction for performance optimization.
     *
     * @param userId ID of the user who owns the category
     * @param id ID of the category to find
     * @return Optional containing the category if found and owned by user, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<Category> findByUserAndId(Long userId, Long id) {
        return categoryRepository.findByUserIdAndId(userId, id);
    }

    /**
     * Saves a category (create or update).
     * If the category has an ID, it will be updated; otherwise, a new category is created.
     * Transactional to ensure data consistency.
     *
     * @param category Category to save
     * @return Saved category with generated ID if new
     */
    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Deletes a category.
     * If the category has children, they will be automatically deleted due to cascade configuration.
     * Transactional to ensure atomic operation.
     *
     * @param category Category to delete
     */
    @Transactional
    public void deleteCategory(Category category) {
        categoryRepository.delete(category);
    }

    /**
     * Retrieves all categories for a specific user.
     *
     * @param id ID of the user
     * @return List of all categories owned by the user, empty list if none found
     */
    public List<Category> findByUserId(Long id) {
        return categoryRepository.findByUserId(id);
    }
}
