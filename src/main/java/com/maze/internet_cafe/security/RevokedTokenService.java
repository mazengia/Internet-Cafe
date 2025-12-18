package com.maze.internet_cafe.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RevokedTokenService {
    // store token -> expiryEpochMillis
    private final Map<String, Long> revoked = new ConcurrentHashMap<>();

    public void revokeToken(String token, long expiryEpochMillis) {
        if (token == null || token.isBlank()) return;
        revoked.put(token, expiryEpochMillis);
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) return false;
        Long exp = revoked.get(token);
        if (exp == null) return false;
        if (exp < Instant.now().toEpochMilli()) {
            revoked.remove(token);
            return false;
        }
        return true;
    }

    // Periodically cleanup expired entries every 10 minutes
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanup() {
        long now = Instant.now().toEpochMilli();
        Iterator<Map.Entry<String, Long>> it = revoked.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            if (e.getValue() < now) it.remove();
        }
    }
}

