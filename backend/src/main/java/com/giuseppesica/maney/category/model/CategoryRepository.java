package com.giuseppesica.maney.category.model;

import com.giuseppesica.maney.utils.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity.
 * Provides CRUD operations and custom query methods for category management.
 * Extends JpaRepository for standard database operations.
 *
 * @author Giuseppe Sica
 * @version 1.0
 * @since 2025-11-27
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its name, user ID, and type.
     * Useful for checking if a category with the same characteristics already exists.
     *
     * @param name Name of the category
     * @param userId ID of the user who owns the category
     * @param type Type of the category (INCOME or OUTCOME)
     * @return Optional containing the category if found, empty otherwise
     */
    Optional<Category> findByNameAndUserIdAndType(String name, Long userId, CategoryType type);

    /**
     * Checks if any categories exist with the given parent ID.
     * Useful for determining if a category has children before deletion.
     *
     * @param parentId ID of the parent category
     * @return true if at least one child category exists, false otherwise
     */
    boolean existsByParentId(Long parentId);

    /**
     * Finds a category by user ID and category ID.
     * Ensures that the category belongs to the specified user.
     *
     * @param userId ID of the user
     * @param id ID of the category
     * @return Optional containing the category if found and owned by the user, empty otherwise
     */
    Optional<Category> findByUserIdAndId(Long userId, Long id);

    /**
     * Retrieves all categories belonging to a specific user.
     *
     * @param userId ID of the user
     * @return List of all categories owned by the user, empty list if none found
     */
    List<Category> findByUserId(Long userId);
}
