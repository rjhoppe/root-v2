package com.github.rjhoppe.root_v2.services;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage rate limiting based on IP address and a fixed time window.
 */
@Service
public class RateLimitingService {

    // Stores <IP Address, <Window Start Time (epoch millis), Request Count>>
    private final Map<String, Map<Long, Integer>> requestCounts = new ConcurrentHashMap<>();
    
    // Default rate limit properties
    private static final int DEFAULT_LIMIT = 5; // 5 requests
    private static final long DEFAULT_WINDOW_MILLIS = 10000; // within 10 seconds

    /**
     * Checks if a request from a given IP address is allowed using default rate limits.
     * @param ipAddress The IP address of the client.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean isAllowed(String ipAddress) {
        return isAllowed(ipAddress, DEFAULT_LIMIT, DEFAULT_WINDOW_MILLIS);
    }

    /**
     * Checks if a request from a given IP address is allowed within a custom rate limit.
     * @param ipAddress The IP address of the client.
     * @param limit The maximum number of requests allowed.
     * @param windowMillis The duration of the window in milliseconds.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean isAllowed(String ipAddress, int limit, long windowMillis) {
        long currentTimeMillis = Instant.now().toEpochMilli();
        long currentWindowStart = (currentTimeMillis / windowMillis) * windowMillis;

        Map<Long, Integer> ipWindowCounts = requestCounts.computeIfAbsent(ipAddress, k -> new ConcurrentHashMap<>());

        Integer count = ipWindowCounts.compute(currentWindowStart, (k, v) -> (v == null) ? 1 : v + 1);

        // Clean up old windows to prevent memory leak
        if (ipWindowCounts.size() > 2) { 
             ipWindowCounts.keySet().removeIf(window -> window < currentWindowStart - windowMillis);
        }

        return count <= limit;
    }
}
