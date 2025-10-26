package globaltests.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class CloudConfigIntegrationGlobalTest {


    @Test
    void testConfigServerFetchesUserServiceConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://localhost:9296/user-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }


    @Test
    void testConfigServerFetchesOrderServiceConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://localhost:9296/order-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }


    @Test
    void testConfigServerFetchesProductServiceConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://localhost:9296/product-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }


    @Test
    void testConfigServerFetchesPaymentServiceConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://localhost:9296/payment-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }


    @Test
    void testConfigServerFetchesFavouriteServiceConfig() {
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject("http://localhost:9296/favourite-service/default", String.class);
        assertNotNull(config);
        assertTrue(config.contains("spring"));
    }
}
