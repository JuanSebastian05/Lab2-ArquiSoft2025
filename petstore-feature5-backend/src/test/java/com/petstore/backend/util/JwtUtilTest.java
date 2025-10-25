package com.petstore.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=TestSecretKeyForJWTMustBe32CharsMin",
    "jwt.expiration=86400000"
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

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
