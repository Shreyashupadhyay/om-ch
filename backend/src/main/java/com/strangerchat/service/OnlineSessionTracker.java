package com.strangerchat.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OnlineSessionTracker {

    // sessionId -> principalKey
    private final Map<String, String> sessionPrincipalMap = new ConcurrentHashMap<>();
    private final AtomicInteger onlineCount = new AtomicInteger(0);

    public void registerSession(String sessionId, String principalKey) {
        if (sessionPrincipalMap.put(sessionId, principalKey) == null) {
            onlineCount.incrementAndGet();
        }
    }

    public void unregisterSession(String sessionId) {
        if (sessionPrincipalMap.remove(sessionId) != null) {
            onlineCount.decrementAndGet();
        }
    }

    public int getOnlineCount() {
        return Math.max(0, onlineCount.get());
    }

    public String getPrincipalKey(String sessionId) {
        return sessionPrincipalMap.get(sessionId);
    }
}
