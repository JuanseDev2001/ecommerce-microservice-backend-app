package globaltests.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class SystemE2ESmokeTest {

    @Test
    void testUserServiceIsUp() {
        assertHealth("http://localhost:8700/user-service/actuator/health");
    }

    @Test
    void testProductServiceIsUp() {
        assertHealth("http://localhost:8500/product-service/actuator/health");
    }

    @Test
    void testOrderServiceIsUp() {
        assertHealth("http://localhost:8300/order-service/actuator/health");
    }

    @Test
    void testPaymentServiceIsUp() {
        assertHealth("http://localhost:8400/payment-service/actuator/health");
    }

    @Test
    void testFavouriteServiceIsUp() {
        assertHealth("http://localhost:8800/favourite-service/actuator/health");
    }

    @Test
    void testProxyClientIsUp() {
        assertHealth("http://localhost:8900/app/actuator/health");
    }

    @Test
    void testShippingServiceIsUp() {
        assertHealth("http://localhost:8600/shipping-service/actuator/health");
    }

    @Test
    void testCloudConfigIsUp() {
        assertHealth("http://localhost:9296/actuator/health");
    }

    @Test
    void testServiceDiscoveryIsUp() {
        assertHealth("http://localhost:8761/actuator/health");
    }


    private void assertHealth(String url) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        assertNotNull(response, "No response from " + url);
        assertTrue(response.contains("UP"), "Service not UP at " + url);
    }
}
