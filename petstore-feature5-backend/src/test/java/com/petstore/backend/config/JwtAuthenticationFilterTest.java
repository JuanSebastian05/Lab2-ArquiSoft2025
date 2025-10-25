package com.petstore.backend.config;

import com.petstore.backend.service.AuthService;
import com.petstore.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Coloca autenticación en el contexto cuando el JWT es válido")
    void setsAuthenticationWhenTokenValid() throws ServletException, IOException {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        AuthService authService = Mockito.mock(AuthService.class);

        Mockito.when(jwtUtil.validateToken("good")).thenReturn(true);
        Mockito.when(jwtUtil.extractEmail("good")).thenReturn("user@petstore.com");
        UserDetails userDetails = User.withUsername("user@petstore.com").password("x").authorities("ROLE_USER").build();
        Mockito.when(authService.loadUserByUsername(anyString())).thenReturn(userDetails);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, authService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@petstore.com");
    }

    @Test
    @DisplayName("No autentica cuando el token es inválido")
    void doesNotAuthenticateWhenInvalid() throws ServletException, IOException {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        AuthService authService = Mockito.mock(AuthService.class);

        Mockito.when(jwtUtil.validateToken("bad")).thenReturn(false);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, authService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
