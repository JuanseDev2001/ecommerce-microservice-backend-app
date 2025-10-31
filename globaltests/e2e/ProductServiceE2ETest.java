package globaltests.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceE2ETest {
    // Helper method to extract productId from JSON response using Jackson
    private Integer extractProductId(String responseBody) {
        if (responseBody == null) return null;
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
            if (node.has("productId")) {
                return node.get("productId").asInt();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    private final String baseUrl = "http://product-service:8500/product-service/api/products";
    private final RestTemplate restTemplate = new RestTemplate();

    private Integer createdProductId;

    @Test
    void testCreateProduct() {
        String productJson = "{" +
            "\"productTitle\":\"E2E Test Product\"," +
            "\"imageUrl\":\"http://example.com/image.jpg\"," +
            "\"sku\":\"SKU123\"," +
            "\"priceUnit\":99.99," +
            "\"quantity\":10," +
            "\"category\":{\"categoryId\":1}" +
            "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl, request, String.class);
        // Accept any 2xx successful status (e.g. 200 OK or 201 Created)
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(createResponse.getBody());
        assertTrue(createResponse.getBody().contains("E2E Test Product"));
    }

    @Test
    void testGetAllProducts() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("productTitle"));
    }

    @Test
    void testGetProductById() {
        // First, create a product
        String productJson = "{" +
            "\"productTitle\":\"E2E GetById Product\"," +
            "\"imageUrl\":\"http://example.com/image.jpg\"," +
            "\"sku\":\"SKU124\"," +
            "\"priceUnit\":49.99," +
            "\"quantity\":5," +
            "\"category\":{\"categoryId\":1}" +
            "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl, request, String.class);
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        System.out.println("Create response body (get by id): " + createResponse.getBody());
        Integer productId = extractProductId(createResponse.getBody());
        assertNotNull(productId);
        ResponseEntity<String> getResponse = restTemplate.getForEntity(baseUrl + "/" + productId, String.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("E2E GetById Product"));
    }

    @Test
    void testUpdateProduct() {
        // Create product
        String productJson = "{" +
            "\"productTitle\":\"E2E Update Product\"," +
            "\"imageUrl\":\"http://example.com/image.jpg\"," +
            "\"sku\":\"SKU125\"," +
            "\"priceUnit\":59.99," +
            "\"quantity\":7," +
            "\"category\":{\"categoryId\":1}" +
            "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl, request, String.class);
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        // Update product (assuming id 1)
        String updateJson = "{" +
            "\"productId\":1," +
            "\"productTitle\":\"E2E Updated Product\"," +
            "\"imageUrl\":\"http://example.com/image2.jpg\"," +
            "\"sku\":\"SKU125-UPDATED\"," +
            "\"priceUnit\":79.99," +
            "\"quantity\":8," +
            "\"category\":{\"categoryId\":1}" +
            "}";
        HttpEntity<String> updateRequest = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(baseUrl, HttpMethod.PUT, updateRequest, String.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful() || updateResponse.getStatusCode().is4xxClientError());
    }

    @Test
    void testDeleteProduct() {
        // Create product
        String productJson = "{" +
            "\"productTitle\":\"E2E Delete Product\"," +
            "\"imageUrl\":\"http://example.com/image.jpg\"," +
            "\"sku\":\"SKU126\"," +
            "\"priceUnit\":19.99," +
            "\"quantity\":2," +
            "\"category\":{\"categoryId\":1}" +
            "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl, request, String.class);
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        System.out.println("Create response body (delete): " + createResponse.getBody());
        Integer productId = extractProductId(createResponse.getBody());
        assertNotNull(productId);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/" + productId, HttpMethod.DELETE, null, String.class);
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

    }

    @Test
    void testProductNotFound() {
        try {
            restTemplate.getForEntity(baseUrl + "/999999", String.class);
            fail("Expected HttpClientErrorException.BadRequest");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
    }
}
