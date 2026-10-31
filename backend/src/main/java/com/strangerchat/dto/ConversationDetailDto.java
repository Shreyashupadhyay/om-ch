package com.strangerchat.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ConversationDetailDto {
    private UUID id;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationSeconds;
    private String endedBy;
    private String partnerLabel;
    private List<MessageDto> messages;

    public ConversationDetailDto() {}

    public ConversationDetailDto(UUID id, Instant startedAt, Instant endedAt, Long durationSeconds,
                                 String endedBy, String partnerLabel, List<MessageDto> messages) {
        this.id = id;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.endedBy = endedBy;
        this.partnerLabel = partnerLabel;
        this.messages = messages;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getEndedBy() { return endedBy; }
    public void setEndedBy(String endedBy) { this.endedBy = endedBy; }

    public String getPartnerLabel() { return partnerLabel; }
    public void setPartnerLabel(String partnerLabel) { this.partnerLabel = partnerLabel; }

    public List<MessageDto> getMessages() { return messages; }
    public void setMessages(List<MessageDto> messages) { this.messages = messages; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private Instant startedAt;
        private Instant endedAt;
        private Long durationSeconds;
        private String endedBy;
        private String partnerLabel;
        private List<MessageDto> messages;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder endedAt(Instant endedAt) { this.endedAt = endedAt; return this; }
        public Builder durationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; return this; }
        public Builder endedBy(String endedBy) { this.endedBy = endedBy; return this; }
        public Builder partnerLabel(String partnerLabel) { this.partnerLabel = partnerLabel; return this; }
        public Builder messages(List<MessageDto> messages) { this.messages = messages; return this; }

        public ConversationDetailDto build() {
            return new ConversationDetailDto(id, startedAt, endedAt, durationSeconds, endedBy, partnerLabel, messages);
        }
    }
}
