package com.camicompany.PsyCare.utils;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils("test-secret", "test-issuer");
    }

    private Authentication buildAuth() {


        User user = new User(
                "camila",
                "password1234",
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("READ")
                )
        );
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }


    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        Authentication auth = buildAuth();

        String token = jwtUtils.generateToken(auth);

        assertNotNull(token);

        DecodedJWT decoded = jwtUtils.validateToken(token);

        assertNotNull(decoded);
        assertEquals("camila", jwtUtils.extractUsername(decoded));
    }

    @Test
    void shouldContainAuthoritiesInToken() {
        Authentication auth = buildAuth();

        String token = jwtUtils.generateToken(auth);
        DecodedJWT decoded = jwtUtils.validateToken(token);

        String authorities = jwtUtils
                .getSpecificClaim(decoded, "authorities")
                .asString();

        assertNotNull(authorities);
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("READ"));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalid() {
        String invalidToken = "esto.no.es.un.jwt";

        var ex = assertThrows(JWTVerificationException.class,
                () -> jwtUtils.validateToken(invalidToken));
        assertEquals("Invalid token. Not authenticated", ex.getMessage());
    }


    @Test
    void shouldThrowExceptionWhenTokenIsTampered() {
        Authentication auth = buildAuth();

        String token = jwtUtils.generateToken(auth);

        String tamperedToken = token + "abc";

       var ex =  assertThrows(JWTVerificationException.class,
                () -> jwtUtils.validateToken(tamperedToken));

       assertEquals("Invalid token. Not authenticated", ex.getMessage());
    }

    @Test
    void shouldFailValidationWithDifferentSecret() {
        Authentication auth = buildAuth();

        String token = jwtUtils.generateToken(auth);

        JwtUtils otherJwtUtils = new JwtUtils("other-secret", "test-issuer");

        var ex = assertThrows(JWTVerificationException.class,
                () -> otherJwtUtils.validateToken(token));

        assertEquals("Invalid token. Not authenticated", ex.getMessage());

    }


    @Test
    void shouldFailValidationWithDifferentIssuer() {
        Authentication auth = buildAuth();

        String token = jwtUtils.generateToken(auth);

        JwtUtils otherJwtUtils = new JwtUtils("test-secret", "other-issuer");

        assertThrows(Exception.class,
                () -> otherJwtUtils.validateToken(token));
    }

    @Test
    void shouldExtractUsernameCorrectly() {
        Authentication auth = buildAuth();

        String token = jwtUtils.generateToken(auth);
        DecodedJWT decoded = jwtUtils.validateToken(token);
        String username = jwtUtils.extractUsername(decoded);
        assertEquals("camila", username);
    }

    @Test
    void shouldReturnAllClaims() {
        Authentication auth = buildAuth();

        String token = jwtUtils.generateToken(auth);
        DecodedJWT decoded = jwtUtils.validateToken(token);

        Map<String, ?> claims = jwtUtils.returnAllClaims(decoded);

        assertNotNull(claims);
        assertTrue(claims.containsKey("authorities"));
    }
}
