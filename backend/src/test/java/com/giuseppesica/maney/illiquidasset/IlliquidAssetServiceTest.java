package com.giuseppesica.maney.illiquidasset;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAssetRepository;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IlliquidAssetService.
 * Tests all business logic operations for managing illiquid assets.
 */
public class IlliquidAssetServiceTest {

    @Mock
    private IlliquidAssetRepository illiquidAssetRepository;

    @InjectMocks
    private IlliquidAssetService illiquidAssetService;

    private Portfolio portfolio;
    private IlliquidAsset illiquidAsset1;
    private IlliquidAsset illiquidAsset2;
    private IlliquidAssetDto illiquidAssetDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a test user
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        // Create a test portfolio
        portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setUser(user);

        // Create test illiquid assets
        illiquidAsset1 = new IlliquidAsset();
        illiquidAsset1.setId(1L);
        illiquidAsset1.setName("Real Estate");
        illiquidAsset1.setDescription("Apartment in downtown");
        illiquidAsset1.setEstimatedValue(250000.0f);
        illiquidAsset1.setPortfolio(portfolio);

        illiquidAsset2 = new IlliquidAsset();
        illiquidAsset2.setId(2L);
        illiquidAsset2.setName("Art Collection");
        illiquidAsset2.setDescription("Vintage paintings");
        illiquidAsset2.setEstimatedValue(50000.0f);
        illiquidAsset2.setPortfolio(portfolio);

