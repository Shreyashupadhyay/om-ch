package com.strangerchat.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    @Value("${app.rate-limit.auth-capacity:10}")
    private int authCapacity;

    @Value("${app.rate-limit.auth-refill-tokens:10}")
    private int authRefillTokens;

    @Value("${app.rate-limit.auth-refill-period-seconds:60}")
    private long authRefillPeriodSeconds;

    @Value("${app.rate-limit.message-capacity:10}")
    private int messageCapacity;

    @Value("${app.rate-limit.message-refill-tokens:10}")
    private int messageRefillTokens;

    @Value("${app.rate-limit.message-refill-period-seconds:2}")
    private long messageRefillPeriodSeconds;

    private final Map<String, TokenBucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, TokenBucket> messageBuckets = new ConcurrentHashMap<>();

    public boolean tryConsumeAuth(String ipAddress) {
        TokenBucket bucket = authBuckets.computeIfAbsent(ipAddress,
                k -> new TokenBucket(authCapacity, authRefillTokens, authRefillPeriodSeconds * 1000L));
        return bucket.tryConsume(1);
    }

    public boolean tryConsumeMessage(String principalKey) {
        TokenBucket bucket = messageBuckets.computeIfAbsent(principalKey,
                k -> new TokenBucket(messageCapacity, messageRefillTokens, messageRefillPeriodSeconds * 1000L));
        return bucket.tryConsume(1);
    }

    public void cleanupStaleBuckets() {
        long now = System.currentTimeMillis();
        authBuckets.entrySet().removeIf(entry -> now - entry.getValue().getLastRefillTime() > 300_000L);
        messageBuckets.entrySet().removeIf(entry -> now - entry.getValue().getLastRefillTime() > 300_000L);
    }

    private static class TokenBucket {
        private final int capacity;
        private final int refillTokens;
        private final long refillIntervalMillis;

        private double tokens;
        private long lastRefillTime;

        public TokenBucket(int capacity, int refillTokens, long refillIntervalMillis) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillIntervalMillis = Math.max(1, refillIntervalMillis);
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume(int cost) {
            refill();
            if (tokens >= cost) {
                tokens -= cost;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed > 0) {
                double tokensToAdd = (elapsed * (double) refillTokens) / (double) refillIntervalMillis;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }

        public synchronized long getLastRefillTime() {
            return lastRefillTime;
        }
    }
}
