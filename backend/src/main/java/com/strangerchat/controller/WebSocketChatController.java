package com.strangerchat.controller;

import com.strangerchat.dto.WsIncomingMessage;
import com.strangerchat.security.StompPrincipal;
import com.strangerchat.security.UserPrincipal;
import com.strangerchat.service.ChatRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatController.class);

    private final ChatRoomService chatRoomService;

    public WebSocketChatController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @MessageMapping("/queue.join")
    public void joinQueue(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        UserPrincipal userPrincipal = extractUserPrincipal(principal);
        String sessionId = headerAccessor.getSessionId();

        if (userPrincipal != null && sessionId != null) {
            log.info("Queue join request from user {} (session {})", userPrincipal.getPrincipalKey(), sessionId);
            chatRoomService.joinQueue(userPrincipal, sessionId);
        }
    }

    @MessageMapping("/queue.leave")
    public void leaveQueue(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            chatRoomService.leaveQueue(sessionId);
        }
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload WsIncomingMessage message,
                            Principal principal,
                            SimpMessageHeaderAccessor headerAccessor) {
        UserPrincipal userPrincipal = extractUserPrincipal(principal);
        String sessionId = headerAccessor.getSessionId();

        if (userPrincipal != null && sessionId != null && message != null) {
            chatRoomService.handleSendMessage(sessionId, userPrincipal, message.getRoomId(), message.getContent());
        }
    }

    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload WsIncomingMessage message,
                                Principal principal,
                                SimpMessageHeaderAccessor headerAccessor) {
        UserPrincipal userPrincipal = extractUserPrincipal(principal);
        String sessionId = headerAccessor.getSessionId();

        if (userPrincipal != null && sessionId != null && message != null) {
            boolean isTyping = Boolean.TRUE.equals(message.getIsTyping());
            chatRoomService.handleTyping(sessionId, userPrincipal, message.getRoomId(), isTyping);
        }
    }

    @MessageMapping("/chat.next")
    public void nextStranger(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        UserPrincipal userPrincipal = extractUserPrincipal(principal);
        String sessionId = headerAccessor.getSessionId();

        if (userPrincipal != null && sessionId != null) {
            log.info("Next stranger requested by user {} (session {})", userPrincipal.getPrincipalKey(), sessionId);
            chatRoomService.leaveRoom(sessionId, userPrincipal, "User clicked Next", true);
        }
    }

    @MessageMapping("/chat.leave")
    public void stopChat(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        UserPrincipal userPrincipal = extractUserPrincipal(principal);
        String sessionId = headerAccessor.getSessionId();

        if (userPrincipal != null && sessionId != null) {
            log.info("Stop chat requested by user {} (session {})", userPrincipal.getPrincipalKey(), sessionId);
            chatRoomService.leaveRoom(sessionId, userPrincipal, "User clicked Stop", false);
        }
    }

    private UserPrincipal extractUserPrincipal(Principal principal) {
        if (principal instanceof Authentication auth) {
            if (auth.getPrincipal() instanceof StompPrincipal sp) {
                return sp.getUserPrincipal();
            } else if (auth.getPrincipal() instanceof UserPrincipal up) {
                return up;
            }
        }
        return null;
    }
}
