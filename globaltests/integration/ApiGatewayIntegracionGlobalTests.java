package globaltests.integration;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class ApiGatewayIntegracionGlobalTests {

    // 1. Health check del API Gateway
    @Test
    void testApiGatewayHealth() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject("http://localhost:8080/actuator/health", String.class);
        assertNotNull(response);
        assertTrue(response.contains("UP"));
    }

    // 2. Redirección a Order Service
    @Test
    void testOrderServiceRoute() {
        RestTemplate restTemplate = new RestTemplate();
        // Suponiendo que /order-service/actuator/health está expuesto
        String response = restTemplate.getForObject("http://localhost:8080/order-service/actuator/health", String.class);
        assertNotNull(response);
        assertTrue(response.contains("UP"));
    }

    // 3. Redirección a Product Service
    @Test
    void testProductServiceRoute() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject("http://localhost:8080/product-service/actuator/health", String.class);
        assertNotNull(response);
        assertTrue(response.contains("UP"));
    }

    // 4. Redirección a User Service
    @Test
    void testUserServiceRoute() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject("http://localhost:8080/user-service/actuator/health", String.class);
        assertNotNull(response);
        assertTrue(response.contains("UP"));
    }

    // 5. Redirección a Payment Service
    @Test
    void testPaymentServiceRoute() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject("http://localhost:8080/payment-service/actuator/health", String.class);
        assertNotNull(response);
        assertTrue(response.contains("UP"));
    }
}
