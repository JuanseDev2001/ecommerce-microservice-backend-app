package globaltests.integration;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class ProxyClientIntegracionGlobalTests {

    // 1. Health check del Proxy Client
    @Test
    void testProxyClientHealth() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject("http://localhost:8900/app/actuator/health", String.class);
        assertNotNull(response);
        assertTrue(response.contains("UP"));
    }

    // 2. Endpoint base /app (debería devolver algo, aunque sea 404 o 401)
    @Test
    void testProxyClientBasePath() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject("http://localhost:8900/app", String.class);
            // Si no lanza excepción, pasa
            assertTrue(true);
        } catch (Exception e) {
            // Si lanza excepción, igual pasa porque el endpoint existe
            assertTrue(e.getMessage().contains("404") || e.getMessage().contains("401") || e.getMessage().contains("403"));
        }
    }

    // 3. Endpoint inexistente bajo /app (debe devolver 404)
    @Test
    void testProxyClientNotFound() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject("http://localhost:8900/app/doesnotexist", String.class);
            fail("Should have thrown exception for 404");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("404"));
        }
    }

    // 4. Health check con método HEAD (debe responder aunque sea 200/UP)
    @Test
    void testProxyClientHealthHead() {
        RestTemplate restTemplate = new RestTemplate();
        var response = restTemplate.headForHeaders("http://localhost:8900/app/actuator/health");
        assertNotNull(response);
        assertTrue(response.getContentLength() >= 0);
    }

    // 5. Health check con error de puerto (debe fallar si el puerto está mal)
    @Test
    void testProxyClientWrongPort() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject("http://localhost:8999/app/actuator/health", String.class);
            fail("Should have thrown exception for connection refused");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("connection") || e.getMessage().toLowerCase().contains("refused"));
        }
    }
}
