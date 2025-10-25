package com.petstore.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CorsConfig.class, properties = {
        "app.cors.allowed-origins=*",
        "app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS",
        "app.cors.allowed-headers=*",
        "app.cors.allow-credentials=true",
        "app.cors.max-age=3600"
})
class CorsConfigTest {

    @Autowired
    private CorsConfigurationSource source;

    @Test
    @DisplayName("Genera configuración CORS con métodos y headers esperados")
    void corsConfigurationCreated() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/any");
        CorsConfiguration cfg = source.getCorsConfiguration(request);
        assertThat(cfg).isNotNull();
        assertThat(cfg.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(cfg.getAllowedHeaders()).isNotEmpty();
        assertThat(cfg.getMaxAge()).isEqualTo(3600);
    }
}
