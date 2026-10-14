package com.nexus.backend.service;

import org.springframework.stereotype.Service;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private static class Window {
        private final Deque<Long> timestamps = new ArrayDeque<>();
    }

    private final Map<String, Window> clientWindows = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        Window window = clientWindows.computeIfAbsent(key, k -> new Window());
        Deque<Long> timestamps = window.timestamps;

        while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > windowMs) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}
