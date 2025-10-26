package globaltests.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FavouriteServiceE2ETest {

    private final String baseUrl = "http://localhost:8800/favourite-service/api/favourites";
    private final String userServiceUrl = "http://localhost:8700/user-service/api/users";
    private final String productServiceUrl = "http://localhost:8500/product-service/api/products";

    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter FAVOURITE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy__HH:mm:ss:SSSSSS");
    // Helper to create a UserDto JSON
    private String buildUserJson(int userId) {
        return String.format("{" +
                "\"userId\":%d," +
                "\"firstName\":\"Test\"," +
                "\"lastName\":\"User\"," +
                "\"email\":\"testuser%d@email.com\"," +
                "\"credential\": {" +
                "\"username\": \"testuser%d\"," +
                "\"password\": \"password%d\"}" +
                "}", userId, userId, userId, userId);
    }

    // Helper to create a ProductDto JSON
    private String buildProductJson(int productId) {
        return String.format("{" +
                "\"productId\":%d," +
                "\"productTitle\":\"Test Product %d\"," +
                "\"priceUnit\":99.99}", productId, productId);
    }

    @BeforeEach
    void setupDependencies() {
        // Create users with userId 1 and 2 if not exist
        for (int userId : new int[]{1, 2}) {
            try {
                restTemplate.getForEntity(userServiceUrl + "/" + userId, String.class);
            } catch (Exception e) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(buildUserJson(userId), headers);
                restTemplate.postForEntity(userServiceUrl, request, String.class);
            }
        }
        // Create products with productId 1 and 2 if not exist
        for (int productId : new int[]{1, 2}) {
            try {
                restTemplate.getForEntity(productServiceUrl + "/" + productId, String.class);
            } catch (Exception e) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(buildProductJson(productId), headers);
                restTemplate.postForEntity(productServiceUrl, request, String.class);
            }
        }
    }

    // Helper to create a FavouriteDto JSON
    private String buildFavouriteJson(int userId, int productId, String likeDate) {
        return String.format("{" +
                "\"userId\":%d," +
                "\"productId\":%d," +
                "\"likeDate\":\"%s\"}", userId, productId, likeDate);
    }

    @Test
    void testCreateFavourite() {
        String likeDate = LocalDateTime.now().format(FAVOURITE_FORMATTER);
        String json = buildFavouriteJson(1, 1, likeDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("userId"));
    }

    @Test
    void testGetAllFavourites() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("collection"));
    }

    @Test
    void testGetFavouriteById() {
        String likeDate = LocalDateTime.now().format(FAVOURITE_FORMATTER);
        // Create first
        String json = buildFavouriteJson(2, 2, likeDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        restTemplate.postForEntity(baseUrl, request, String.class);
        // Get by id
        String url = String.format("%s/%d/%d/%s", baseUrl, 2, 2, likeDate.replace(" ", "%20"));
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("userId"));
    }

    @Test
    void testUpdateFavourite() {
        String likeDate = LocalDateTime.now().format(FAVOURITE_FORMATTER);
        String json = buildFavouriteJson(1, 2, likeDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        restTemplate.postForEntity(baseUrl, request, String.class);
        // Update (simulate, e.g. update likeDate)
        String newLikeDate = LocalDateTime.now().plusMinutes(1).format(FAVOURITE_FORMATTER);
        String updateJson = buildFavouriteJson(1, 2, newLikeDate);
        HttpEntity<String> updateRequest = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(baseUrl, HttpMethod.PUT, updateRequest, String.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testDeleteFavourite() {
        String likeDate = LocalDateTime.now().format(FAVOURITE_FORMATTER);
        String json = buildFavouriteJson(2, 1, likeDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        restTemplate.postForEntity(baseUrl, request, String.class);
        // Delete
        String url = String.format("%s/%d/%d/%s", baseUrl, 2, 1, likeDate.replace(" ", "%20"));
        ResponseEntity<String> deleteResponse = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());
    }
}
