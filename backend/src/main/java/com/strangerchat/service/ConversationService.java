package com.strangerchat.service;

import com.strangerchat.dto.ConversationDetailDto;
import com.strangerchat.dto.ConversationSummaryDto;
import com.strangerchat.dto.MessageDto;
import com.strangerchat.entity.Conversation;
import com.strangerchat.entity.Message;
import com.strangerchat.exception.ResourceNotFoundException;
import com.strangerchat.repository.ConversationRepository;
import com.strangerchat.repository.MessageRepository;
import com.strangerchat.repository.UserRepository;
import com.strangerchat.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Conversation startConversationIfEligible(UserPrincipal participant1, UserPrincipal participant2) {
        if (participant1.isGuest() && participant2.isGuest()) {
            return null;
        }

        Conversation conversation = Conversation.builder()
                .startedAt(Instant.now())
                .userOneIsGuest(participant1.isGuest())
                .userTwoIsGuest(participant2.isGuest())
                .build();

        if (!participant1.isGuest()) {
            userRepository.findById(participant1.getId()).ifPresent(conversation::setUserOne);
        }
        if (!participant2.isGuest()) {
            userRepository.findById(participant2.getId()).ifPresent(conversation::setUserTwo);
        }

        return conversationRepository.save(conversation);
    }

    @Transactional
    public void recordMessage(UUID conversationId, UserPrincipal sender, String content) {
        if (conversationId == null) {
            return;
        }

        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            Message message = Message.builder()
                    .conversation(conversation)
                    .content(content)
                    .senderLabel(sender.isGuest() ? "Stranger" : "User")
                    .sentAt(Instant.now())
                    .build();

            if (!sender.isGuest()) {
                userRepository.findById(sender.getId()).ifPresent(message::setSenderUser);
            }

            messageRepository.save(message);
        });
    }

    @Transactional
    public void endConversation(UUID conversationId, String endedBy) {
        if (conversationId == null) {
            return;
        }

        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (conversation.getEndedAt() == null) {
                conversation.setEndedAt(Instant.now());
                conversation.setEndedBy(endedBy);
                conversationRepository.save(conversation);
            }
        });
    }

    @Transactional(readOnly = true)
    public Page<ConversationSummaryDto> getUserConversations(UserPrincipal principal, Pageable pageable) {
        if (principal.isGuest()) {
            return Page.empty(pageable);
        }

        Page<Conversation> page = conversationRepository.findUserConversations(principal.getId(), pageable);
        return page.map(conv -> {
            Instant end = conv.getEndedAt() != null ? conv.getEndedAt() : Instant.now();
            long duration = Duration.between(conv.getStartedAt(), end).getSeconds();

            List<Message> msgs = conv.getMessages();
            int count = msgs != null ? msgs.size() : 0;
            String preview = (msgs != null && !msgs.isEmpty()) ? msgs.get(msgs.size() - 1).getContent() : "No messages";

            return ConversationSummaryDto.builder()
                    .id(conv.getId())
                    .startedAt(conv.getStartedAt())
                    .endedAt(conv.getEndedAt())
                    .durationSeconds(duration)
                    .messageCount(count)
                    .lastMessagePreview(preview)
                    .partnerLabel("Stranger")
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public ConversationDetailDto getConversationDetail(UUID conversationId, UserPrincipal principal) {
        if (principal.isGuest()) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        Conversation conv = conversationRepository.findUserConversationWithMessages(conversationId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        Instant end = conv.getEndedAt() != null ? conv.getEndedAt() : Instant.now();
        long duration = Duration.between(conv.getStartedAt(), end).getSeconds();

        List<MessageDto> messageDtos = conv.getMessages().stream().map(m -> {
            boolean fromMe = (m.getSenderUser() != null && m.getSenderUser().getId().equals(principal.getId()));
            return MessageDto.builder()
                    .id(m.getId())
                    .senderLabel(fromMe ? "You" : "Stranger")
                    .fromMe(fromMe)
                    .content(m.getContent())
                    .sentAt(m.getSentAt())
                    .build();
        }).toList();

        return ConversationDetailDto.builder()
                .id(conv.getId())
                .startedAt(conv.getStartedAt())
                .endedAt(conv.getEndedAt())
                .durationSeconds(duration)
                .endedBy(conv.getEndedBy())
                .partnerLabel("Stranger")
                .messages(messageDtos)
                .build();
    }

    @Transactional
    public void deleteConversationForUser(UUID conversationId, UserPrincipal principal) {
        if (principal.isGuest()) {
            return;
        }

        conversationRepository.findById(conversationId).ifPresent(conv -> {
            boolean isUserOne = conv.getUserOne() != null && conv.getUserOne().getId().equals(principal.getId());
            boolean isUserTwo = conv.getUserTwo() != null && conv.getUserTwo().getId().equals(principal.getId());

            if (isUserOne) {
                conv.setUserOneDeleted(true);
            }
            if (isUserTwo) {
                conv.setUserTwoDeleted(true);
            }

            boolean oneDead = conv.isUserOneDeleted() || conv.getUserOne() == null;
            boolean twoDead = conv.isUserTwoDeleted() || conv.getUserTwo() == null;

            if (oneDead && twoDead) {
                conversationRepository.delete(conv);
            } else {
                conversationRepository.save(conv);
            }
        });
    }
}
