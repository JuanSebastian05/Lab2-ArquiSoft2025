package com.petstore.backend.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import com.petstore.backend.repository.CategoryRepository;
import com.petstore.backend.repository.ProductRepository;
import com.petstore.backend.repository.PromotionRepository;
import com.petstore.backend.repository.UserRepository;
import com.petstore.backend.service.AuthService;
import com.petstore.backend.service.PromotionService;

/**
 * Test de GraphQLResolver enfocado en manejo de excepciones
 */
class GraphQLResolverExceptionTest {

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
    }

    @AfterEach
    void cleanup() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void promotions_whenRepositoryThrows_returnsEmptyList() {
        when(promotionRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        
        List<com.petstore.backend.entity.Promotion> result = resolver.promotions();
        
        assertThat(result).isEmpty();
    }

    @Test
    void promotionsActive_whenServiceThrows_returnsEmptyList() {
        when(promotionService.getAllActivePromotionsEntities()).thenThrow(new RuntimeException("Service error"));
        
        List<com.petstore.backend.entity.Promotion> result = resolver.promotionsActive();
        
        assertThat(result).isEmpty();
    }

    @Test
    void promotionsExpired_whenServiceThrows_returnsEmptyList() {
        when(promotionService.getAllExpiredPromotionsEntities()).thenThrow(new RuntimeException("Service error"));
        
        List<com.petstore.backend.entity.Promotion> result = resolver.promotionsExpired();
        
        assertThat(result).isEmpty();
    }

    @Test
    void promotionsScheduled_whenServiceThrows_returnsEmptyList() {
        when(promotionService.getAllScheduledPromotionsEntities()).thenThrow(new RuntimeException("Service error"));
        
        List<com.petstore.backend.entity.Promotion> result = resolver.promotionsScheduled();
        
        assertThat(result).isEmpty();
    }

    @Test
    void promotionsByStatus_whenServiceThrows_returnsEmptyList() {
        when(promotionService.getPromotionsByStatusEntities("ACTIVE")).thenThrow(new RuntimeException("Service error"));
        
        List<com.petstore.backend.entity.Promotion> result = resolver.promotionsByStatus("ACTIVE");
        
        assertThat(result).isEmpty();
    }

    @Test
    void promotionsByCategory_whenServiceThrows_returnsEmptyList() {
        when(promotionService.getPromotionsByCategoryEntities(1)).thenThrow(new RuntimeException("Service error"));
        
        List<com.petstore.backend.entity.Promotion> result = resolver.promotionsByCategory(1);
        
        assertThat(result).isEmpty();
    }

    @Test
    void promotion_whenRepositoryThrows_returnsNull() {
        when(promotionRepository.findById(1)).thenThrow(new RuntimeException("DB error"));
        
        com.petstore.backend.entity.Promotion result = resolver.promotion(1);
        
        assertThat(result).isNull();
    }

    @Test
    void categories_whenRepositoryThrows_returnsEmptyList() {
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        
        List<com.petstore.backend.entity.Category> result = resolver.categories();
        
        assertThat(result).isEmpty();
    }

    @Test
    void category_whenRepositoryThrows_returnsNull() {
        when(categoryRepository.findById(1)).thenThrow(new RuntimeException("DB error"));
        
        com.petstore.backend.entity.Category result = resolver.category(1);
        
        assertThat(result).isNull();
    }

    @Test
    void products_whenRepositoryThrows_returnsEmptyList() {
        when(productRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        
        List<com.petstore.backend.entity.Product> result = resolver.products();
        
        assertThat(result).isEmpty();
    }

    @Test
    void productsByCategory_whenRepositoryThrows_returnsEmptyList() {
        when(productRepository.findByCategoryCategoryId(1)).thenThrow(new RuntimeException("DB error"));
        
        List<com.petstore.backend.entity.Product> result = resolver.productsByCategory(1);
        
        assertThat(result).isEmpty();
    }

    @Test
    void product_whenRepositoryThrows_returnsNull() {
        when(productRepository.findById(1)).thenThrow(new RuntimeException("DB error"));
        
        com.petstore.backend.entity.Product result = resolver.product(1);
        
        assertThat(result).isNull();
    }

    @Test
    void promotionProducts_whenRepositoryThrows_returnsEmptyList() {
        com.petstore.backend.entity.Promotion promo = org.mockito.Mockito.mock(com.petstore.backend.entity.Promotion.class);
        when(promo.getPromotionId()).thenReturn(1);
        when(productRepository.findByPromotionPromotionId(1)).thenThrow(new RuntimeException("DB error"));
        
        List<com.petstore.backend.entity.Product> result = resolver.promotionProducts(promo);
        
        assertThat(result).isEmpty();
    }

    @Test
    void categoryPromotions_whenServiceThrows_returnsEmptyList() {
        com.petstore.backend.entity.Category cat = org.mockito.Mockito.mock(com.petstore.backend.entity.Category.class);
        when(cat.getCategoryId()).thenReturn(1);
        when(promotionService.getPromotionsByCategoryEntities(1)).thenThrow(new RuntimeException("Service error"));
        
        List<com.petstore.backend.entity.Promotion> result = resolver.categoryPromotions(cat);
        
        assertThat(result).isEmpty();
    }

    @Test
    void categoryProducts_whenRepositoryThrows_returnsEmptyList() {
        com.petstore.backend.entity.Category cat = org.mockito.Mockito.mock(com.petstore.backend.entity.Category.class);
        when(cat.getCategoryId()).thenReturn(1);
        when(productRepository.findByCategoryCategoryId(1)).thenThrow(new RuntimeException("DB error"));
        
        List<com.petstore.backend.entity.Product> result = resolver.categoryProducts(cat);
        
        assertThat(result).isEmpty();
    }
}
