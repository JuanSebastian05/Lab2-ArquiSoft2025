package com.petstore.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void generate_and_validate_token() {
        String email = "user@example.com";
        String token = jwtUtil.generateToken(email);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
        assertThat(jwtUtil.getEmailFromToken(token)).isEqualTo(email);
    }

    @Test
    void validateToken_returnsFalse_forInvalidToken() {
        String invalid = "this.is.not.a.jwt";
        assertThat(jwtUtil.validateToken(invalid)).isFalse();
    }
}
