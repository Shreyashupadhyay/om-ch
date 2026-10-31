package com.strangerchat.service;

import com.strangerchat.dto.ReportRequest;
import com.strangerchat.entity.*;
import com.strangerchat.exception.BadRequestException;
import com.strangerchat.repository.BlockRepository;
import com.strangerchat.repository.ConversationRepository;
import com.strangerchat.repository.ReportRepository;
import com.strangerchat.repository.UserRepository;
import com.strangerchat.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    private final ReportRepository reportRepository;
    private final BlockRepository blockRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ModerationService(ReportRepository reportRepository,
                             BlockRepository blockRepository,
                             ConversationRepository conversationRepository,
                             UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.blockRepository = blockRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void submitReport(ReportRequest request, UserPrincipal principal) {
        Report report = Report.builder()
                .reason(request.getReason())
                .details(request.getDetails())
                .build();

        if (principal.isGuest()) {
            report.setReporterGuestId(principal.getGuestId());
        } else {
            userRepository.findById(principal.getId()).ifPresent(report::setReporterUser);
        }

        if (request.getConversationId() != null) {
            conversationRepository.findById(request.getConversationId()).ifPresent(report::setConversation);
        }

        reportRepository.save(report);
        log.info("Report filed by {} for conversation {}", principal.getPrincipalKey(), request.getConversationId());
    }

    @Transactional
    public void blockUser(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BadRequestException("You cannot block yourself");
        }

        BlockId blockId = new BlockId(blockerId, blockedId);
        if (!blockRepository.existsById(blockId)) {
            Block block = new Block(blockId);
            userRepository.findById(blockerId).ifPresent(block::setBlockerUser);
            userRepository.findById(blockedId).ifPresent(block::setBlockedUser);
            blockRepository.save(block);
            log.info("User {} blocked user {}", blockerId, blockedId);
        }
    }

    @Transactional(readOnly = true)
    public boolean areUsersBlocked(UUID userA, UUID userB) {
        if (userA == null || userB == null) {
            return false;
        }
        return blockRepository.isBlockedEitherWay(userA, userB);
    }
}
