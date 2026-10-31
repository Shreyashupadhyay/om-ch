package com.strangerchat.dto;

import jakarta.validation.constraints.Size;

public class WsIncomingMessage {
    private String roomId;

    @Size(max = 1000, message = "Message content must not exceed 1000 characters")
    private String content;

    private Boolean isTyping;

    public WsIncomingMessage() {}

    public WsIncomingMessage(String roomId, String content, Boolean isTyping) {
        this.roomId = roomId;
        this.content = content;
        this.isTyping = isTyping;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsTyping() { return isTyping; }
    public void setIsTyping(Boolean isTyping) { this.isTyping = isTyping; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String roomId;
        private String content;
        private Boolean isTyping;

        public Builder roomId(String roomId) { this.roomId = roomId; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder isTyping(Boolean isTyping) { this.isTyping = isTyping; return this; }

        public WsIncomingMessage build() {
            return new WsIncomingMessage(roomId, content, isTyping);
        }
    }
}
