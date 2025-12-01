package com.giuseppesica.maney.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppesica.maney.category.controller.CategoryController;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.model.CategoryDto;
import com.giuseppesica.maney.category.service.CategoryService;
import com.giuseppesica.maney.config.CorsConfig;
import com.giuseppesica.maney.config.SecurityConfig;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import com.giuseppesica.maney.utils.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CategoryController.
 * Tests all REST endpoints for category management using MockMvc.
 */
@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationHelper authenticationHelper;

    private User testUser;
    private Category testCategory;
    private Category testParentCategory;
    private CategoryDto testCategoryDto;

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
        testParentCategory.setChildren(new ArrayList<>());

        // Create test category
        testCategory = new Category();
        testCategory.setId(2L);
        testCategory.setName("Test Category");
        testCategory.setColor("#33C3FF");
        testCategory.setType(CategoryType.OUTCOME);
        testCategory.setUser(testUser);
        testCategory.setParent(testParentCategory);
        testCategory.setChildren(new ArrayList<>());

        // Create test DTO
        testCategoryDto = new CategoryDto();
        testCategoryDto.setName("New Category");
        testCategoryDto.setColor("#28A745");
        testCategoryDto.setType(CategoryType.INCOME);
    }

    // ==================== CREATE CATEGORY TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateCategory_Success_ReturnsCreated() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);

        Category savedCategory = new Category(testCategoryDto);
        savedCategory.setId(3L);
        savedCategory.setUser(testUser);

        when(categoryService.saveCategory(any(Category.class))).thenReturn(savedCategory);

        // When & Then
        mockMvc.perform(post("/user/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Category"))
                .andExpect(jsonPath("$.color").value("#28A745"))
                .andExpect(jsonPath("$.type").value("INCOME"));

        verify(userService, times(1)).UserFromAuthentication(any(Authentication.class));
        verify(categoryService, times(1)).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateCategory_WithParent_Success() throws Exception {
        // Given
        testCategoryDto.setParentId(1L);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findById(1L)).thenReturn(Optional.of(testParentCategory));

        Category savedCategory = new Category(testCategoryDto);
        savedCategory.setId(3L);
        savedCategory.setUser(testUser);
        savedCategory.setParent(testParentCategory);

        when(categoryService.saveCategory(any(Category.class))).thenReturn(savedCategory);

        // When & Then
        mockMvc.perform(post("/user/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Category"))
                .andExpect(jsonPath("$.parentId").value(1L));

        verify(categoryService, times(1)).findById(1L);
        verify(categoryService, times(1)).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateCategory_ParentNotFound_Returns404() throws Exception {
        // Given
        testCategoryDto.setParentId(999L);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/user/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryDto)))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).findById(999L);
        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateCategory_InvalidData_Returns400() throws Exception {
        // Given
        CategoryDto invalidDto = new CategoryDto();
        invalidDto.setName("");  // Blank name
        invalidDto.setColor("invalid");  // Invalid color format
        invalidDto.setType(CategoryType.INCOME);

        // When & Then
        mockMvc.perform(post("/user/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    // ==================== GET SINGLE CATEGORY TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetCategory_Success_ReturnsCategory() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 2L)).thenReturn(Optional.of(testCategory));

        // When & Then
        mockMvc.perform(get("/user/categories/{id}", 2L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.color").value("#33C3FF"))
                .andExpect(jsonPath("$.type").value("OUTCOME"))
                .andExpect(jsonPath("$.parentId").value(1L));

        verify(userService, times(1)).UserFromAuthentication(any(Authentication.class));
        verify(categoryService, times(1)).findByUserAndId(1L, 2L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetCategory_NotFound_Returns404() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/user/categories/{id}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).findByUserAndId(1L, 999L);
    }

    // ==================== GET ALL USER CATEGORIES TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetUserCategories_Success_ReturnsAllCategories() throws Exception {
        // Given
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category 1");
        category1.setColor("#FF5733");
        category1.setType(CategoryType.INCOME);
        category1.setUser(testUser);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Category 2");
        category2.setColor("#33C3FF");
        category2.setType(CategoryType.OUTCOME);
        category2.setUser(testUser);

        List<Category> categories = List.of(category1, category2);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserId(1L)).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/user/categories")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Category 1"))
                .andExpect(jsonPath("$[0].color").value("#FF5733"))
                .andExpect(jsonPath("$[0].type").value("INCOME"))
                .andExpect(jsonPath("$[1].name").value("Category 2"))
                .andExpect(jsonPath("$[1].color").value("#33C3FF"))
                .andExpect(jsonPath("$[1].type").value("OUTCOME"));

        verify(userService, times(1)).UserFromAuthentication(any(Authentication.class));
        verify(categoryService, times(1)).findByUserId(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetUserCategories_EmptyList_ReturnsEmptyArray() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserId(1L)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/user/categories")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).UserFromAuthentication(any(Authentication.class));
        verify(categoryService, times(1)).findByUserId(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetUserCategories_WithHierarchy_ReturnsNestedCategories() throws Exception {
        // Given
        Category parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Parent");
        parentCategory.setColor("#FF5733");
        parentCategory.setType(CategoryType.OUTCOME);
        parentCategory.setUser(testUser);

        Category childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setName("Child");
        childCategory.setColor("#33C3FF");
        childCategory.setType(CategoryType.OUTCOME);
        childCategory.setUser(testUser);
        childCategory.setParent(parentCategory);

        List<Category> children = new ArrayList<>();
        children.add(childCategory);
        parentCategory.setChildren(children);

        List<Category> categories = List.of(parentCategory, childCategory);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserId(1L)).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/user/categories")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Parent"))
                .andExpect(jsonPath("$[0].children").isArray())
                .andExpect(jsonPath("$[0].children.length()").value(1))
                .andExpect(jsonPath("$[0].children[0].name").value("Child"))
                .andExpect(jsonPath("$[1].name").value("Child"))
                .andExpect(jsonPath("$[1].parentId").value(1));

        verify(categoryService, times(1)).findByUserId(1L);
    }

    @Test
    void testGetUserCategories_Unauthenticated_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/user/categories")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).findByUserId(any());
    }

    // ==================== UPDATE CATEGORY TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateCategory_Success_ReturnsUpdated() throws Exception {
        // Given
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");
        updateDto.setColor("#FF0000");
        updateDto.setType(CategoryType.INCOME);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 2L)).thenReturn(Optional.of(testCategory));

        Category updatedCategory = new Category();
        updatedCategory.setId(2L);
        updatedCategory.setName("Updated Category");
        updatedCategory.setColor("#FF0000");
        updatedCategory.setType(CategoryType.INCOME);
        updatedCategory.setUser(testUser);

        when(categoryService.saveCategory(any(Category.class))).thenReturn(updatedCategory);

        // When & Then
        mockMvc.perform(put("/user/categories/{id}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Category"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.type").value("INCOME"));

        verify(categoryService, times(1)).findByUserAndId(1L, 2L);
        verify(categoryService, times(1)).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateCategory_ChangeParent_Success() throws Exception {
        // Given
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");
        updateDto.setColor("#FF0000");
        updateDto.setType(CategoryType.OUTCOME);
        updateDto.setParentId(1L);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 2L)).thenReturn(Optional.of(testCategory));
        when(categoryService.findById(1L)).thenReturn(Optional.of(testParentCategory));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(testCategory);

        // When & Then
        mockMvc.perform(put("/user/categories/{id}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).findById(1L);
        verify(categoryService, times(1)).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateCategory_RemoveParent_Success() throws Exception {
        // Given
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");
        updateDto.setColor("#FF0000");
        updateDto.setType(CategoryType.OUTCOME);
        updateDto.setParentId(null);  // Remove parent

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 2L)).thenReturn(Optional.of(testCategory));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(testCategory);

        // When & Then
        mockMvc.perform(put("/user/categories/{id}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(categoryService, never()).findById(any());
        verify(categoryService, times(1)).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateCategory_NotFound_Returns404() throws Exception {
        // Given
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");
        updateDto.setColor("#FF0000");
        updateDto.setType(CategoryType.INCOME);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/user/categories/{id}", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).findByUserAndId(1L, 999L);
        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateCategory_ParentNotFound_Returns404() throws Exception {
        // Given
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");
        updateDto.setColor("#FF0000");
        updateDto.setType(CategoryType.OUTCOME);
        updateDto.setParentId(999L);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 2L)).thenReturn(Optional.of(testCategory));
        when(categoryService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/user/categories/{id}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).findById(999L);
        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    // ==================== DELETE CATEGORY TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteCategory_Success_ReturnsNoContent() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 2L)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryService).deleteCategory(testCategory);

        // When & Then
        mockMvc.perform(delete("/user/categories/{id}", 2L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).findByUserAndId(1L, 2L);
        verify(categoryService, times(1)).deleteCategory(testCategory);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteCategory_NotFound_Returns404() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/user/categories/{id}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).findByUserAndId(1L, 999L);
        verify(categoryService, never()).deleteCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteCategory_WithChildren_CascadeDeletes() throws Exception {
        // Given
        Category childCategory = new Category();
        childCategory.setId(3L);
        childCategory.setName("Child Category");
        childCategory.setParent(testParentCategory);

        List<Category> children = new ArrayList<>();
        children.add(childCategory);
        testParentCategory.setChildren(children);

        when(userService.UserFromAuthentication(any(Authentication.class))).thenReturn(testUser);
        when(categoryService.findByUserAndId(1L, 1L)).thenReturn(Optional.of(testParentCategory));
        doNothing().when(categoryService).deleteCategory(testParentCategory);

        // When & Then
        mockMvc.perform(delete("/user/categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(testParentCategory);
        // Note: Cascade deletion is handled by Hibernate
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    void testCreateCategory_Unauthenticated_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(post("/user/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryDto)))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    @Test
    void testGetCategory_Unauthenticated_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/user/categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).findByUserAndId(any(), any());
    }
}

