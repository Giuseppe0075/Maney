package com.giuseppesica.maney.category.controller;

import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.model.CategoryDto;
import com.giuseppesica.maney.category.service.CategoryService;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Category management.
 * Provides endpoints for CRUD operations on user categories.
 * All endpoints require authentication and operate only on the authenticated user's categories.
 * Base path: /user/categories
 *
 * @author Giuseppe Sica
 * @version 1.0
 * @since 2025-11-27
 */
@RestController
@RequestMapping("/user/categories")
public class CategoryController {
    private final UserService userService;
    private final CategoryService categoryService;

    /**
     * Constructor for dependency injection.
     *
     * @param userService Service for user operations
     * @param categoryService Service for category operations
     */
    public CategoryController(UserService userService, CategoryService categoryService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

    /**
     * Creates a new category for the authenticated user.
     * If parentId is provided, sets up the hierarchical relationship.
     * Endpoint: POST /user/category
     *
     * @param authentication Spring Security authentication object containing user details
     * @param categoryDto DTO containing category data (name, color, type, optional parentId)
     * @return ResponseEntity with created CategoryDto and HTTP 200 OK
     * @throws NotFoundException if parent category does not exist
     */
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            Authentication authentication,
            @Valid @RequestBody CategoryDto categoryDto
            ) {

        User user = userService.UserFromAuthentication(authentication);
        Category category = new Category(categoryDto);
        if (categoryDto.getParentId() != null) {
            Category parentCategory = categoryService.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent category does not exist"));
            category.setParent(parentCategory);
        }
        category.setUser(user);
        CategoryDto responseDto = new CategoryDto(categoryService.saveCategory(category));
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Retrieves a specific category by ID for the authenticated user.
     * Ensures the category belongs to the requesting user.
     * Endpoint: GET /user/category/{id}
     *
     * @param authentication Spring Security authentication object
     * @param id ID of the category to retrieve
     * @return ResponseEntity with CategoryDto and HTTP 200 OK
     * @throws NotFoundException if category not found or doesn't belong to user
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.UserFromAuthentication(authentication);
        Category category = categoryService.findByUserAndId(user.getId(), id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        CategoryDto responseDto = new CategoryDto(category);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Updates an existing category for the authenticated user.
     * Can modify name, color, type, and parent relationship.
     * Setting parentId to null removes the parent relationship.
     * Endpoint: PUT /user/category/{id}
     *
     * @param authentication Spring Security authentication object
     * @param id ID of the category to update
     * @param categoryDto DTO containing updated category data
     * @return ResponseEntity with updated CategoryDto and HTTP 200 OK
     * @throws NotFoundException if category or parent category not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto
    ) {
        User user = userService.UserFromAuthentication(authentication);
        Category category = categoryService.findByUserAndId(user.getId(), id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        category.setName(categoryDto.getName());
        category.setColor(categoryDto.getColor());
        category.setType(categoryDto.getType());
        if(categoryDto.getParentId() != null) {
            Category parentCategory = categoryService.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent category does not exist"));
            category.setParent(parentCategory);
        } else {
            category.setParent(null);
        }
        CategoryDto responseDto = new CategoryDto(categoryService.saveCategory(category));
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Deletes a category for the authenticated user.
     * If the category has children, they will be automatically deleted (cascade).
     * Endpoint: DELETE /user/category/{id}
     *
     * @param authentication Spring Security authentication object
     * @param id ID of the category to delete
     * @return ResponseEntity with HTTP 204 No Content
     * @throws NotFoundException if category not found or doesn't belong to user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.UserFromAuthentication(authentication);
        Category category = categoryService.findByUserAndId(user.getId(), id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        categoryService.deleteCategory(category);
        return ResponseEntity.noContent().build();
    }

}
