package com.petstore.backend.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.petstore.backend.entity.Promotion;
import com.petstore.backend.entity.User;
import com.petstore.backend.repository.CategoryRepository;
import com.petstore.backend.repository.ProductRepository;
import com.petstore.backend.repository.PromotionRepository;
import com.petstore.backend.repository.UserRepository;
import com.petstore.backend.service.AuthService;
import com.petstore.backend.service.PromotionService;

class GraphQLResolverTest {

    @Mock
    private PromotionService promotionService;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private GraphQLResolver resolver;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clear security context to avoid cross-test pollution
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void health_returnsNonEmptyString() {
        String s = resolver.health();
        assertThat(s).contains("GraphQL API is running");
    }

    @Test
    void promotions_returnsListFromRepository() {
        Promotion p = org.mockito.Mockito.mock(Promotion.class);
        when(promotionRepository.findAll()).thenReturn(List.of(p));

        List<Promotion> result = resolver.promotions();

        assertThat(result).hasSize(1);
        verify(promotionRepository).findAll();
    }

    @Test
    void promotionsActive_callsServiceAndReturnsList() {
        Promotion p = org.mockito.Mockito.mock(Promotion.class);
        when(promotionService.getAllActivePromotionsEntities()).thenReturn(List.of(p));

        List<Promotion> result = resolver.promotionsActive();

        assertThat(result).hasSize(1);
        verify(promotionService).getAllActivePromotionsEntities();
    }

    @Test
    void currentUser_requiresAuthentication_andReturnsUser() {
        // Arrange authentication
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("alice@example.com");

        SecurityContext sc = org.mockito.Mockito.mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = org.mockito.Mockito.mock(User.class);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        // Act
        User result = resolver.currentUser();

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findByEmail("alice@example.com");
    }

    @Test
    void promotion_byId_returnsPromotionOrNull() {
        Promotion p = org.mockito.Mockito.mock(Promotion.class);
        when(promotionRepository.findById(1)).thenReturn(Optional.of(p));

        Promotion res = resolver.promotion(1);
        assertThat(res).isNotNull();

        when(promotionRepository.findById(2)).thenReturn(Optional.empty());
        Promotion res2 = resolver.promotion(2);
        assertThat(res2).isNull();
    }

    @Test
    void promotionsExpired_callsServiceAndReturnsList() {
        Promotion p = org.mockito.Mockito.mock(Promotion.class);
        when(promotionService.getAllExpiredPromotionsEntities()).thenReturn(List.of(p));

        List<Promotion> result = resolver.promotionsExpired();

        assertThat(result).hasSize(1);
        verify(promotionService).getAllExpiredPromotionsEntities();
    }

    @Test
    void promotionsScheduled_callsServiceAndReturnsList() {
        Promotion p = org.mockito.Mockito.mock(Promotion.class);
        when(promotionService.getAllScheduledPromotionsEntities()).thenReturn(List.of(p));

        List<Promotion> result = resolver.promotionsScheduled();

        assertThat(result).hasSize(1);
        verify(promotionService).getAllScheduledPromotionsEntities();
    }

    @Test
    void promotionsByStatus_callsServiceWithStatusName() {
        Promotion p = org.mockito.Mockito.mock(Promotion.class);
        when(promotionService.getPromotionsByStatusEntities("ACTIVE")).thenReturn(List.of(p));

        List<Promotion> result = resolver.promotionsByStatus("ACTIVE");

        assertThat(result).hasSize(1);
        verify(promotionService).getPromotionsByStatusEntities("ACTIVE");
    }

    @Test
    void promotionsByCategory_callsServiceWithCategoryId() {
        Promotion p = org.mockito.Mockito.mock(Promotion.class);
        when(promotionService.getPromotionsByCategoryEntities(5)).thenReturn(List.of(p));

        List<Promotion> result = resolver.promotionsByCategory(5);

        assertThat(result).hasSize(1);
        verify(promotionService).getPromotionsByCategoryEntities(5);
    }

