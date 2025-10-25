package com.selimhorri.app.integration;

import com.selimhorri.app.ShippingServiceApplication;
import com.selimhorri.app.config.mapper.MapperConfig;
import com.selimhorri.app.config.client.ClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ShippingServiceApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // Spring context should load without exceptions
    }

    @Test
    void testObjectMapperBean() {
        MapperConfig config = new MapperConfig();
        ObjectMapper mapper = config.objectMapperBean();
        assertNotNull(mapper);
        assertTrue(mapper.isEnabled(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    void testRestTemplateBean() {
        ClientConfig config = new ClientConfig();
        RestTemplate restTemplate = config.restTemplateBean();
        assertNotNull(restTemplate);
    }

    @Test
    void testMainMethodDoesNotThrow() {
        assertDoesNotThrow(() -> ShippingServiceApplication.main(new String[]{}));
    }

    @Test
    void testApplicationClassIsNotNull() {
        assertNotNull(ShippingServiceApplication.class);
    }
}
