package com.petstore.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConfigTest {

    @Test
    @DisplayName("PasswordEncoder encripta y valida correctamente")
    void passwordEncoder_works() {
        PasswordConfig config = new PasswordConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        String raw = "s3cr3t";
        String hash = encoder.encode(raw);
        assertThat(encoder.matches(raw, hash)).isTrue();
        assertThat(encoder.matches("other", hash)).isFalse();
    }
}
