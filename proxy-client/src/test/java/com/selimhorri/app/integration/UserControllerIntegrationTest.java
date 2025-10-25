package com.selimhorri.app.integration;

import com.selimhorri.app.business.user.controller.UserController;
import com.selimhorri.app.business.user.model.UserDto;
import com.selimhorri.app.business.user.model.CredentialDto;
import com.selimhorri.app.business.user.service.UserClientService;
import com.selimhorri.app.business.user.model.response.UserUserServiceCollectionDtoResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

public class UserControllerIntegrationTest {

    @Test
    void testFindAllReturnsCollection() {
        UserClientService mockService = Mockito.mock(UserClientService.class);
        UserUserServiceCollectionDtoResponse response = new UserUserServiceCollectionDtoResponse();
        Mockito.when(mockService.findAll()).thenReturn(ResponseEntity.ok(response));
        UserController controller = new UserController(mockService);
        ResponseEntity<UserUserServiceCollectionDtoResponse> result = controller.findAll();
        assertEquals(response, result.getBody());
    }

    @Test
    void testFindByIdReturnsUser() {
        UserClientService mockService = Mockito.mock(UserClientService.class);
        UserDto user = new UserDto(); user.setUserId(1);
        Mockito.when(mockService.findById("1")).thenReturn(ResponseEntity.ok(user));
        UserController controller = new UserController(mockService);
        ResponseEntity<UserDto> result = controller.findById("1");
        assertEquals(user, result.getBody());
    }

    @Test
    void testFindByUsernameReturnsUser() {
        UserClientService mockService = Mockito.mock(UserClientService.class);
        UserDto user = new UserDto();
        CredentialDto cred = new CredentialDto();
        cred.setUsername("testuser");
        user.setCredentialDto(cred);
        Mockito.when(mockService.findByUsername("testuser")).thenReturn(ResponseEntity.ok(user));
        UserController controller = new UserController(mockService);
        ResponseEntity<UserDto> result = controller.findByUsername("testuser");
        assertEquals(user, result.getBody());
    }

    @Test
    void testSaveReturnsSavedUser() {
        UserClientService mockService = Mockito.mock(UserClientService.class);
        UserDto user = new UserDto();
        CredentialDto cred = new CredentialDto();
        cred.setUsername("saveuser");
        user.setCredentialDto(cred);
        Mockito.when(mockService.save(user)).thenReturn(ResponseEntity.ok(user));
        UserController controller = new UserController(mockService);
        ResponseEntity<UserDto> result = controller.save(user);
        assertEquals(user, result.getBody());
    }

    @Test
    void testDeleteByIdReturnsTrue() {
        UserClientService mockService = Mockito.mock(UserClientService.class);
        Mockito.when(mockService.deleteById("1")).thenReturn(ResponseEntity.ok(true));
        UserController controller = new UserController(mockService);
        ResponseEntity<Boolean> result = controller.deleteById("1");
        assertTrue(result.getBody());
    }
}
