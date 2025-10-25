package com.selimhorri.app.integration;

import com.selimhorri.app.business.user.model.CredentialDto;
import com.selimhorri.app.business.user.model.RoleBasedAuthority;
import com.selimhorri.app.business.user.model.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserDetailsImplIntegrationTest {

    @Test
    void testUserDetailsImplWithCredential() {
        CredentialDto credential = CredentialDto.builder()
                .username("integrationUser")
                .password("integrationPass")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertEquals("integrationUser", userDetails.getUsername());
        assertEquals("integrationPass", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testUserDetailsImplDisabled() {
        CredentialDto credential = CredentialDto.builder()
                .username("disabledUser")
                .password("pass")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(false)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void testUserDetailsImplAccountExpired() {
        CredentialDto credential = CredentialDto.builder()
                .username("expiredUser")
                .password("pass")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(false)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertFalse(userDetails.isAccountNonExpired());
    }

    @Test
    void testUserDetailsImplAccountLocked() {
        CredentialDto credential = CredentialDto.builder()
                .username("lockedUser")
                .password("pass")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(false)
                .isCredentialsNonExpired(true)
                .build();
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void testUserDetailsImplCredentialsExpired() {
        CredentialDto credential = CredentialDto.builder()
                .username("credExpiredUser")
                .password("pass")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(false)
                .build();
        UserDetailsImpl userDetails = new UserDetailsImpl(credential);
        assertFalse(userDetails.isCredentialsNonExpired());
    }
}
