package com.strangerchat.service;

import com.strangerchat.security.UserPrincipal;

import java.util.Optional;

public interface MatchmakingService {

    /**
     * Adds user to the waiting matchmaking queue.
     */
    void addToQueue(UserPrincipal principal, String sessionId, String lastPartnerKey);

    /**
     * Removes user from the waiting matchmaking queue by their WebSocket sessionId.
     */
    void removeFromQueue(String sessionId);

    /**
     * Attempts to find and match two compatible candidates.
     */
    Optional<MatchResult> tryMatch();

    /**
     * Returns the current number of users waiting in the queue.
     */
    int getQueueLength();

    /**
     * Removes candidate on disconnect.
     */
    void handleDisconnect(String sessionId);

    record MatchResult(
            UserPrincipal user1,
            String sessionId1,
            UserPrincipal user2,
            String sessionId2
    ) {}
}
