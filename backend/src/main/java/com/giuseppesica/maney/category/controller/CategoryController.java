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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user/category")
public class CategoryController {
    private final UserService userService;
    private final CategoryService categoryService;

    public CategoryController(UserService userService, CategoryService categoryService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

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
