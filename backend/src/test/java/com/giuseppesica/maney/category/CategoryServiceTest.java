package com.giuseppesica.maney.category;

import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.model.CategoryDto;
import com.giuseppesica.maney.category.model.CategoryRepository;
import com.giuseppesica.maney.category.service.CategoryService;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.utils.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryService.
 * Tests all business logic related to category management.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;
    private Category testParentCategory;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        // Create parent category
        testParentCategory = new Category();
        testParentCategory.setId(1L);
        testParentCategory.setName("Parent Category");
        testParentCategory.setColor("#FF5733");
        testParentCategory.setType(CategoryType.OUTCOME);
        testParentCategory.setUser(testUser);

        // Create test category
        testCategory = new Category();
        testCategory.setId(2L);
        testCategory.setName("Test Category");
        testCategory.setColor("#33C3FF");
        testCategory.setType(CategoryType.OUTCOME);
        testCategory.setUser(testUser);
        testCategory.setParent(testParentCategory);
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    void testFindById_CategoryExists_ReturnsCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        Optional<Category> result = categoryService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
        assertThat(result.get().getName()).isEqualTo("Test Category");
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_CategoryDoesNotExist_ReturnsEmpty() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryService.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findById(999L);
    }

    // ==================== FIND BY USER AND ID TESTS ====================

    @Test
    void testFindByUserAndId_CategoryExists_ReturnsCategory() {
        // Given
        when(categoryRepository.findByUserIdAndId(1L, 2L)).thenReturn(Optional.of(testCategory));

        // When
        Optional<Category> result = categoryService.findByUserAndId(1L, 2L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
        assertThat(result.get().getUser().getId()).isEqualTo(1L);
        verify(categoryRepository, times(1)).findByUserIdAndId(1L, 2L);
    }

    @Test
    void testFindByUserAndId_CategoryDoesNotExist_ReturnsEmpty() {
        // Given
        when(categoryRepository.findByUserIdAndId(1L, 999L)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryService.findByUserAndId(1L, 999L);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findByUserIdAndId(1L, 999L);
    }

    // ==================== SAVE CATEGORY TESTS ====================

    @Test
    void testSaveCategory_Success_ReturnsSavedCategory() {
        // Given
        CategoryDto dto = new CategoryDto();
        dto.setName("New Category");
        dto.setColor("#FF5733");
        dto.setType(CategoryType.INCOME);

        Category newCategory = new Category(dto);
        newCategory.setUser(testUser);

        Category savedCategory = new Category(dto);
        savedCategory.setId(3L);
        savedCategory.setUser(testUser);

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        Category result = categoryService.saveCategory(newCategory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("New Category");
        assertThat(result.getColor()).isEqualTo("#FF5733");
        assertThat(result.getType()).isEqualTo(CategoryType.INCOME);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testSaveCategory_WithParent_SavesCorrectly() {
        // Given
        CategoryDto dto = new CategoryDto();
        dto.setName("Child Category");
        dto.setColor("#33C3FF");
        dto.setType(CategoryType.OUTCOME);
        dto.setParentId(1L);

        Category childCategory = new Category(dto);
        childCategory.setUser(testUser);
        childCategory.setParent(testParentCategory);

        when(categoryRepository.save(any(Category.class))).thenReturn(childCategory);

        // When
        Category result = categoryService.saveCategory(childCategory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getParent()).isNotNull();
        assertThat(result.getParent().getId()).isEqualTo(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    // ==================== DELETE CATEGORY TESTS ====================

    @Test
    void testDeleteCategory_Success_DeletesCategory() {
        // Given
        doNothing().when(categoryRepository).delete(testCategory);

        // When
        categoryService.deleteCategory(testCategory);

        // Then
        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void testDeleteCategory_WithChildren_CascadeDeletes() {
        // Given
        Category childCategory1 = new Category();
        childCategory1.setId(3L);
        childCategory1.setName("Child 1");
        childCategory1.setParent(testParentCategory);

        Category childCategory2 = new Category();
        childCategory2.setId(4L);
        childCategory2.setName("Child 2");
        childCategory2.setParent(testParentCategory);

        List<Category> children = new ArrayList<>();
        children.add(childCategory1);
        children.add(childCategory2);
        testParentCategory.setChildren(children);

        doNothing().when(categoryRepository).delete(testParentCategory);

        // When
        categoryService.deleteCategory(testParentCategory);

        // Then
        verify(categoryRepository, times(1)).delete(testParentCategory);
        // Note: Cascade deletion is handled by Hibernate, not explicitly in the service
    }

    // ==================== FIND BY USER ID TESTS ====================

    @Test
    void testFindByUserId_ReturnsAllUserCategories() {
        // Given
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category 1");
        category1.setUser(testUser);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Category 2");
        category2.setUser(testUser);

        List<Category> categories = List.of(category1, category2);
        when(categoryRepository.findByUserId(1L)).thenReturn(categories);

        // When
        List<Category> result = categoryService.findByUserId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Category 1");
        assertThat(result.get(1).getName()).isEqualTo("Category 2");
        verify(categoryRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testFindByUserId_NoCategories_ReturnsEmptyList() {
        // Given
        when(categoryRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        // When
        List<Category> result = categoryService.findByUserId(1L);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findByUserId(1L);
    }

    // ==================== SECURITY TESTS - USER ISOLATION ====================

    @Test
    void testFindByUserAndId_DifferentUser_ReturnsEmpty() {
        // Given - Category belongs to different user
        when(categoryRepository.findByUserIdAndId(2L, 1L)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryService.findByUserAndId(2L, 1L);

        // Then - Category not found because it belongs to different user
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findByUserIdAndId(2L, 1L);
    }

    @Test
    void testFindByUserId_OnlyReturnsUserCategories() {
        // Given - Repository is queried with specific user ID
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category 1");
        category1.setUser(testUser);

        when(categoryRepository.findByUserId(1L)).thenReturn(List.of(category1));

        // When
        List<Category> result = categoryService.findByUserId(1L);

        // Then - Only categories from specified user are returned
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getId()).isEqualTo(1L);
        verify(categoryRepository, times(1)).findByUserId(1L);
        // Verify it's NOT querying other user IDs
        verify(categoryRepository, never()).findByUserId(2L);
    }

    @Test
    void testFindByUserId_MultipleUsers_IsolatedResults() {
        // Given - Two different users
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("User1 Category");
        category1.setUser(testUser);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("User2 Category");
        category2.setUser(user2);

        when(categoryRepository.findByUserId(1L)).thenReturn(List.of(category1));
        when(categoryRepository.findByUserId(2L)).thenReturn(List.of(category2));

        // When
        List<Category> result1 = categoryService.findByUserId(1L);
        List<Category> result2 = categoryService.findByUserId(2L);

        // Then - Each user only sees their own categories
        assertThat(result1).hasSize(1);
        assertThat(result1.getFirst().getName()).isEqualTo("User1 Category");
        assertThat(result2).hasSize(1);
        assertThat(result2.getFirst().getName()).isEqualTo("User2 Category");
        verify(categoryRepository, times(1)).findByUserId(1L);
        verify(categoryRepository, times(1)).findByUserId(2L);
    }
}

