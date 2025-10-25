package com.selimhorri.app.integration;

import com.selimhorri.app.config.encoder.EncoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class EncoderConfigIntegrationTest {

    @Test
    void testPasswordEncoderBeanMatches() {
        EncoderConfig config = new EncoderConfig();
        PasswordEncoder encoder = config.getPasswordEncoder();
        String raw = "integrationPass";
        String encoded = encoder.encode(raw);
        assertTrue(encoder.matches(raw, encoded));
    }
}
