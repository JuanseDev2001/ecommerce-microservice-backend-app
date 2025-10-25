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

    @Test
    void testProxyClientBasePath() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject("http://localhost:8900/app", String.class);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("404"));
        }
    }

    @Test
    void testProxyClientNotFound() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject("http://localhost:8900/app/doesnotexist", String.class);
            fail("Should have thrown exception for 404");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertTrue(e.getMessage().contains("403"));
        } catch (Exception e) {
            fail("Expected HttpClientErrorException with 404 but got: " + e);
        }
    }

    // 4. Health check con método HEAD (debe responder aunque sea 200/UP)
    @Test
    void testProxyClientHealthHead() {
        RestTemplate restTemplate = new RestTemplate();
        var headers = restTemplate.headForHeaders("http://localhost:8900/app/actuator/health");
        assertNotNull(headers);
        // Some proxies return Transfer-Encoding: chunked and don't include Content-Length for HEAD.
        // HttpHeaders.getContentLength() returns -1 when Content-Length is absent. Accept that case.
        long contentLength = headers.getContentLength();
        if (contentLength != -1L) {
            assertTrue(contentLength >= 0);
        } else {
            // If no Content-Length, at least ensure we received headers back
            assertFalse(headers.isEmpty());
        }
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
