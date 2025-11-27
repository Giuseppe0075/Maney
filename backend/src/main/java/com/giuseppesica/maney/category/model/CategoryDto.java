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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    @NotNull(message = "Name is required")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Color is required")
    @Pattern(regexp ="^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code")
    private String color;

    @NotNull(message = "Type is required")
    private CategoryType type;

    private Long parentId;

    private List<CategoryDto> children;

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
