package com.strangerchat.controller;

import com.strangerchat.dto.OnlineStatsDto;
import com.strangerchat.service.ChatRoomService;
import com.strangerchat.service.MatchmakingService;
import com.strangerchat.service.OnlineSessionTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "Live user and room statistics")
public class StatsController {

    private final OnlineSessionTracker sessionTracker;
    private final ChatRoomService chatRoomService;
    private final MatchmakingService matchmakingService;

    public StatsController(OnlineSessionTracker sessionTracker,
                           ChatRoomService chatRoomService,
                           MatchmakingService matchmakingService) {
        this.sessionTracker = sessionTracker;
        this.chatRoomService = chatRoomService;
        this.matchmakingService = matchmakingService;
    }

    @GetMapping("/online")
    @Operation(summary = "Get online users and active room count")
    public ResponseEntity<OnlineStatsDto> getOnlineStats() {
        int online = Math.max(1, sessionTracker.getOnlineCount());
        int rooms = chatRoomService.getActiveRoomCount();
        int queue = matchmakingService.getQueueLength();

        return ResponseEntity.ok(OnlineStatsDto.builder()
                .onlineUsers(online)
                .activeRooms(rooms)
                .queueLength(queue)
                .build());
    }
}
