package com.petstore.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.petstore.backend.dto.LoginResponse;
import com.petstore.backend.entity.Role;
import com.petstore.backend.entity.User;
import com.petstore.backend.repository.UserRepository;
import com.petstore.backend.util.JwtUtil;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildMarketingAdmin(String email, String password) {
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("Marketing Admin");

        User u = new User();
        u.setUserId(10);
        u.setUserName("Alice");
        u.setEmail(email);
        u.setPassword(password);
        u.setRole(role);
        return u;
    }

    @Test
    void authenticateMarketingAdmin_success_returnsLoginResponseWithToken() {
        String email = "alice@example.com";
        String password = "password123";
        User user = buildMarketingAdmin(email, password);

        when(userRepository.findMarketingAdminByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(email)).thenReturn("token-123");

        LoginResponse response = authService.authenticateMarketingAdmin(email, password);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isEqualTo("token-123");
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getUserName()).isEqualTo("Alice");
        assertThat(response.getRole()).isEqualTo("Marketing Admin");
    }

    @Test
    void authenticateMarketingAdmin_wrongPassword_throws() {
        String email = "alice@example.com";
        User user = buildMarketingAdmin(email, "password123");

        when(userRepository.findMarketingAdminByEmail(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.authenticateMarketingAdmin(email, "bad"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Contraseña incorrecta");
    }

    @Test
    void validateToken_delegatesToJwtUtil() {
        when(jwtUtil.validateToken("ok")).thenReturn(true);
        when(jwtUtil.validateToken("bad")).thenReturn(false);
        assertThat(authService.validateToken("ok")).isTrue();
        assertThat(authService.validateToken("bad")).isFalse();
    }

    @Test
    void getUserFromToken_buildsInfoMap() {
        String email = "alice@example.com";
        User user = buildMarketingAdmin(email, "password123");

        when(jwtUtil.validateToken("t")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("t")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Map<String, Object> info = authService.getUserFromToken("t");

        assertThat(info.get("email")).isEqualTo(email);
        assertThat(info.get("userName")).isEqualTo("Alice");
        assertThat(info.get("role")).isEqualTo("Marketing Admin");
    }

    @Test
    void login_success_returnsMapWithTokenAndUser() {
        String email = "alice@example.com";
        User user = buildMarketingAdmin(email, "password123");

        when(userRepository.findMarketingAdminByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(email)).thenReturn("tt");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Map<String, Object> map = authService.login(email, "password123");
        assertThat(map.get("success")).isEqualTo(true);
        assertThat(map.get("token")).isEqualTo("tt");

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) map.get("user");
        assertThat(userMap.get("email")).isEqualTo(email);
    }

    @Test
    void login_failure_returnsDefaultMap() {
        when(userRepository.findMarketingAdminByEmail("bad@example.com")).thenReturn(Optional.empty());
        Map<String, Object> map = authService.login("bad@example.com", "x");
        assertThat(map.get("success")).isEqualTo(false);
        assertThat(map.get("token")).isEqualTo("");
        assertThat(map.get("user")).isInstanceOf(HashMap.class);
    }

    @Test
    void loadUserByUsername_success_returnsUserDetails() {
        String email = "alice@example.com";
        User user = buildMarketingAdmin(email, "password123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails ud = authService.loadUserByUsername(email);
        assertThat(ud.getUsername()).isEqualTo(email);
        assertThat(ud.getPassword()).isEqualTo("password123");
        assertThat(ud.getAuthorities()).hasSize(1);
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.loadUserByUsername("none@example.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void isMarketingAdmin_returnsTrue_whenFound() {
        when(userRepository.findMarketingAdminByEmail("admin@example.com"))
                .thenReturn(Optional.of(buildMarketingAdmin("admin@example.com", "x")));
        assertThat(authService.isMarketingAdmin("admin@example.com")).isTrue();
    }

    @Test
    void isMarketingAdmin_returnsFalse_whenNotFound() {
        when(userRepository.findMarketingAdminByEmail("noone@example.com")).thenReturn(Optional.empty());
        assertThat(authService.isMarketingAdmin("noone@example.com")).isFalse();
    }

    @Test
    void getUserFromToken_invalidToken_throws() {
        when(jwtUtil.validateToken("bad")).thenReturn(false);
        assertThatThrownBy(() -> authService.getUserFromToken("bad"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Token inválido");
    }

    @Test
    void authenticateMarketingAdmin_notFound_throws() {
        when(userRepository.findMarketingAdminByEmail("none@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.authenticateMarketingAdmin("none@example.com", "x"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void validateToken_exception_returnsFalse() {
        when(jwtUtil.validateToken("bad")).thenThrow(new RuntimeException("Invalid"));
        assertThat(authService.validateToken("bad")).isFalse();
    }
}

