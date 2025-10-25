package com.selimhorri.app.unit;

import com.selimhorri.app.UserServiceApplication;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.dto.AddressDto;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceApplicationUnitTest {

    @Test
    void testMainMethodExists() {
        try {
            java.lang.reflect.Method main = UserServiceApplication.class.getMethod("main", String[].class);
            int modifiers = main.getModifiers();
            assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
            assertTrue(java.lang.reflect.Modifier.isStatic(modifiers));
        } catch (NoSuchMethodException e) {
            fail("main method not found");
        }
    }

    @Test
    void testUserMappingHelperMapToDto() {
        Credential cred = Credential.builder().credentialId(1).username("user").password("pass").build();
        User user = User.builder().userId(2).firstName("A").lastName("B").email("a@b.com").credential(cred).build();
        UserDto dto = UserMappingHelper.map(user);
        assertEquals(2, dto.getUserId());
        assertEquals("A", dto.getFirstName());
        assertEquals("user", dto.getCredentialDto().getUsername());
    }

    @Test
    void testUserMappingHelperMapToEntity() {
        CredentialDto cred = CredentialDto.builder().credentialId(1).username("user").password("pass").build();
        UserDto dto = UserDto.builder().userId(2).firstName("A").lastName("B").email("a@b.com").credentialDto(cred).build();
        User user = UserMappingHelper.map(dto);
        assertEquals(2, user.getUserId());
        assertEquals("A", user.getFirstName());
        assertEquals("user", user.getCredential().getUsername());
    }

    @Test
    void testUserDtoBuilder() {
        UserDto dto = UserDto.builder().userId(10).firstName("F").lastName("L").email("f@l.com").build();
        assertEquals(10, dto.getUserId());
        assertEquals("F", dto.getFirstName());
        assertEquals("L", dto.getLastName());
        assertEquals("f@l.com", dto.getEmail());
    }

    @Test
    void testUserDtoWithAddresses() {
        AddressDto address = new AddressDto();
        UserDto dto = UserDto.builder().userId(1).addressDtos(Set.of(address)).build();
        assertNotNull(dto.getAddressDtos());
        assertEquals(1, dto.getAddressDtos().size());
    }
}
