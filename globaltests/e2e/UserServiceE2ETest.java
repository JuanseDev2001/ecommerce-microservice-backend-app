package globaltests.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceE2ETest {
    private final String userServiceUrl = "http://localhost:8700/user-service/api/users";
    private final String credentialServiceUrl = "http://localhost:8700/user-service/api/credentials";
    private final String addressServiceUrl = "http://localhost:8700/user-service/api/address";
    private final String verificationTokenServiceUrl = "http://localhost:8700/user-service/api/verificationTokens";
    private final RestTemplate restTemplate = new RestTemplate();

    private int createdUserId;
    private int createdCredentialId;
    private int createdAddressId;
    private int createdVerificationTokenId;

    @Test
    void testCreateAndFetchUser() {
        String uniqueUsername = "johndoe" + System.currentTimeMillis();
        String userJson = "{" +
            "\"firstName\":\"John\"," +
            "\"lastName\":\"Doe\"," +
            "\"email\":\"john.doe@email.com\"," +
            "\"credential\": {" +
                "\"username\": \"" + uniqueUsername + "\"," +
                "\"password\": \"password\"," +
                "\"roleBasedAuthority\": \"ROLE_USER\"," +
                "\"isEnabled\": true," +
                "\"isAccountNonExpired\": true," +
                "\"isAccountNonLocked\": true," +
                "\"isCredentialsNonExpired\": true" +
            "}" +
        "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(userServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        int idx = response.getBody().indexOf("userId");
        String sub = response.getBody().substring(idx);
        String[] parts = sub.split(":");
        createdUserId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Fetch by ID
        ResponseEntity<String> getResponse = restTemplate.getForEntity(userServiceUrl + "/" + createdUserId, String.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("John"));
        // Fetch by username
        ResponseEntity<String> getByUsername = restTemplate.getForEntity(userServiceUrl + "/username/" + uniqueUsername, String.class);
        assertEquals(HttpStatus.OK, getByUsername.getStatusCode());
        assertTrue(getByUsername.getBody().contains(uniqueUsername));
    }

    @Test
    void testUpdateUser() {
        testCreateAndFetchUser();
        String updateJson = "{" +
            "\"userId\":" + createdUserId + "," +
            "\"firstName\":\"Jane\"," +
            "\"lastName\":\"Smith\"," +
            "\"email\":\"jane.smith@email.com\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(userServiceUrl, HttpMethod.PUT, request, String.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
        assertTrue(updateResponse.getBody().contains("Jane"));
    }

    @Test
    void testCreateAndUpdateAddress() {
        testCreateAndFetchUser();
        String addressJson = "{" +
            "\"fullAddress\":\"123 Main St, Metropolis, Countryland\"," +
            "\"postalCode\":\"12345\"," +
            "\"city\":\"Metropolis\"," +
            "\"user\":{\"userId\":" + createdUserId + "}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(addressJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(addressServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        int idx = response.getBody().indexOf("addressId");
        String sub = response.getBody().substring(idx);
        String[] parts = sub.split(":");
        createdAddressId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Fetch and update
        ResponseEntity<String> getResponse = restTemplate.getForEntity(addressServiceUrl + "/" + createdAddressId, String.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        String updateJson = "{" +
            "\"addressId\":" + createdAddressId + "," +
            "\"fullAddress\":\"456 Second St, Gotham, Countryland\"," +
            "\"postalCode\":\"54321\"," +
            "\"city\":\"Gotham\"," +
            "\"user\":{\"userId\":" + createdUserId + "}}";
        HttpEntity<String> updateRequest = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(addressServiceUrl, HttpMethod.PUT, updateRequest, String.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
        assertTrue(updateResponse.getBody().contains("Second St"));
    }

    @Test
    void testCreateAndUpdateCredential() {
        testCreateAndFetchUser();
        String credentialJson = "{" +
            "\"username\":\"janedoe\"," +
            "\"password\":\"newpass\"," +
            "\"user\":{\"userId\":" + createdUserId + "}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(credentialJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(credentialServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        int idx = response.getBody().indexOf("credentialId");
        String sub = response.getBody().substring(idx);
        String[] parts = sub.split(":");
        createdCredentialId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Fetch and update
        ResponseEntity<String> getResponse = restTemplate.getForEntity(credentialServiceUrl + "/" + createdCredentialId, String.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        String updateJson = "{" +
            "\"credentialId\":" + createdCredentialId + "," +
            "\"username\":\"janedoe2\"," +
            "\"password\":\"newpass2\"," +
            "\"roleBasedAuthority\":\"ROLE_USER\"," +
            "\"isEnabled\":true," +
            "\"isAccountNonExpired\":true," +
            "\"isAccountNonLocked\":true," +
            "\"isCredentialsNonExpired\":true," +
            "\"user\":{\"userId\":" + createdUserId + "}}";
        System.out.println("[DEBUG] Credential Update Payload: " + updateJson);
        HttpEntity<String> updateRequest = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(credentialServiceUrl, HttpMethod.PUT, updateRequest, String.class);
        System.out.println("[DEBUG] Credential Update Response: " + updateResponse.getStatusCode() + " - " + updateResponse.getBody());
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
        assertTrue(updateResponse.getBody().contains("janedoe2"));
    }

    @Test
    void testCreateAndDeleteVerificationToken() {
        testCreateAndFetchUser();
        String tokenJson = "{" +
            "\"token\":\"sometoken\"," +
            "\"credential\": {" +
                "\"credentialId\": " + createdCredentialId + "," +
                "\"username\": \"janedoe2\"," +
                "\"password\": \"newpass2\"," +
                "\"roleBasedAuthority\": \"ROLE_USER\"," +
                "\"isEnabled\": true," +
                "\"isAccountNonExpired\": true," +
                "\"isAccountNonLocked\": true," +
                "\"isCredentialsNonExpired\": true," +
                "\"user\": {\"userId\": " + createdUserId + "}" +
            "}}";
        System.out.println("[DEBUG] VerificationToken Create Payload: " + tokenJson);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(tokenJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(verificationTokenServiceUrl, request, String.class);
        System.out.println("[DEBUG] VerificationToken Create Response: " + response.getStatusCode() + " - " + response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        int idx = response.getBody().indexOf("verificationTokenId");
        String sub = response.getBody().substring(idx);
        String[] parts = sub.split(":");
        createdVerificationTokenId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Fetch and delete
        ResponseEntity<String> getResponse = restTemplate.getForEntity(verificationTokenServiceUrl + "/" + createdVerificationTokenId, String.class);
        System.out.println("[DEBUG] VerificationToken Fetch Response: " + getResponse.getStatusCode() + " - " + getResponse.getBody());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        ResponseEntity<String> deleteResponse = restTemplate.exchange(verificationTokenServiceUrl + "/" + createdVerificationTokenId, HttpMethod.DELETE, null, String.class);
        System.out.println("[DEBUG] VerificationToken Delete Response: " + deleteResponse.getStatusCode() + " - " + deleteResponse.getBody());
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());
    }
}
