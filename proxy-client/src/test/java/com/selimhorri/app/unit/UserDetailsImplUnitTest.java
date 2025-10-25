package com.selimhorri.app.unit;

import com.selimhorri.app.business.user.model.UserDetailsImpl;
import com.selimhorri.app.business.user.model.CredentialDto;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public class UserDetailsImplUnitTest {

    @Test
    void testGetUsername() {
        CredentialDto credential = getCredential("user1", "pass", "ROLE_USER");
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertEquals("user1", userDetails.getUsername());
    }

    @Test
    void testGetPassword() {
        CredentialDto credential = getCredential("user1", "pass123", "ROLE_USER");
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertEquals("pass123", userDetails.getPassword());
    }

    @Test
    void testIsEnabled() {
        CredentialDto credential = getCredential("user1", "pass", "ROLE_USER");
        credential.setIsEnabled(true);
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testGetAuthorities() {
        CredentialDto credential = getCredential("user1", "pass", "ROLE_ADMIN");
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void testAccountNonExpiredAndLocked() {
        CredentialDto credential = getCredential("user1", "pass", "ROLE_USER");
        credential.setIsAccountNonExpired(true);
        credential.setIsAccountNonLocked(true);
        credential.setIsCredentialsNonExpired(true);
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    // Helper to create a CredentialDto with default values
    private CredentialDto getCredential(String username, String password, String role) {
        CredentialDto credential = new CredentialDto();
        credential.setUsername(username);
        credential.setPassword(password);
        credential.setRoleBasedAuthority(com.selimhorri.app.business.user.model.RoleBasedAuthority.valueOf(role));
        credential.setIsEnabled(true);
        credential.setIsAccountNonExpired(true);
        credential.setIsAccountNonLocked(true);
        credential.setIsCredentialsNonExpired(true);
        return credential;
    }
}
