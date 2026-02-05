package com.github.rjhoppe.root_v2.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    void testIsAllowed_WithinLimit() {
        String ip = "192.168.1.1";
        // Using a limit of 5 requests per 10 seconds (default)
        assertTrue(rateLimitingService.isAllowed(ip)); // 1st request
        assertTrue(rateLimitingService.isAllowed(ip)); // 2nd request
        assertTrue(rateLimitingService.isAllowed(ip)); // 3rd request
        assertTrue(rateLimitingService.isAllowed(ip)); // 4th request
        assertTrue(rateLimitingService.isAllowed(ip)); // 5th request
    }

    @Test
    void testIsAllowed_ExceedsLimit() {
        String ip = "192.168.1.2";
        int limit = 3;
        long windowMillis = 5000; // 5 seconds

        // First 3 requests should be allowed
        assertTrue(rateLimitingService.isAllowed(ip, limit, windowMillis));
        assertTrue(rateLimitingService.isAllowed(ip, limit, windowMillis));
        assertTrue(rateLimitingService.isAllowed(ip, limit, windowMillis));

        // 4th request should be blocked
        assertFalse(rateLimitingService.isAllowed(ip, limit, windowMillis));
    }

    @Test
    void testIsAllowed_ResetsAfterWindow() throws InterruptedException {
        String ip = "192.168.1.3";
        int limit = 2;
        long windowMillis = 100; // A very short window of 100ms for testing

        // First 2 requests are allowed
        assertTrue(rateLimitingService.isAllowed(ip, limit, windowMillis));
        assertTrue(rateLimitingService.isAllowed(ip, limit, windowMillis));

        // 3rd is blocked
        assertFalse(rateLimitingService.isAllowed(ip, limit, windowMillis));

        // Wait for the window to pass
        Thread.sleep(windowMillis + 10);

        // After the window has passed, a new request should be allowed
        assertTrue(rateLimitingService.isAllowed(ip, limit, windowMillis));
    }

    @Test
    void testIsAllowed_DifferentIps() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";
        int limit = 1;

        // First request for ip1 is allowed
        assertTrue(rateLimitingService.isAllowed(ip1, limit, 10000));
        // Second request for ip1 is blocked
        assertFalse(rateLimitingService.isAllowed(ip1, limit, 10000));

        // First request for ip2 should be allowed, as it's a different IP
        assertTrue(rateLimitingService.isAllowed(ip2, limit, 10000));
    }
}
