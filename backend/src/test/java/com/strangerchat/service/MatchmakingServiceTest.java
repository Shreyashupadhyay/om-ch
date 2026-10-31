package com.strangerchat.service;

import com.strangerchat.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchmakingServiceTest {

    @Mock
    private ModerationService moderationService;

    @InjectMocks
    private InMemoryMatchmakingService matchmakingService;

    private UserPrincipal guest1;
    private UserPrincipal guest2;
    private UserPrincipal registeredUser1;
    private UserPrincipal registeredUser2;

    @BeforeEach
    void setUp() {
        guest1 = UserPrincipal.createGuest("g1", "Guest-1001");
        guest2 = UserPrincipal.createGuest("g2", "Guest-1002");

        registeredUser1 = UserPrincipal.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .isGuest(false)
                .build();

        registeredUser2 = UserPrincipal.builder()
                .id(UUID.randomUUID())
                .username("user2")
                .isGuest(false)
                .build();
    }

    @Test
    void testSingleUserDoesNotMatch() {
        matchmakingService.addToQueue(guest1, "sess-1", null);
        assertEquals(1, matchmakingService.getQueueLength());

        Optional<MatchmakingService.MatchResult> match = matchmakingService.tryMatch();
        assertTrue(match.isEmpty());
    }

    @Test
    void testTwoDifferentGuestsMatchSuccessfully() {
        matchmakingService.addToQueue(guest1, "sess-1", null);
        matchmakingService.addToQueue(guest2, "sess-2", null);
        assertEquals(2, matchmakingService.getQueueLength());

        Optional<MatchmakingService.MatchResult> match = matchmakingService.tryMatch();
        assertTrue(match.isPresent());
        assertEquals(0, matchmakingService.getQueueLength());

        MatchmakingService.MatchResult result = match.get();
        assertEquals("sess-1", result.sessionId1());
        assertEquals("sess-2", result.sessionId2());
    }

    @Test
    void testUserNeverMatchesWithThemselves() {
        // Same user queueing twice (e.g. rapid clicks or second tab with same account)
        matchmakingService.addToQueue(registeredUser1, "sess-1a", null);
        matchmakingService.addToQueue(registeredUser1, "sess-1b", null);

        // Queue removes previous entry of same user
        assertEquals(1, matchmakingService.getQueueLength());

        Optional<MatchmakingService.MatchResult> match = matchmakingService.tryMatch();
        assertTrue(match.isEmpty());
    }

    @Test
    void testBlockedUsersDoNotMatch() {
        when(moderationService.areUsersBlocked(registeredUser1.getId(), registeredUser2.getId()))
                .thenReturn(true);

        matchmakingService.addToQueue(registeredUser1, "sess-reg1", null);
        matchmakingService.addToQueue(registeredUser2, "sess-reg2", null);

        Optional<MatchmakingService.MatchResult> match = matchmakingService.tryMatch();
        assertTrue(match.isEmpty());
        assertEquals(2, matchmakingService.getQueueLength());
    }

    @Test
    void testRemoveFromQueueAndDisconnect() {
        matchmakingService.addToQueue(guest1, "sess-1", null);
        assertEquals(1, matchmakingService.getQueueLength());

        matchmakingService.handleDisconnect("sess-1");
        assertEquals(0, matchmakingService.getQueueLength());
    }
}
