package com.strangerchat.config;

import com.strangerchat.security.JwtTokenProvider;
import com.strangerchat.security.StompPrincipal;
import com.strangerchat.security.UserPrincipal;
import com.strangerchat.service.OnlineSessionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JwtTokenProvider tokenProvider;
    private final OnlineSessionTracker sessionTracker;

    public WebSocketAuthChannelInterceptor(JwtTokenProvider tokenProvider, OnlineSessionTracker sessionTracker) {
        this.tokenProvider = tokenProvider;
        this.sessionTracker = sessionTracker;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                UserPrincipal principal = tokenProvider.getPrincipalFromToken(token);
                String sessionId = accessor.getSessionId();

                StompPrincipal stompPrincipal = new StompPrincipal(sessionId, principal);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(stompPrincipal, null, principal.getAuthorities());

                accessor.setUser(authentication);
                sessionTracker.registerSession(sessionId, principal.getPrincipalKey());

                log.info("WebSocket CONNECT authenticated for: {} (sessionId: {})",
                        principal.getPrincipalKey(), sessionId);
            } else {
                log.warn("WebSocket CONNECT attempted without valid JWT token");
                throw new IllegalArgumentException("Invalid or missing authentication token");
            }
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
            return header;
        }

        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        return null;
    }
}
