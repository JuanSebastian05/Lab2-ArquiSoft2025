package com.petstore.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired(required = false)
    private SecurityFilterChain securityFilterChain;

    @Test
    @DisplayName("SecurityFilterChain bean se carga en el contexto")
    void securityFilterChain_isLoaded() {
        assertThat(securityFilterChain).isNotNull();
    }
}
