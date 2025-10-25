package com.selimhorri.app.unit;

import com.selimhorri.app.jwt.util.impl.JwtUtilImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilImplUnitTest {

    @Test
    void testExtractUsername() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
        String token = jwtUtil.generateToken(new DummyUserDetails("user1"));
        assertEquals("user1", jwtUtil.extractUsername(token));
    }

    @Test
    void testExtractExpiration() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
        String token = jwtUtil.generateToken(new DummyUserDetails("user1"));
        Date expiration = jwtUtil.extractExpiration(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testGenerateTokenNotNull() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
        String token = jwtUtil.generateToken(new DummyUserDetails("user1"));
        assertNotNull(token);
    }

    @Test
    void testValidateTokenTrue() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
        UserDetails user = new DummyUserDetails("user1");
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.validateToken(token, user));
    }

    @Test
    void testValidateTokenFalseForWrongUser() {
        JwtUtilImpl jwtUtil = new JwtUtilImpl();
        UserDetails user = new DummyUserDetails("user1");
        String token = jwtUtil.generateToken(user);
        UserDetails otherUser = new DummyUserDetails("user2");
        assertFalse(jwtUtil.validateToken(token, otherUser));
    }

    // Dummy UserDetails for testing
    static class DummyUserDetails implements UserDetails {
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
}
