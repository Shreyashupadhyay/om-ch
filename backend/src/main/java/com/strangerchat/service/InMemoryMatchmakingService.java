package com.strangerchat.service;

import com.strangerchat.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InMemoryMatchmakingService implements MatchmakingService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryMatchmakingService.class);

    private final ModerationService moderationService;
    private final List<QueueEntry> queue = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public InMemoryMatchmakingService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    public static class QueueEntry {
        private final UserPrincipal principal;
        private final String sessionId;
        private final String lastPartnerKey;
        private final Instant joinedAt;

        public QueueEntry(UserPrincipal principal, String sessionId, String lastPartnerKey) {
            this.principal = principal;
            this.sessionId = sessionId;
            this.lastPartnerKey = lastPartnerKey;
            this.joinedAt = Instant.now();
        }

        public UserPrincipal getPrincipal() { return principal; }
        public String getSessionId() { return sessionId; }
        public String getLastPartnerKey() { return lastPartnerKey; }
        public Instant getJoinedAt() { return joinedAt; }
    }

    @Override
    public void addToQueue(UserPrincipal principal, String sessionId, String lastPartnerKey) {
        lock.lock();
        try {
            removeFromQueueInternal(sessionId);
            queue.removeIf(entry -> entry.getPrincipal().getPrincipalKey().equals(principal.getPrincipalKey()));

            queue.add(new QueueEntry(principal, sessionId, lastPartnerKey));
            log.info("User {} (session {}) joined queue. Current queue length: {}",
                    principal.getPrincipalKey(), sessionId, queue.size());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeFromQueue(String sessionId) {
        lock.lock();
        try {
            removeFromQueueInternal(sessionId);
        } finally {
            lock.unlock();
        }
    }

    private void removeFromQueueInternal(String sessionId) {
        queue.removeIf(entry -> entry.getSessionId().equals(sessionId));
    }

    @Override
    public void handleDisconnect(String sessionId) {
        removeFromQueue(sessionId);
    }

    @Override
    public int getQueueLength() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<MatchResult> tryMatch() {
        lock.lock();
        try {
            if (queue.size() < 2) {
                return Optional.empty();
            }

            QueueEntry first = queue.get(0);

            for (int i = 1; i < queue.size(); i++) {
                QueueEntry candidate = queue.get(i);

                if (isCompatible(first, candidate)) {
                    queue.remove(i);
                    queue.remove(0);

                    log.info("Match found: {} and {}",
                            first.getPrincipal().getPrincipalKey(),
                            candidate.getPrincipal().getPrincipalKey());

                    return Optional.of(new MatchResult(
                            first.getPrincipal(),
                            first.getSessionId(),
                            candidate.getPrincipal(),
                            candidate.getSessionId()
                    ));
                }
            }

            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    private boolean isCompatible(QueueEntry a, QueueEntry b) {
        if (a.getPrincipal().getPrincipalKey().equals(b.getPrincipal().getPrincipalKey())) {
            return false;
        }

        if (a.getSessionId().equals(b.getSessionId())) {
            return false;
        }

        if (!a.getPrincipal().isGuest() && !b.getPrincipal().isGuest()) {
            if (moderationService.areUsersBlocked(a.getPrincipal().getId(), b.getPrincipal().getId())) {
                return false;
            }
        }

        if (a.getLastPartnerKey() != null && a.getLastPartnerKey().equals(b.getPrincipal().getPrincipalKey())) {
            long waitingSeconds = Math.min(
                    Instant.now().getEpochSecond() - a.getJoinedAt().getEpochSecond(),
                    Instant.now().getEpochSecond() - b.getJoinedAt().getEpochSecond()
            );
            if (queue.size() > 2 || waitingSeconds < 8) {
                return false;
            }
        }

        if (b.getLastPartnerKey() != null && b.getLastPartnerKey().equals(a.getPrincipal().getPrincipalKey())) {
            long waitingSeconds = Math.min(
                    Instant.now().getEpochSecond() - a.getJoinedAt().getEpochSecond(),
                    Instant.now().getEpochSecond() - b.getJoinedAt().getEpochSecond()
            );
            if (queue.size() > 2 || waitingSeconds < 8) {
                return false;
            }
        }

        return true;
    }

    @Scheduled(fixedRate = 30000)
    public void cleanupTimeouts() {
        lock.lock();
        try {
            Instant threshold = Instant.now().minusSeconds(300);
            Iterator<QueueEntry> iterator = queue.iterator();
            while (iterator.hasNext()) {
                QueueEntry entry = iterator.next();
                if (entry.getJoinedAt().isBefore(threshold)) {
                    log.info("Removing timed-out user from queue: {}", entry.getPrincipal().getPrincipalKey());
                    iterator.remove();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
