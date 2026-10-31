package com.strangerchat.config;

import com.strangerchat.service.ChatRoomService;
import com.strangerchat.service.OnlineSessionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final ChatRoomService chatRoomService;
    private final OnlineSessionTracker sessionTracker;

    public WebSocketEventListener(ChatRoomService chatRoomService, OnlineSessionTracker sessionTracker) {
        this.chatRoomService = chatRoomService;
        this.sessionTracker = sessionTracker;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            log.info("WebSocket disconnected: sessionId {}", sessionId);
            sessionTracker.unregisterSession(sessionId);
            chatRoomService.handleDisconnect(sessionId);
        }
    }
}
