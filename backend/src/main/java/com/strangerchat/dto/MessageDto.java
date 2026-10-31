package com.strangerchat.dto;

import java.time.Instant;
import java.util.UUID;

public class MessageDto {
    private UUID id;
    private String senderLabel;
    private boolean fromMe;
    private String content;
    private Instant sentAt;

    public MessageDto() {}

    public MessageDto(UUID id, String senderLabel, boolean fromMe, String content, Instant sentAt) {
        this.id = id;
        this.senderLabel = senderLabel;
        this.fromMe = fromMe;
        this.content = content;
        this.sentAt = sentAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSenderLabel() { return senderLabel; }
    public void setSenderLabel(String senderLabel) { this.senderLabel = senderLabel; }

    public boolean isFromMe() { return fromMe; }
    public void setFromMe(boolean fromMe) { this.fromMe = fromMe; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String senderLabel;
        private boolean fromMe;
        private String content;
        private Instant sentAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder senderLabel(String senderLabel) { this.senderLabel = senderLabel; return this; }
        public Builder fromMe(boolean fromMe) { this.fromMe = fromMe; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder sentAt(Instant sentAt) { this.sentAt = sentAt; return this; }

        public MessageDto build() {
            return new MessageDto(id, senderLabel, fromMe, content, sentAt);
        }
    }
}
