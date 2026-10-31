package com.strangerchat.dto;

import java.time.Instant;
import java.util.UUID;

public class ConversationSummaryDto {
    private UUID id;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationSeconds;
    private int messageCount;
    private String lastMessagePreview;
    private String partnerLabel;

    public ConversationSummaryDto() {}

    public ConversationSummaryDto(UUID id, Instant startedAt, Instant endedAt, Long durationSeconds,
                                  int messageCount, String lastMessagePreview, String partnerLabel) {
        this.id = id;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.messageCount = messageCount;
        this.lastMessagePreview = lastMessagePreview;
        this.partnerLabel = partnerLabel;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }

    public String getPartnerLabel() { return partnerLabel; }
    public void setPartnerLabel(String partnerLabel) { this.partnerLabel = partnerLabel; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private Instant startedAt;
        private Instant endedAt;
        private Long durationSeconds;
        private int messageCount;
        private String lastMessagePreview;
        private String partnerLabel;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder endedAt(Instant endedAt) { this.endedAt = endedAt; return this; }
        public Builder durationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; return this; }
        public Builder messageCount(int messageCount) { this.messageCount = messageCount; return this; }
        public Builder lastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; return this; }
        public Builder partnerLabel(String partnerLabel) { this.partnerLabel = partnerLabel; return this; }

        public ConversationSummaryDto build() {
            return new ConversationSummaryDto(id, startedAt, endedAt, durationSeconds, messageCount, lastMessagePreview, partnerLabel);
        }
    }
}