        // Create a test DTO
        illiquidAssetDto = new IlliquidAssetDto();
        illiquidAssetDto.setName("Vintage Car");
        illiquidAssetDto.setDescription("1967 Mustang");
        illiquidAssetDto.setEstimatedValue(75000.0f);
    }

    @Test
    public void testGetIlliquidAssets_ReturnsListOfAssets() {
        // Given
        List<IlliquidAsset> assets = Arrays.asList(illiquidAsset1, illiquidAsset2);
        when(illiquidAssetRepository.findByPortfolioId(1L)).thenReturn(assets);

        // When
        List<IlliquidAssetDto> result = illiquidAssetService.getIlliquidAssets(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Real Estate", result.get(0).getName());
        assertEquals("Art Collection", result.get(1).getName());
        verify(illiquidAssetRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    public void testGetIlliquidAssets_EmptyPortfolio_ReturnsEmptyList() {
        // Given
        when(illiquidAssetRepository.findByPortfolioId(1L)).thenReturn(List.of());

        // When
        List<IlliquidAssetDto> result = illiquidAssetService.getIlliquidAssets(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(illiquidAssetRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    public void testGetIlliquidAssetById_AssetExists_ReturnsAsset() {
        // Given
        when(illiquidAssetRepository.findByIdAndPortfolioId(1L, 1L))
                .thenReturn(Optional.of(illiquidAsset1));

        // When
        Optional<IlliquidAsset> result = illiquidAssetService.getIlliquidAssetById(1L, 1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Real Estate", result.get().getName());
        assertEquals(250000.0f, result.get().getEstimatedValue());
        verify(illiquidAssetRepository, times(1)).findByIdAndPortfolioId(1L, 1L);
    }

    @Test
    public void testGetIlliquidAssetById_AssetDoesNotExist_ReturnsEmpty() {
        // Given
        when(illiquidAssetRepository.findByIdAndPortfolioId(99L, 1L))
                .thenReturn(Optional.empty());

        // When
        Optional<IlliquidAsset> result = illiquidAssetService.getIlliquidAssetById(1L, 99L);

        // Then
        assertFalse(result.isPresent());
        verify(illiquidAssetRepository, times(1)).findByIdAndPortfolioId(99L, 1L);
    }

    @Test
    public void testGetIlliquidAssetById_WrongPortfolio_ReturnsEmpty() {
        // Given
        when(illiquidAssetRepository.findByIdAndPortfolioId(1L, 99L))
                .thenReturn(Optional.empty());

        // When
        Optional<IlliquidAsset> result = illiquidAssetService.getIlliquidAssetById(99L, 1L);

        // Then
        assertFalse(result.isPresent());
        verify(illiquidAssetRepository, times(1)).findByIdAndPortfolioId(1L, 99L);
    }

    @Test
    public void testCreateIlliquidAsset_Success() {
        // Given
        IlliquidAsset savedAsset = new IlliquidAsset(illiquidAssetDto);
        savedAsset.setId(3L);
        savedAsset.setPortfolio(portfolio);

        when(illiquidAssetRepository.save(any(IlliquidAsset.class))).thenReturn(savedAsset);

        // When
        IlliquidAsset result = illiquidAssetService.createIlliquidAsset(illiquidAssetDto, portfolio);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Vintage Car", result.getName());
        assertEquals("1967 Mustang", result.getDescription());
        assertEquals(75000.0f, result.getEstimatedValue());
        assertEquals(portfolio, result.getPortfolio());
        verify(illiquidAssetRepository, times(1)).save(any(IlliquidAsset.class));
    }

    @Test
    public void testUpdateIlliquidAsset_AssetExists_UpdatesSuccessfully() {
        // Given
        when(illiquidAssetRepository.findByIdAndPortfolioId(1L, 1L))
                .thenReturn(Optional.of(illiquidAsset1));

        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Real Estate");
        updateDto.setDescription("Renovated apartment");
        updateDto.setEstimatedValue(300000.0f);

        IlliquidAsset updatedAsset = new IlliquidAsset();
        updatedAsset.setId(1L);
        updatedAsset.setName("Updated Real Estate");
        updatedAsset.setDescription("Renovated apartment");
        updatedAsset.setEstimatedValue(300000.0f);
        updatedAsset.setPortfolio(portfolio);

        when(illiquidAssetRepository.save(any(IlliquidAsset.class))).thenReturn(updatedAsset);

        // When
        Optional<IlliquidAsset> result = illiquidAssetService.updateIlliquidAsset(1L, 1L, updateDto);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Updated Real Estate", result.get().getName());
        assertEquals("Renovated apartment", result.get().getDescription());
        assertEquals(300000.0f, result.get().getEstimatedValue());
        verify(illiquidAssetRepository, times(1)).findByIdAndPortfolioId(1L, 1L);
        verify(illiquidAssetRepository, times(1)).save(any(IlliquidAsset.class));
    }

    @Test
    public void testUpdateIlliquidAsset_AssetDoesNotExist_ReturnsEmpty() {
        // Given
        when(illiquidAssetRepository.findByIdAndPortfolioId(99L, 1L))
                .thenReturn(Optional.empty());

        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Asset");
        updateDto.setDescription("Description");
        updateDto.setEstimatedValue(100000.0f);

        // When
        Optional<IlliquidAsset> result = illiquidAssetService.updateIlliquidAsset(1L, 99L, updateDto);

        // Then
        assertFalse(result.isPresent());
        verify(illiquidAssetRepository, times(1)).findByIdAndPortfolioId(99L, 1L);
        verify(illiquidAssetRepository, never()).save(any(IlliquidAsset.class));
    }

    @Test
    public void testUpdateIlliquidAsset_WrongPortfolio_ReturnsEmpty() {
        // Given
        when(illiquidAssetRepository.findByIdAndPortfolioId(1L, 99L))
                .thenReturn(Optional.empty());

        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Asset");
        updateDto.setDescription("Description");
        updateDto.setEstimatedValue(100000.0f);

        // When
        Optional<IlliquidAsset> result = illiquidAssetService.updateIlliquidAsset(99L, 1L, updateDto);

        // Then
        assertFalse(result.isPresent());
        verify(illiquidAssetRepository, times(1)).findByIdAndPortfolioId(1L, 99L);
        verify(illiquidAssetRepository, never()).save(any(IlliquidAsset.class));
    }

    @Test
    public void testDeleteIlliquidAsset_Success() {
        // Given
        doNothing().when(illiquidAssetRepository).delete(illiquidAsset1);

        // When
        illiquidAssetService.deleteIlliquidAsset(illiquidAsset1);

        // Then
        verify(illiquidAssetRepository, times(1)).delete(illiquidAsset1);
    }

    @Test
    public void testCreateIlliquidAsset_WithNullDescription_Success() {
        // Given
        IlliquidAssetDto dtoWithNullDescription = new IlliquidAssetDto();
        dtoWithNullDescription.setName("Simple Asset");
        dtoWithNullDescription.setDescription(null);
        dtoWithNullDescription.setEstimatedValue(1000.0f);

        IlliquidAsset savedAsset = new IlliquidAsset(dtoWithNullDescription);
        savedAsset.setId(4L);
        savedAsset.setPortfolio(portfolio);

        when(illiquidAssetRepository.save(any(IlliquidAsset.class))).thenReturn(savedAsset);

        // When
        IlliquidAsset result = illiquidAssetService.createIlliquidAsset(dtoWithNullDescription, portfolio);

        // Then
        assertNotNull(result);
        assertEquals("Simple Asset", result.getName());
        assertNull(result.getDescription());
        assertEquals(1000.0f, result.getEstimatedValue());
        verify(illiquidAssetRepository, times(1)).save(any(IlliquidAsset.class));
    }

    @Test
    public void testUpdateIlliquidAsset_PartialUpdate_Success() {
        // Given
        when(illiquidAssetRepository.findByIdAndPortfolioId(1L, 1L))
                .thenReturn(Optional.of(illiquidAsset1));

        IlliquidAssetDto partialUpdateDto = new IlliquidAssetDto();
        partialUpdateDto.setName("Partially Updated");
        partialUpdateDto.setDescription(illiquidAsset1.getDescription());
        partialUpdateDto.setEstimatedValue(illiquidAsset1.getEstimatedValue());

        IlliquidAsset updatedAsset = new IlliquidAsset();
        updatedAsset.setId(1L);
        updatedAsset.setName("Partially Updated");
        updatedAsset.setDescription(illiquidAsset1.getDescription());
        updatedAsset.setEstimatedValue(illiquidAsset1.getEstimatedValue());
        updatedAsset.setPortfolio(portfolio);

        when(illiquidAssetRepository.save(any(IlliquidAsset.class))).thenReturn(updatedAsset);

        // When
        Optional<IlliquidAsset> result = illiquidAssetService.updateIlliquidAsset(1L, 1L, partialUpdateDto);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Partially Updated", result.get().getName());
        assertEquals(illiquidAsset1.getDescription(), result.get().getDescription());
        assertEquals(illiquidAsset1.getEstimatedValue(), result.get().getEstimatedValue());
        verify(illiquidAssetRepository, times(1)).save(any(IlliquidAsset.class));
    }
}

