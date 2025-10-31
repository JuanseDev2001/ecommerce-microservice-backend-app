package globaltests.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderServiceE2ETest {

    private final String userServiceUrl = "http://user-service:8700/user-service/api/users";
    private final String cartServiceUrl = "http://cart-service:8300/order-service/api/carts";
    private final String orderServiceUrl = "http://order-service:8300/order-service/api/orders";
    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter ORDER_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy__HH:mm:ss:SSSSSS");

    private Integer ensureUserExists(int userId) {
        try {
            restTemplate.getForEntity(userServiceUrl + "/" + userId, String.class);
        } catch (Exception e) {
            String userJson = String.format("{" +
                    "\"userId\":%d," +
                    "\"firstName\":\"OrderTest\"," +
                    "\"lastName\":\"User\"," +
                    "\"email\":\"ordertestuser%d@email.com\"," +
                    "\"credential\": {" +
                    "\"username\": \"ordertestuser%d\"," +
                    "\"password\": \"password%d\"}" +
                    "}", userId, userId, userId, userId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(userJson, headers);
            restTemplate.postForEntity(userServiceUrl, request, String.class);
        }
        return userId;
    }

    private Integer createCartForUser(int userId) {
        String cartJson = String.format("{" +
                "\"userId\":%d" +
                "}", userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(cartJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(cartServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String body = response.getBody();
        assertNotNull(body);
        // Extract cartId from response
        int idx = body.indexOf("cartId");
        assertTrue(idx > 0);
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int cartId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        return cartId;
    }

    private String buildOrderJson(Integer cartId, String desc, double fee) {
        String orderDate = LocalDateTime.now().format(ORDER_FORMATTER);
    return String.format(java.util.Locale.US, "{" +
        "\"orderDate\":\"%s\"," +
        "\"orderDesc\":\"%s\"," +
        "\"orderFee\":%.2f," +
        "\"cart\": {\"cartId\": %d}" +
        "}", orderDate, desc, fee, cartId);
    }

    @Test
    void testCreateOrder() {
        int userId = ensureUserExists(1001);
        int cartId = createCartForUser(userId);
        String orderJson = buildOrderJson(cartId, "Test Order", 123.45);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(orderServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("orderId"));
    }

    @Test
    void testGetAllOrders() {
        ResponseEntity<String> response = restTemplate.getForEntity(orderServiceUrl, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("collection"));
    }

    @Test
    void testGetOrderById() {
        int userId = ensureUserExists(1002);
        int cartId = createCartForUser(userId);
        String orderJson = buildOrderJson(cartId, "Order By Id", 222.22);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(orderServiceUrl, request, String.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        String body = createResponse.getBody();
        assertNotNull(body);
        int idx = body.indexOf("orderId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int orderId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        ResponseEntity<String> getResponse = restTemplate.getForEntity(orderServiceUrl + "/" + orderId, String.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("orderId"));
    }

    @Test
    void testUpdateOrder() {
        int userId = ensureUserExists(1003);
        int cartId = createCartForUser(userId);
        String orderJson = buildOrderJson(cartId, "Order To Update", 333.33);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(orderServiceUrl, request, String.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        String body = createResponse.getBody();
        int idx = body.indexOf("orderId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int orderId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Update order
    String updateJson = String.format(java.util.Locale.US, "{" +
        "\"orderId\":%d," +
        "\"orderDate\":\"%s\"," +
        "\"orderDesc\":\"Updated Desc\"," +
        "\"orderFee\":%.2f," +
        "\"cart\": {\"cartId\": %d}" +
        "}", orderId, LocalDateTime.now().format(ORDER_FORMATTER), 444.44, cartId);
        HttpEntity<String> updateRequest = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(orderServiceUrl, HttpMethod.PUT, updateRequest, String.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
        assertTrue(updateResponse.getBody().contains("Updated Desc"));
    }

    @Test
    void testDeleteOrder() {
        int userId = ensureUserExists(1004);
        int cartId = createCartForUser(userId);
        String orderJson = buildOrderJson(cartId, "Order To Delete", 555.55);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(orderServiceUrl, request, String.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        String body = createResponse.getBody();
        int idx = body.indexOf("orderId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int orderId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Delete order
        ResponseEntity<String> deleteResponse = restTemplate.exchange(orderServiceUrl + "/" + orderId, HttpMethod.DELETE, null, String.class);
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());
    }
}
