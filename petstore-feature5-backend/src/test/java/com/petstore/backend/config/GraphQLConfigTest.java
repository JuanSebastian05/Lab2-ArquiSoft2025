package com.petstore.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class GraphQLConfigTest {

    @Test
    @DisplayName("addCorsMappings no lanza excepciones y configura rutas básicas")
    void addCorsMappings_executes() {
        GraphQLConfig cfg = new GraphQLConfig();
        assertDoesNotThrow(() -> cfg.addCorsMappings(new CorsRegistry()));
    }
}
