package globaltests.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShippingServiceE2ETest {

    private final String productServiceUrl = "http://localhost:8500/product-service/api/products";
    private final String orderServiceUrl = "http://localhost:8300/order-service/api/orders";
    private final String shippingServiceUrl = "http://localhost:8600/shipping-service/api/shippings";
    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter ORDER_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy__HH:mm:ss:SSSSSS");

    @BeforeAll
    static void ensureCategoryExists() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8500/product-service/api/categories/1";
        try {
            restTemplate.getForEntity(url, String.class);
        } catch (Exception e) {
            String categoryJson = "{" +
                "\"categoryId\":1," +
                "\"categoryTitle\":\"Root\"," +
                "\"imageUrl\":\"http://example.com/root.jpg\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(categoryJson, headers);
            restTemplate.postForEntity("http://localhost:8500/product-service/api/categories", request, String.class);
        }
    }

    private Integer createCategory() {
        // Use categoryId 1, which is ensured to exist
        return 1;
    }

    private Integer createProduct(Integer categoryId) {
        String productJson = String.format(java.util.Locale.US, "{" +
            "\"productTitle\":\"ShippingTestProduct\"," +
            "\"imageUrl\":\"http://example.com/image.jpg\"," +
            "\"sku\":\"SKU_SHIP\"," +
            "\"priceUnit\":50.00," +
            "\"quantity\":100," +
            "\"category\":{\"categoryId\":%d}" +
            "}", categoryId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(productJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(productServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String body = response.getBody();
        assertNotNull(body);
        int idx = body.indexOf("productId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int productId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        return productId;
    }

    private Integer createUser() {
        String userJson = "{" +
            "\"firstName\":\"ShippingUser\"," +
            "\"lastName\":\"Test\"," +
            "\"email\":\"shippinguser@email.com\"," +
            "\"credential\": {\"username\": \"shippinguser\", \"password\": \"password\"}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8700/user-service/api/users", request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String body = response.getBody();
        assertNotNull(body);
        int idx = body.indexOf("userId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int userId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        return userId;
    }

    private Integer createCart(Integer userId) {
        String cartJson = String.format("{\"userId\":%d}", userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(cartJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8300/order-service/api/carts", request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String body = response.getBody();
        assertNotNull(body);
        int idx = body.indexOf("cartId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int cartId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        return cartId;
    }

    private Integer createOrder() {
        Integer userId = createUser();
        Integer cartId = createCart(userId);
        String orderJson = String.format(java.util.Locale.US, "{" +
            "\"orderDate\":\"%s\"," +
            "\"orderDesc\":\"ShippingTestOrder\"," +
            "\"orderFee\":%.2f," +
            "\"cart\": {\"cartId\": %d}" +
            "}", LocalDateTime.now().format(ORDER_FORMATTER), 100.00, cartId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(orderServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String body = response.getBody();
        assertNotNull(body);
        int idx = body.indexOf("orderId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int orderId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        return orderId;
    }

    private String buildOrderItemJson(Integer orderId, Integer productId, int quantity) {
        return String.format(java.util.Locale.US, "{" +
                "\"orderId\":%d," +
                "\"productId\":%d," +
                "\"orderedQuantity\":%d," +
                "\"order\":{\"orderId\":%d}," +
                "\"product\":{\"productId\":%d}" +
                "}", orderId, productId, quantity, orderId, productId);
    }

    @Test
    void testCreateOrderItem() {
        int categoryId = createCategory();
        int productId = createProduct(categoryId);
        int orderId = createOrder();
        String orderItemJson = buildOrderItemJson(orderId, productId, 5);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderItemJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(shippingServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("orderId"));
        assertTrue(response.getBody().contains("productId"));
    }

    @Test
    void testGetAllOrderItems() {
        int categoryId = createCategory();
        int productId = createProduct(categoryId);
        int orderId = createOrder();
        String orderItemJson = buildOrderItemJson(orderId, productId, 3);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderItemJson, headers);
        restTemplate.postForEntity(shippingServiceUrl, request, String.class);
        ResponseEntity<String> response = restTemplate.getForEntity(shippingServiceUrl, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("collection"));
    }

    @Test
    void testGetOrderItemById() {
        // Create product via product service E2E logic
        int categoryId = createCategory();
        int productId = createProduct(categoryId);
        // Create order via order service E2E logic
        int orderId = createOrder();
        // Now create the shipping order item
        String orderItemJson = buildOrderItemJson(orderId, productId, 2);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderItemJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(shippingServiceUrl, request, String.class);
        System.out.println("[DEBUG] Create OrderItem Response: Status=" + createResponse.getStatusCode() + ", Body=" + createResponse.getBody());
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        // Test get by id
        ResponseEntity<String> getResponse = restTemplate.getForEntity(shippingServiceUrl + "/" + orderId + "/" + productId, String.class);
        System.out.println("[DEBUG] Get OrderItemById Response: Status=" + getResponse.getStatusCode() + ", Body=" + getResponse.getBody());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("orderId"));
        assertTrue(getResponse.getBody().contains("productId"));
        // Clean up: delete order item, order, and product
        restTemplate.exchange(shippingServiceUrl + "/" + orderId + "/" + productId, HttpMethod.DELETE, null, String.class);
        restTemplate.delete(orderServiceUrl + "/" + orderId);
        restTemplate.delete(productServiceUrl + "/" + productId);
    }

    @Test
    void testUpdateOrderItem() {
        int categoryId = createCategory();
        int productId = createProduct(categoryId);
        int orderId = createOrder();
        String orderItemJson = buildOrderItemJson(orderId, productId, 1);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderItemJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(shippingServiceUrl, request, String.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        // Update order item (change quantity)
        String updateJson = buildOrderItemJson(orderId, productId, 10);
        HttpEntity<String> updateRequest = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(shippingServiceUrl, HttpMethod.PUT, updateRequest, String.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
        assertTrue(updateResponse.getBody().contains("10"));
    }

    @Test
    void testDeleteOrderItem() {
        // Create product via product service E2E logic
        int categoryId = createCategory();
        int productId = createProduct(categoryId);
        // Create order via order service E2E logic
        int orderId = createOrder();
        // Now create the shipping order item
        String orderItemJson = buildOrderItemJson(orderId, productId, 7);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderItemJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(shippingServiceUrl, request, String.class);
        System.out.println("[DEBUG] Create OrderItem Response: Status=" + createResponse.getStatusCode() + ", Body=" + createResponse.getBody());
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        // Test delete order item
        ResponseEntity<String> deleteResponse = restTemplate.exchange(shippingServiceUrl + "/" + orderId + "/" + productId, HttpMethod.DELETE, null, String.class);
        System.out.println("[DEBUG] Delete OrderItem Response: Status=" + deleteResponse.getStatusCode() + ", Body=" + deleteResponse.getBody());
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());
        // Clean up: delete order and product
        restTemplate.delete(orderServiceUrl + "/" + orderId);
        restTemplate.delete(productServiceUrl + "/" + productId);
    }
}
