package globaltests.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentServiceE2ETest {

    private final String orderServiceUrl = "http://order-service:8300/order-service/api/orders";
    private final String paymentServiceUrl = "http://payment-service:8400/payment-service/api/payments";
    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter ORDER_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy__HH:mm:ss:SSSSSS");

    private Integer createOrder() {
        String orderJson = String.format(java.util.Locale.US, "{" +
                "\"orderDate\":\"%s\"," +
                "\"orderDesc\":\"PaymentTestOrder\"," +
                "\"orderFee\":%.2f," +
                "\"cart\": {\"cartId\": 1}" + // Assumes cartId 1 exists, or adapt as needed
                "}", LocalDateTime.now().format(ORDER_FORMATTER), 100.00);
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

    private String buildPaymentJson(Integer orderId, boolean isPayed, String status) {
        return String.format(java.util.Locale.US, "{" +
                "\"isPayed\":%s," +
                "\"paymentStatus\":\"%s\"," +
                "\"order\": {\"orderId\": %d}" +
                "}", isPayed, status, orderId);
    }

    @Test
    void testCreatePayment() {
        int orderId = createOrder();
        String paymentJson = buildPaymentJson(orderId, false, "NOT_STARTED");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(paymentJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(paymentServiceUrl, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("paymentId"));
    }

    @Test
    void testGetAllPayments() {
        int orderId = createOrder();
        String paymentJson = buildPaymentJson(orderId, false, "NOT_STARTED");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(paymentJson, headers);
        restTemplate.postForEntity(paymentServiceUrl, request, String.class);
        ResponseEntity<String> response = restTemplate.getForEntity(paymentServiceUrl, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("collection"));
    }

    @Test
    void testGetPaymentById() {
        int orderId = createOrder();
        String paymentJson = buildPaymentJson(orderId, false, "NOT_STARTED");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(paymentJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(paymentServiceUrl, request, String.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        String body = createResponse.getBody();
        assertNotNull(body);
        int idx = body.indexOf("paymentId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int paymentId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        ResponseEntity<String> getResponse = restTemplate.getForEntity(paymentServiceUrl + "/" + paymentId, String.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("paymentId"));
    }

    @Test
    void testUpdatePayment() {
        int orderId = createOrder();
        String paymentJson = buildPaymentJson(orderId, false, "NOT_STARTED");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(paymentJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(paymentServiceUrl, request, String.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        String body = createResponse.getBody();
        int idx = body.indexOf("paymentId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int paymentId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Update payment
        String updateJson = String.format(java.util.Locale.US, "{" +
                "\"paymentId\":%d," +
                "\"isPayed\":true," +
                "\"paymentStatus\":\"COMPLETED\"," +
                "\"order\": {\"orderId\": %d}" +
                "}", paymentId, orderId);
        HttpEntity<String> updateRequest = new HttpEntity<>(updateJson, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(paymentServiceUrl, HttpMethod.PUT, updateRequest, String.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
        assertTrue(updateResponse.getBody().contains("COMPLETED"));
    }

    @Test
    void testDeletePayment() {
        int orderId = createOrder();
        String paymentJson = buildPaymentJson(orderId, false, "NOT_STARTED");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(paymentJson, headers);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(paymentServiceUrl, request, String.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        String body = createResponse.getBody();
        int idx = body.indexOf("paymentId");
        String sub = body.substring(idx);
        String[] parts = sub.split(":");
        int paymentId = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        // Delete payment
        ResponseEntity<String> deleteResponse = restTemplate.exchange(paymentServiceUrl + "/" + paymentId, HttpMethod.DELETE, null, String.class);
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());
    }
}
