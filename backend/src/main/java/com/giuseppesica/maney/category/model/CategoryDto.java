package com.giuseppesica.maney.category.model;

import com.giuseppesica.maney.utils.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object for Category entity.
 * Used for API requests and responses to transfer category data between client and server.
 * Includes validation constraints for input data.
 *
 * @author Giuseppe Sica
 * @version 1.0
 * @since 2025-11-27
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    /**
     * Name of the category.
     * Must not be null or blank.
     * Examples: "Food", "Transport", "Salary"
     */
    @NotNull(message = "Name is required")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * Hex color code for UI visualization.
     * Must be a valid 6-digit hex color code starting with #.
     * Format: #RRGGBB (e.g., "#FF5733", "#33C3FF")
     */
    @NotNull(message = "Color is required")
    @Pattern(regexp ="^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code")
    private String color;

    /**
     * Type of category (INCOME or OUTCOME).
     * Must not be null.
     */
    @NotNull(message = "Type is required")
    private CategoryType type;

    /**
     * ID of the parent category for hierarchical categorization.
     * Optional - can be null for top-level categories.
     */
    private Long parentId;

    /**
     * List of child categories.
     * Populated when converting from entity to DTO.
     * Optional - can be null if children are not loaded or don't exist.
     */
    private List<CategoryDto> children;

    /**
     * Constructor to create a CategoryDto from a Category entity.
     * Recursively converts children if present.
     *
     * @param category Category entity to convert
     */
    public CategoryDto(Category category) {
        this.name = category.getName();
        this.color = category.getColor();
        this.type = category.getType();
        if (category.getParent() != null) {
            this.parentId = category.getParent().getId();
        }
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            this.children = category.getChildren().stream()
                    .map(CategoryDto::new)
                    .toList();
        }
    }
}
