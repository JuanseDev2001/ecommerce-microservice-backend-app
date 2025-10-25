package com.selimhorri.app.unit;

import com.selimhorri.app.CloudConfigApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.lang.annotation.Annotation;
import static org.junit.jupiter.api.Assertions.*;

class CloudConfigApplicationUnitTests {

    @Test
    void testMainMethodRuns() {
        // Just check that main does not throw
        assertDoesNotThrow(() -> CloudConfigApplication.main(new String[]{}));
    }

    @Test
    void testSpringBootApplicationAnnotationPresent() {
        assertTrue(CloudConfigApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void testEnableEurekaClientAnnotationPresent() {
        assertTrue(CloudConfigApplication.class.isAnnotationPresent(org.springframework.cloud.netflix.eureka.EnableEurekaClient.class));
    }

    @Test
    void testEnableConfigServerAnnotationPresent() {
        assertTrue(CloudConfigApplication.class.isAnnotationPresent(org.springframework.cloud.config.server.EnableConfigServer.class));
    }

    @Test
    void testClassIsPublic() {
        assertTrue(java.lang.reflect.Modifier.isPublic(CloudConfigApplication.class.getModifiers()));
    }
}