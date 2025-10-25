package com.selimhorri.app.unit;

import com.selimhorri.app.config.encoder.EncoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class EncoderConfigUnitTest {

    @Test
    void testPasswordEncoderBeanIsBCrypt() {
        EncoderConfig config = new EncoderConfig();
        PasswordEncoder encoder = config.getPasswordEncoder();
        assertNotNull(encoder);
        String raw = "password123";
        String encoded = encoder.encode(raw);
        assertTrue(encoder.matches(raw, encoded));
    }
}
