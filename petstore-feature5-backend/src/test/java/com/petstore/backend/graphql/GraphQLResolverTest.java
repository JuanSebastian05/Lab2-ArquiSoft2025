package com.petstore.backend.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
