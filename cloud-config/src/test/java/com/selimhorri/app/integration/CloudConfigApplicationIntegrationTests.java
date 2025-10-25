package com.selimhorri.app.integration;

import com.selimhorri.app.CloudConfigApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CloudConfigApplication.class)
class CloudConfigApplicationIntegrationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void configServerBeanExists() {
        // ConfigServerConfiguration bean should exist if config server is enabled
        assertTrue(context.getBeanNamesForType(org.springframework.cloud.config.server.config.ConfigServerConfiguration.class).length > 0);
    }

    @Test
    void eurekaClientBeanExists() {
        // Eureka client bean should exist if Eureka is enabled
        assertTrue(context.getBeanNamesForType(org.springframework.cloud.netflix.eureka.EurekaClientConfigBean.class).length > 0);
    }

    @Test
    void mainClassIsLoaded() {
        assertNotNull(context.getBean(CloudConfigApplication.class));
    }

    @Test
    void environmentIsNotNull() {
        assertNotNull(context.getEnvironment());
    }
}