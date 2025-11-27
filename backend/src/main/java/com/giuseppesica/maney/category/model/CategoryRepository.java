package com.giuseppesica.maney.category.model;

import com.giuseppesica.maney.utils.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameAndUserIdAndType(String name, Long userId, CategoryType type);
    boolean existsByParentId(Long parentId);
    Optional<Category> findByUserIdAndId(Long userId, Long id);
}
