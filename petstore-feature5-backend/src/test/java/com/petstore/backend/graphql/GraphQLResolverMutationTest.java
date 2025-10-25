package com.petstore.backend.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.petstore.backend.dto.CategoryDTO;
import com.petstore.backend.dto.PromotionDTO;
import com.petstore.backend.entity.Promotion;
import com.petstore.backend.repository.CategoryRepository;
import com.petstore.backend.repository.ProductRepository;
import com.petstore.backend.repository.PromotionRepository;
import com.petstore.backend.repository.UserRepository;
import com.petstore.backend.service.AuthService;
import com.petstore.backend.service.PromotionService;

class GraphQLResolverMutationTest {

    @Mock private PromotionService promotionService;
    @Mock private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PromotionRepository promotionRepository;

    @InjectMocks private GraphQLResolver resolver;

    AutoCloseable mocks;

    @BeforeEach
    void init() {
        mocks = MockitoAnnotations.openMocks(this);
        // Set authenticated security context
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        Mockito.when(auth.getName()).thenReturn("alice@example.com");
        SecurityContext sc = Mockito.mock(SecurityContext.class);
        Mockito.when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

    @AfterEach
    void cleanup() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    private PromotionDTO buildDto(Integer categoryId) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionName("Promo");
        dto.setDescription("Desc");
        dto.setDiscountPercentage(BigDecimal.valueOf(10));
        dto.setStartDate(LocalDateTime.of(2024,1,1,0,0));
        dto.setEndDate(LocalDateTime.of(2024,1,10,0,0));
        if (categoryId != null) {
            CategoryDTO cat = new CategoryDTO();
            cat.setCategoryId(categoryId);
            dto.setCategory(cat);
        }
        return dto;
    }

    @Test
    void createPromotion_authenticated_callsService_andReturnsEntity() {
        Promotion expected = new Promotion();
        expected.setPromotionId(1);
        when(promotionService.createPromotion(
            any(), any(), any(LocalDate.class), any(LocalDate.class), any(Double.class), any(Integer.class), any(Integer.class), any()
        )).thenReturn(expected);

        PromotionDTO dto = buildDto(5);
        Promotion result = resolver.createPromotion(dto);
        assertThat(result).isNotNull();
        assertThat(result.getPromotionId()).isEqualTo(1);
    }

    @Test
    void updatePromotion_authenticated_callsService_andReturnsEntity() {
        Promotion updated = new Promotion();
        updated.setPromotionId(9);
        when(promotionService.updatePromotion(
            eq(9), any(), any(), any(LocalDate.class), any(LocalDate.class), any(Double.class), any(Integer.class), any(Integer.class), any()
        )).thenReturn(updated);

        PromotionDTO dto = buildDto(7);
        Promotion res = resolver.updatePromotion(9, dto);
        assertThat(res).isNotNull();
        assertThat(res.getPromotionId()).isEqualTo(9);
    }

    @Test
    void deletePromotion_whenServiceReturnsTrue_returnsTrue() {
        when(promotionService.deletePromotion(7)).thenReturn(true);
        assertThat(resolver.deletePromotion(7)).isTrue();
    }

    @Test
    void deletePromotion_whenServiceReturnsFalse_throws() {
        when(promotionService.deletePromotion(8)).thenReturn(false);
        assertThatThrownBy(() -> resolver.deletePromotion(8))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void login_successfulAuthentication_returnsLoginResponse() {
        // Arrange
        com.petstore.backend.dto.LoginResponse mockResponse = new com.petstore.backend.dto.LoginResponse();
        mockResponse.setSuccess(true);
        mockResponse.setToken("jwt-token-123");
        mockResponse.setMessage("Login successful");
        
        when(authService.authenticateMarketingAdmin("admin@test.com", "password123"))
            .thenReturn(mockResponse);

        // Act
        com.petstore.backend.dto.LoginResponse result = resolver.login("admin@test.com", "password123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getToken()).isEqualTo("jwt-token-123");
    }

    @Test
    void login_failedAuthentication_returnsErrorResponse() {
        // Arrange
        when(authService.authenticateMarketingAdmin("wrong@test.com", "wrongpass"))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // Act
        com.petstore.backend.dto.LoginResponse result = resolver.login("wrong@test.com", "wrongpass");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Invalid credentials");
    }

    @Test
    void createPromotion_withoutCategory_stillWorks() {
        Promotion expected = new Promotion();
        expected.setPromotionId(2);
        when(promotionService.createPromotion(
            any(), any(), any(LocalDate.class), any(LocalDate.class), any(Double.class), any(Integer.class), any(Integer.class), eq(null)
        )).thenReturn(expected);

        PromotionDTO dto = buildDto(null); // No category
        Promotion result = resolver.createPromotion(dto);
        assertThat(result).isNotNull();
        assertThat(result.getPromotionId()).isEqualTo(2);
    }

    @Test
    void updatePromotion_withoutCategory_stillWorks() {
        Promotion updated = new Promotion();
        updated.setPromotionId(10);
        when(promotionService.updatePromotion(
            eq(10), any(), any(), any(LocalDate.class), any(LocalDate.class), any(Double.class), any(Integer.class), any(Integer.class), eq(null)
        )).thenReturn(updated);

        PromotionDTO dto = buildDto(null); // No category
        Promotion res = resolver.updatePromotion(10, dto);
        assertThat(res).isNotNull();
        assertThat(res.getPromotionId()).isEqualTo(10);
    }

    @Test
    void createPromotion_whenServiceThrows_propagatesException() {
        when(promotionService.createPromotion(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database error"));

        PromotionDTO dto = buildDto(1);
        assertThatThrownBy(() -> resolver.createPromotion(dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to create promotion");
    }

    @Test
    void updatePromotion_whenServiceThrows_propagatesException() {
        when(promotionService.updatePromotion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Not found"));

        PromotionDTO dto = buildDto(1);
        assertThatThrownBy(() -> resolver.updatePromotion(99, dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to update promotion");
    }
}
