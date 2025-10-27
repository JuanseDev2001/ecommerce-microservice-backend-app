package com.selimhorri.app.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ServiceDiscoveryApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // Spring context should load without exceptions
    }

    @Test
    void testApplicationClassIsNotNull() {
        assertNotNull(com.selimhorri.app.ServiceDiscoveryApplication.class);
    }

    @Test
    void testSpringBootApplicationAnnotationPresent() {
        assertTrue(com.selimhorri.app.ServiceDiscoveryApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void testEnableEurekaServerAnnotationPresent() {
        assertTrue(com.selimhorri.app.ServiceDiscoveryApplication.class.isAnnotationPresent(org.springframework.cloud.netflix.eureka.server.EnableEurekaServer.class));
    }
}
