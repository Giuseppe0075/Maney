package com.giuseppesica.maney.category.model;

import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.utils.CategoryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a category for cash movements.
 * Categories can be hierarchical (parent-child relationship) and belong to a specific user.
 * When a parent category is deleted, all its children are automatically deleted (cascade).
 *
 * @author Giuseppe Sica
 * @version 1.0
 * @since 2025-11-27
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /**
     * Unique identifier for the category.
     */
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent category for hierarchical categorization.
     * Can be null for top-level categories.
     * Example: "Food" can be parent of "Restaurants", "Groceries", etc.
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * List of child categories.
     * Automatically managed by JPA - when parent is deleted, children are also deleted (cascade).
     * orphanRemoval = true ensures that removing a child from this list deletes it from database.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    /**
     * Name of the category.
     * Must not be null or blank.
     * Example: "Food", "Transport", "Salary"
     */
    @NotNull
    @NotBlank
    private String name;

    /**
     * Hex color code for UI visualization.
     * Must not be null.
     * Format: #RRGGBB (e.g., "#FF5733")
     */
    @NotNull
    private String color;

    /**
     * Type of category indicating if it's for income or outcome transactions.
     * Must not be null.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private CategoryType type;

    /**
     * Owner of the category.
     * Each category belongs to a specific user and cannot be null.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Constructor to create a Category from a CategoryDto.
     * Sets name, color, and type from the DTO.
     * Parent and user must be set separately.
     *
     * @param categoryDto DTO containing category data
     */
    public Category(CategoryDto categoryDto) {
        this.name = categoryDto.getName();
        this.color = categoryDto.getColor();
        this.type = categoryDto.getType();
    }
}
