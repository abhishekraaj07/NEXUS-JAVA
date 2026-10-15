package com.nexus.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private static class Window {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private volatile long lastAccessTime = System.currentTimeMillis();

        synchronized boolean tryAcquire(long now, int maxRequests, long windowMs) {
            this.lastAccessTime = now;
            while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > windowMs) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }

        synchronized boolean isStale(long thresholdTime) {
            return lastAccessTime < thresholdTime;
        }
    }

    private final Map<String, Window> clientWindows = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        Window window = clientWindows.computeIfAbsent(key, k -> new Window());
        return window.tryAcquire(now, maxRequests, windowMs);
    }

    @Scheduled(fixedRate = 120000)
    public void cleanupStaleWindows() {
        long threshold = System.currentTimeMillis() - 60000;
        clientWindows.entrySet().removeIf(entry -> entry.getValue().isStale(threshold));
    }
}
