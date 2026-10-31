package com.strangerchat.controller;

import com.strangerchat.dto.BlockRequest;
import com.strangerchat.dto.ReportRequest;
import com.strangerchat.security.UserPrincipal;
import com.strangerchat.service.ChatRoomService;
import com.strangerchat.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@Tag(name = "Moderation", description = "Safety, reporting, and user blocking endpoints")
public class ModerationController {

    private static final Logger log = LoggerFactory.getLogger(ModerationController.class);

    private final ModerationService moderationService;
    private final ChatRoomService chatRoomService;

    public ModerationController(ModerationService moderationService, ChatRoomService chatRoomService) {
        this.moderationService = moderationService;
        this.chatRoomService = chatRoomService;
    }

    @PostMapping("/api/reports")
    @Operation(summary = "Submit a moderation report for inappropriate behavior or spam")
    public ResponseEntity<Map<String, String>> submitReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        moderationService.submitReport(request, principal);
        return ResponseEntity.ok(Map.of("message", "Report submitted successfully. Thank you for keeping our community safe."));
    }

    @PostMapping("/api/blocks")
    @Operation(summary = "Block a chat partner so you are never matched with them again")
    public ResponseEntity<Map<String, String>> blockPartner(
            @RequestBody BlockRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal.isGuest()) {
            return ResponseEntity.ok(Map.of("message", "Guest blocked current stranger."));
        }

        if (request.getRoomId() != null) {
            ChatRoomService.ActiveRoom room = chatRoomService.getRoom(request.getRoomId());
            if (room != null) {
                UserPrincipal partner = room.getUser1().getPrincipalKey().equals(principal.getPrincipalKey())
                        ? room.getUser2()
                        : room.getUser1();

                if (!partner.isGuest() && partner.getId() != null) {
                    moderationService.blockUser(principal.getId(), partner.getId());
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "User blocked successfully. You will not be rematched with them."));
    }
}
