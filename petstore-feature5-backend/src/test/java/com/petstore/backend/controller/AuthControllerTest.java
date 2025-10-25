package com.petstore.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.backend.dto.LoginRequest;
import com.petstore.backend.dto.LoginResponse;
import com.petstore.backend.dto.UserResponseDTO;
import com.petstore.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de JWT/Security
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest request;

    @BeforeEach
    void setup() {
        request = new LoginRequest();
        request.setEmail("admin@petstore.com");
        request.setPassword("secret");
    }

    @Test
    @DisplayName("GET /api/auth/status responde OK")
    void status_ok() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @DisplayName("POST /api/auth/login - éxito")
    void login_success() throws Exception {
        LoginResponse response = new LoginResponse("token-123", "Admin", "admin@petstore.com", "Marketing Admin");
        response.setMessage("Login exitoso");

        Mockito.when(authService.authenticateMarketingAdmin(eq("admin@petstore.com"), eq("secret")))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("token-123"));
    }

    @Test
    @DisplayName("POST /api/auth/login - fallo de autenticación")
    void login_failure() throws Exception {
        Mockito.when(authService.authenticateMarketingAdmin(any(), any()))
                .thenThrow(new RuntimeException("bad creds"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/auth/verify - token válido")
    void verify_valid() throws Exception {
        Mockito.when(authService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/api/auth/verify")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/auth/verify - token inválido")
    void verify_invalid() throws Exception {
        Mockito.when(authService.validateToken("bad-token")).thenReturn(false);

        mockMvc.perform(get("/api/auth/verify")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/me - token válido")
    void me_ok() throws Exception {
    Map<String, Object> dto = new HashMap<>();
    dto.put("email", "admin@petstore.com");
    dto.put("role", "Marketing Admin");

    Mockito.when(authService.getUserFromToken("valid")).thenReturn(dto);

    mockMvc.perform(get("/api/auth/me")
                    .header("Authorization", "Bearer valid"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("admin@petstore.com"))
            .andExpect(jsonPath("$.role").value("Marketing Admin"));
}


    @Test
    @DisplayName("GET /api/auth/me - token inválido")
    void me_invalid() throws Exception {
        Mockito.when(authService.getUserFromToken("bad"))
                .thenThrow(new RuntimeException("bad token"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized());
    }
}
