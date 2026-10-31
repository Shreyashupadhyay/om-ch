package com.strangerchat.controller;

import com.strangerchat.dto.ConversationDetailDto;
import com.strangerchat.dto.ConversationSummaryDto;
import com.strangerchat.exception.UnauthorizedException;
import com.strangerchat.security.UserPrincipal;
import com.strangerchat.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations", description = "Endpoints for viewing and managing saved conversation histories")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    @Operation(summary = "List current user's past saved conversations with pagination")
    public ResponseEntity<Page<ConversationSummaryDto>> listConversations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        if (principal.isGuest()) {
            throw new UnauthorizedException("Guest users do not have persistent conversation histories");
        }

        Page<ConversationSummaryDto> result = conversationService.getUserConversations(
                principal,
                PageRequest.of(page, size, Sort.by("startedAt").descending())
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full transcript of a specific conversation")
    public ResponseEntity<ConversationDetailDto> getConversationDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal.isGuest()) {
            throw new UnauthorizedException("Guest users do not have persistent conversation histories");
        }

        ConversationDetailDto detail = conversationService.getConversationDetail(id, principal);
        return ResponseEntity.ok(detail);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a conversation from user history")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal.isGuest()) {
            throw new UnauthorizedException("Guest users cannot delete conversations");
        }

        conversationService.deleteConversationForUser(id, principal);
        return ResponseEntity.noContent().build();
    }
}
