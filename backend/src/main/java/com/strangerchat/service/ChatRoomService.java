package com.strangerchat.service;

import com.strangerchat.dto.WsEventMessage;
import com.strangerchat.dto.WsOutgoingMessage;
import com.strangerchat.entity.Conversation;
import com.strangerchat.security.RateLimiterService;
import com.strangerchat.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatRoomService {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomService.class);

    private final MatchmakingService matchmakingService;
    private final ConversationService conversationService;
    private final ProfanityFilterService profanityFilterService;
    private final RateLimiterService rateLimiterService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, ActiveRoom> roomsById = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToRoomId = new ConcurrentHashMap<>();
    private final Map<String, String> userLastPartnerKey = new ConcurrentHashMap<>();

    public ChatRoomService(MatchmakingService matchmakingService,
                           ConversationService conversationService,
                           ProfanityFilterService profanityFilterService,
                           RateLimiterService rateLimiterService,
                           SimpMessagingTemplate messagingTemplate) {
        this.matchmakingService = matchmakingService;
        this.conversationService = conversationService;
        this.profanityFilterService = profanityFilterService;
        this.rateLimiterService = rateLimiterService;
        this.messagingTemplate = messagingTemplate;
    }

    public static class ActiveRoom {
        private final String roomId;
        private final UserPrincipal user1;
        private final String sessionId1;
        private final UserPrincipal user2;
        private final String sessionId2;
        private final UUID conversationId;
        private final Instant createdAt;

        public ActiveRoom(String roomId, UserPrincipal user1, String sessionId1,
                          UserPrincipal user2, String sessionId2, UUID conversationId) {
            this.roomId = roomId;
            this.user1 = user1;
            this.sessionId1 = sessionId1;
            this.user2 = user2;
            this.sessionId2 = sessionId2;
            this.conversationId = conversationId;
            this.createdAt = Instant.now();
        }

        public String getRoomId() { return roomId; }
        public UserPrincipal getUser1() { return user1; }
        public String getSessionId1() { return sessionId1; }
        public UserPrincipal getUser2() { return user2; }
        public String getSessionId2() { return sessionId2; }
        public UUID getConversationId() { return conversationId; }
        public Instant getCreatedAt() { return createdAt; }

        public boolean containsSession(String sessionId) {
            return sessionId1.equals(sessionId) || sessionId2.equals(sessionId);
        }

        public String getPartnerSession(String mySessionId) {
            return sessionId1.equals(mySessionId) ? sessionId2 : sessionId1;
        }

        public UserPrincipal getPartnerPrincipal(String mySessionId) {
            return sessionId1.equals(mySessionId) ? user2 : user1;
        }

        public UserPrincipal getMyPrincipal(String mySessionId) {
            return sessionId1.equals(mySessionId) ? user1 : user2;
        }
    }

    public void joinQueue(UserPrincipal principal, String sessionId) {
        leaveRoom(sessionId, principal, "Joined new queue", false);

        String lastPartner = userLastPartnerKey.get(principal.getPrincipalKey());
        matchmakingService.addToQueue(principal, sessionId, lastPartner);

        sendEventToUser(sessionId, WsEventMessage.builder()
                .event(WsEventMessage.EventType.WAITING_IN_QUEUE)
                .message("Looking for someone you can chat with...")
                .payload(Map.of("queueLength", matchmakingService.getQueueLength()))
                .build());

        matchAndNotify();
    }

    public void leaveQueue(String sessionId) {
        matchmakingService.removeFromQueue(sessionId);
    }

    @Scheduled(fixedRate = 1000)
    public void scheduledMatchWorker() {
        matchAndNotify();
    }

    public void matchAndNotify() {
        Optional<MatchmakingService.MatchResult> matchOpt;
        while ((matchOpt = matchmakingService.tryMatch()).isPresent()) {
            MatchmakingService.MatchResult match = matchOpt.get();

            String roomId = UUID.randomUUID().toString();

            Conversation conversation = conversationService.startConversationIfEligible(
                    match.user1(), match.user2()
            );
            UUID conversationId = (conversation != null) ? conversation.getId() : null;

            ActiveRoom room = new ActiveRoom(
                    roomId,
                    match.user1(), match.sessionId1(),
                    match.user2(), match.sessionId2(),
                    conversationId
            );

            roomsById.put(roomId, room);
            sessionToRoomId.put(match.sessionId1(), roomId);
            sessionToRoomId.put(match.sessionId2(), roomId);

            userLastPartnerKey.put(match.user1().getPrincipalKey(), match.user2().getPrincipalKey());
            userLastPartnerKey.put(match.user2().getPrincipalKey(), match.user1().getPrincipalKey());

            log.info("Room created: {} (convId: {}) between {} and {}",
                    roomId, conversationId, match.user1().getPrincipalKey(), match.user2().getPrincipalKey());

            Map<String, Object> payload1 = Map.of(
                    "roomId", roomId,
                    "conversationId", conversationId != null ? conversationId.toString() : "",
                    "partnerLabel", "Stranger"
            );
            Map<String, Object> payload2 = Map.of(
                    "roomId", roomId,
                    "conversationId", conversationId != null ? conversationId.toString() : "",
                    "partnerLabel", "Stranger"
            );

            sendEventToUser(match.sessionId1(), WsEventMessage.builder()
                    .event(WsEventMessage.EventType.MATCHED)
                    .roomId(roomId)
                    .message("Connected with a stranger. Say hi!")
                    .payload(payload1)
                    .build());

            sendEventToUser(match.sessionId2(), WsEventMessage.builder()
                    .event(WsEventMessage.EventType.MATCHED)
                    .roomId(roomId)
                    .message("Connected with a stranger. Say hi!")
                    .payload(payload2)
                    .build());
        }
    }

    public void handleSendMessage(String sessionId, UserPrincipal sender, String roomId, String rawContent) {
        ActiveRoom room = roomsById.get(roomId);
        if (room == null || !room.containsSession(sessionId)) {
            sendErrorEvent(sessionId, "Chat room not found or you are not a participant");
            return;
        }

        if (!rateLimiterService.tryConsumeMessage(sender.getPrincipalKey())) {
            sendErrorEvent(sessionId, "You are sending messages too fast. Please slow down.");
            return;
        }

        String sanitized = profanityFilterService.sanitizeAndFilter(rawContent);
        if (sanitized.isBlank()) {
            return;
        }

        conversationService.recordMessage(room.getConversationId(), sender, sanitized);

        String messageId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        WsOutgoingMessage senderMsg = WsOutgoingMessage.builder()
                .id(messageId)
                .roomId(roomId)
                .senderLabel("You")
                .fromMe(true)
                .content(sanitized)
                .timestamp(now)
                .build();
        sendMessageToUser(sessionId, senderMsg);

        String partnerSession = room.getPartnerSession(sessionId);
        WsOutgoingMessage partnerMsg = WsOutgoingMessage.builder()
                .id(messageId)
                .roomId(roomId)
                .senderLabel("Stranger")
                .fromMe(false)
                .content(sanitized)
                .timestamp(now)
                .build();
        sendMessageToUser(partnerSession, partnerMsg);
    }

    public void handleTyping(String sessionId, UserPrincipal sender, String roomId, boolean isTyping) {
        ActiveRoom room = roomsById.get(roomId);
        if (room != null && room.containsSession(sessionId)) {
            String partnerSession = room.getPartnerSession(sessionId);
            sendEventToUser(partnerSession, WsEventMessage.builder()
                    .event(isTyping ? WsEventMessage.EventType.PARTNER_TYPING : WsEventMessage.EventType.PARTNER_STOPPED_TYPING)
                    .roomId(roomId)
                    .message(isTyping ? "Stranger is typing..." : "")
                    .build());
        }
    }

    public void leaveRoom(String sessionId, UserPrincipal user, String reason, boolean requestNext) {
        String roomId = sessionToRoomId.remove(sessionId);
        if (roomId == null) {
            return;
        }

        ActiveRoom room = roomsById.remove(roomId);
        if (room != null) {
            String partnerSession = room.getPartnerSession(sessionId);
            sessionToRoomId.remove(partnerSession);

            conversationService.endConversation(room.getConversationId(), user.getUsername());

            sendEventToUser(partnerSession, WsEventMessage.builder()
                    .event(WsEventMessage.EventType.PARTNER_LEFT)
                    .roomId(roomId)
                    .message("Stranger has disconnected.")
                    .payload(Map.of("conversationId", room.getConversationId() != null ? room.getConversationId().toString() : ""))
                    .build());

            sendEventToUser(sessionId, WsEventMessage.builder()
                    .event(WsEventMessage.EventType.ROOM_ENDED)
                    .roomId(roomId)
                    .message("You left the chat.")
                    .payload(Map.of("conversationId", room.getConversationId() != null ? room.getConversationId().toString() : ""))
                    .build());

            log.info("Room {} closed. Reason: {}", roomId, reason);
        }

        if (requestNext) {
            joinQueue(user, sessionId);
        }
    }

    public void handleDisconnect(String sessionId) {
        matchmakingService.handleDisconnect(sessionId);
        String roomId = sessionToRoomId.remove(sessionId);
        if (roomId != null) {
            ActiveRoom room = roomsById.remove(roomId);
            if (room != null) {
                String partnerSession = room.getPartnerSession(sessionId);
                sessionToRoomId.remove(partnerSession);
                conversationService.endConversation(room.getConversationId(), "Disconnect");

                sendEventToUser(partnerSession, WsEventMessage.builder()
                        .event(WsEventMessage.EventType.PARTNER_LEFT)
                        .roomId(roomId)
                        .message("Stranger has disconnected.")
                        .payload(Map.of("conversationId", room.getConversationId() != null ? room.getConversationId().toString() : ""))
                        .build());
            }
        }
    }

    public int getActiveRoomCount() {
        return roomsById.size();
    }

    public ActiveRoom getRoom(String roomId) {
        return roomsById.get(roomId);
    }

    public ActiveRoom getRoomBySession(String sessionId) {
        String roomId = sessionToRoomId.get(sessionId);
        return roomId != null ? roomsById.get(roomId) : null;
    }

    private void sendMessageToUser(String sessionId, WsOutgoingMessage message) {
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/messages", message);
    }

    private void sendEventToUser(String sessionId, WsEventMessage event) {
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/events", event);
    }

    private void sendErrorEvent(String sessionId, String message) {
        sendEventToUser(sessionId, WsEventMessage.builder()
                .event(WsEventMessage.EventType.ERROR)
                .message(message)
                .build());
    }
}
