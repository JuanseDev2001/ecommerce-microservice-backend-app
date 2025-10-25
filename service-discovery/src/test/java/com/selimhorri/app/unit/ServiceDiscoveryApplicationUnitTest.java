package com.selimhorri.app.unit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceDiscoveryApplicationUnitTest {

    @Test
    void testMainMethodExists() {
        // Just check that the main method exists and is public static
        try {
            java.lang.reflect.Method main = com.selimhorri.app.ServiceDiscoveryApplication.class.getMethod("main", String[].class);
            int modifiers = main.getModifiers();
            assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
            assertTrue(java.lang.reflect.Modifier.isStatic(modifiers));
        } catch (NoSuchMethodException e) {
            fail("main method not found");
        }
    }

    @Test
    void testMainWithNullArgsThrows() {
        // Should throw IllegalArgumentException if null is passed
        assertThrows(IllegalArgumentException.class, () -> com.selimhorri.app.ServiceDiscoveryApplication.main(null));
    }

    @Test
    void testClassHasSpringBootApplicationAnnotation() {
        assertTrue(com.selimhorri.app.ServiceDiscoveryApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void testClassHasEnableEurekaServerAnnotation() {
        assertTrue(com.selimhorri.app.ServiceDiscoveryApplication.class.isAnnotationPresent(org.springframework.cloud.netflix.eureka.server.EnableEurekaServer.class));
    }

    @Test
    void testClassName() {
        assertEquals("ServiceDiscoveryApplication", com.selimhorri.app.ServiceDiscoveryApplication.class.getSimpleName());
    }
}