    @Test
    void categories_returnsListFromRepository() {
        com.petstore.backend.entity.Category cat = org.mockito.Mockito.mock(com.petstore.backend.entity.Category.class);
        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        List<com.petstore.backend.entity.Category> result = resolver.categories();

        assertThat(result).hasSize(1);
        verify(categoryRepository).findAll();
    }

    @Test
    void category_byId_returnsCategoryOrNull() {
        com.petstore.backend.entity.Category cat = org.mockito.Mockito.mock(com.petstore.backend.entity.Category.class);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));

        com.petstore.backend.entity.Category res = resolver.category(1);
        assertThat(res).isNotNull();

        when(categoryRepository.findById(2)).thenReturn(Optional.empty());
        com.petstore.backend.entity.Category res2 = resolver.category(2);
        assertThat(res2).isNull();
    }

    @Test
    void products_returnsListFromRepository() {
        com.petstore.backend.entity.Product prod = org.mockito.Mockito.mock(com.petstore.backend.entity.Product.class);
        when(productRepository.findAll()).thenReturn(List.of(prod));

        List<com.petstore.backend.entity.Product> result = resolver.products();

        assertThat(result).hasSize(1);
        verify(productRepository).findAll();
    }

    @Test
    void productsByCategory_callsRepositoryWithCategoryId() {
        com.petstore.backend.entity.Product prod = org.mockito.Mockito.mock(com.petstore.backend.entity.Product.class);
        when(productRepository.findByCategoryCategoryId(3)).thenReturn(List.of(prod));

        List<com.petstore.backend.entity.Product> result = resolver.productsByCategory(3);

        assertThat(result).hasSize(1);
        verify(productRepository).findByCategoryCategoryId(3);
    }

    @Test
    void product_byId_returnsProductOrNull() {
        com.petstore.backend.entity.Product prod = org.mockito.Mockito.mock(com.petstore.backend.entity.Product.class);
        when(productRepository.findById(1)).thenReturn(Optional.of(prod));

        com.petstore.backend.entity.Product res = resolver.product(1);
        assertThat(res).isNotNull();

        when(productRepository.findById(2)).thenReturn(Optional.empty());
        com.petstore.backend.entity.Product res2 = resolver.product(2);
        assertThat(res2).isNull();
    }

    @Test
    void promotionProducts_resolvesProductsForPromotion() {
        Promotion promo = org.mockito.Mockito.mock(Promotion.class);
        when(promo.getPromotionId()).thenReturn(10);
        
        com.petstore.backend.entity.Product prod = org.mockito.Mockito.mock(com.petstore.backend.entity.Product.class);
        when(productRepository.findByPromotionPromotionId(10)).thenReturn(List.of(prod));

        List<com.petstore.backend.entity.Product> result = resolver.promotionProducts(promo);

        assertThat(result).hasSize(1);
        verify(productRepository).findByPromotionPromotionId(10);
    }

    @Test
    void categoryPromotions_resolvesPromotionsForCategory() {
        com.petstore.backend.entity.Category cat = org.mockito.Mockito.mock(com.petstore.backend.entity.Category.class);
        when(cat.getCategoryId()).thenReturn(7);
        
        Promotion promo = org.mockito.Mockito.mock(Promotion.class);
        when(promotionService.getPromotionsByCategoryEntities(7)).thenReturn(List.of(promo));

        List<Promotion> result = resolver.categoryPromotions(cat);

        assertThat(result).hasSize(1);
        verify(promotionService).getPromotionsByCategoryEntities(7);
    }

    @Test
    void categoryProducts_resolvesProductsForCategory() {
        com.petstore.backend.entity.Category cat = org.mockito.Mockito.mock(com.petstore.backend.entity.Category.class);
        when(cat.getCategoryId()).thenReturn(8);
        
        com.petstore.backend.entity.Product prod = org.mockito.Mockito.mock(com.petstore.backend.entity.Product.class);
        when(productRepository.findByCategoryCategoryId(8)).thenReturn(List.of(prod));

        List<com.petstore.backend.entity.Product> result = resolver.categoryProducts(cat);

        assertThat(result).hasSize(1);
        verify(productRepository).findByCategoryCategoryId(8);
    }
}
