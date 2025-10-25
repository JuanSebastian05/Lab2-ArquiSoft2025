package com.petstore.backend.graphql;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.petstore.backend.repository.CategoryRepository;
import com.petstore.backend.repository.ProductRepository;
import com.petstore.backend.repository.PromotionRepository;
import com.petstore.backend.repository.UserRepository;
import com.petstore.backend.service.AuthService;
import com.petstore.backend.service.PromotionService;

/**
 * Test de GraphQLResolver enfocado en autorización y seguridad
 */
class GraphQLResolverAuthTest {

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

    private void setUnauthenticated() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        SecurityContext sc = Mockito.mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

    private void setAnonymousUser() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("anonymousUser");
        SecurityContext sc = Mockito.mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

    private PromotionDTO buildDto() {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionName("Test Promo");
        dto.setDescription("Test Description");
        dto.setDiscountPercentage(BigDecimal.valueOf(15));
        dto.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        dto.setEndDate(LocalDateTime.of(2024, 1, 31, 0, 0));
        CategoryDTO cat = new CategoryDTO();
        cat.setCategoryId(1);
        dto.setCategory(cat);
        return dto;
    }

    @Test
    void currentUser_whenNotAuthenticated_throwsRuntimeException() {
        setUnauthenticated();
        assertThatThrownBy(() -> resolver.currentUser())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void currentUser_whenAnonymousUser_throwsRuntimeException() {
        setAnonymousUser();
        assertThatThrownBy(() -> resolver.currentUser())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void createPromotion_whenNotAuthenticated_throwsRuntimeException() {
        setUnauthenticated();
        PromotionDTO dto = buildDto();
        
        assertThatThrownBy(() -> resolver.createPromotion(dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void updatePromotion_whenNotAuthenticated_throwsRuntimeException() {
        setUnauthenticated();
        PromotionDTO dto = buildDto();
        
        assertThatThrownBy(() -> resolver.updatePromotion(1, dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void deletePromotion_whenNotAuthenticated_throwsRuntimeException() {
        setUnauthenticated();
        
        assertThatThrownBy(() -> resolver.deletePromotion(1))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void createPromotion_whenAnonymousUser_throwsRuntimeException() {
        setAnonymousUser();
        PromotionDTO dto = buildDto();
        
        assertThatThrownBy(() -> resolver.createPromotion(dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void updatePromotion_whenAnonymousUser_throwsRuntimeException() {
        setAnonymousUser();
        PromotionDTO dto = buildDto();
        
        assertThatThrownBy(() -> resolver.updatePromotion(1, dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void deletePromotion_whenAnonymousUser_throwsRuntimeException() {
        setAnonymousUser();
        
        assertThatThrownBy(() -> resolver.deletePromotion(1))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    void currentUser_whenNoAuthentication_throwsRuntimeException() {
        // No security context at all
        SecurityContextHolder.clearContext();
        
        assertThatThrownBy(() -> resolver.currentUser())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authentication required");
    }
}
