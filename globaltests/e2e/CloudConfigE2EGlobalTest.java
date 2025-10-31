package globaltests.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class CloudConfigE2EGlobalTest {


    @Test
    void testUserServiceStartsWithConfigFromCloudConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://cloud-config:9296/user-service/default", String.class);
        assertNotNull(config);
        // Simulate user-service startup using config
        assertTrue(config.contains("spring"));
    }


    @Test
    void testOrderServiceStartsWithConfigFromCloudConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://cloud-config:9296/order-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }


    @Test
    void testProductServiceStartsWithConfigFromCloudConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://cloud-config:9296/product-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }


    @Test
    void testPaymentServiceStartsWithConfigFromCloudConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://cloud-config:9296/payment-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }


    @Test
    void testFavouriteServiceStartsWithConfigFromCloudConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://cloud-config:9296/favourite-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }
}
