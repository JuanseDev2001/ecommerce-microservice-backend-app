package com.selimhorri.app.integration;

import com.selimhorri.app.jwt.util.impl.JwtUtilImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilImplIntegrationTest {

    // Dummy UserDetails for testing
    static class DummyUserDetails implements org.springframework.security.core.userdetails.UserDetails {
        private final String username;
        DummyUserDetails(String username) { this.username = username; }
        @Override public String getUsername() { return username; }
        @Override public String getPassword() { return "pass"; }
        @Override public boolean isAccountNonExpired() { return true; }
        @Override public boolean isAccountNonLocked() { return true; }
        @Override public boolean isCredentialsNonExpired() { return true; }
        @Override public boolean isEnabled() { return true; }
        @Override public java.util.Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() { return java.util.Collections.emptyList(); }
    }

    @Test
    void testTokenLifecycle() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
    UserDetails user = new DummyUserDetails("integrationUser");
        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertEquals("integrationUser", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token, user));
    }

    @Test
    void testTokenInvalidForOtherUser() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
    UserDetails user = new DummyUserDetails("integrationUser");
    String token = jwtUtil.generateToken(user);
    UserDetails other = new DummyUserDetails("otherUser");
        assertFalse(jwtUtil.validateToken(token, other));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        JwtUtilImpl jwtUtil = new JwtUtilImpl() {
            @Override
            public String generateToken(UserDetails userDetails) {
                // Override to set short expiration for test
                java.util.Map<String, Object> claims = new java.util.HashMap<>();
                return io.jsonwebtoken.Jwts.builder()
                        .setClaims(claims)
                        .setSubject(userDetails.getUsername())
                        .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                        .setExpiration(new java.util.Date(System.currentTimeMillis() + 100))
                        .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, "secret")
                        .compact();
            }
        };
        UserDetails user = new DummyUserDetails("expUser");
        String token = jwtUtil.generateToken(user);
        Thread.sleep(150); // Wait for expiration
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtUtil.validateToken(token, user));
    }

    @Test
    void testExtractClaims() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
    UserDetails user = new DummyUserDetails("claimsUser");
        String token = jwtUtil.generateToken(user);
        String subject = jwtUtil.extractClaims(token, claims -> claims.getSubject());
        assertEquals("claimsUser", subject);
    }

    @Test
    void testExtractExpirationNotNull() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
    UserDetails user = new DummyUserDetails("expUser");
        String token = jwtUtil.generateToken(user);
        assertNotNull(jwtUtil.extractExpiration(token));
    }
}
