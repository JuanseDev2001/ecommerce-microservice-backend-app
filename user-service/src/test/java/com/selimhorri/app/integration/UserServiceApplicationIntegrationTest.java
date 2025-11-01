package com.selimhorri.app.integration;

import com.selimhorri.app.UserServiceApplication;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.domain.Credential;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // Spring context should load without exceptions
    }

    @Test
    void testUserMappingHelperMapToDtoAndBack() {
        Credential cred = Credential.builder().credentialId(1).username("user").password("pass").build();
        User user = User.builder().userId(2).firstName("A").lastName("B").email("a@b.com").credential(cred).build();
        UserDto dto = UserMappingHelper.map(user);
        User user2 = UserMappingHelper.map(dto);
        assertEquals(user.getUserId(), user2.getUserId());
        assertEquals(user.getFirstName(), user2.getFirstName());
        assertEquals(user.getCredential().getUsername(), user2.getCredential().getUsername());
    }

    @Test
    void testUserDtoBuilderWithCredential() {
        CredentialDto cred = CredentialDto.builder().credentialId(1).username("user").password("pass").build();
        UserDto dto = UserDto.builder().userId(2).credentialDto(cred).build();
        assertEquals(2, dto.getUserId());
        assertEquals("user", dto.getCredentialDto().getUsername());
    }

    @Test
    void testApplicationClassIsNotNull() {
        assertNotNull(UserServiceApplication.class);
    }
}
