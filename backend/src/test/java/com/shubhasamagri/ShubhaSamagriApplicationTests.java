package com.shubhasamagri;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify the Spring application context loads correctly.
 * Uses H2 in-memory database (no external dependencies required).
 */
@SpringBootTest
@ActiveProfiles("test")
class ShubhaSamagriApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that all beans are correctly wired and app starts without errors
    }
}
