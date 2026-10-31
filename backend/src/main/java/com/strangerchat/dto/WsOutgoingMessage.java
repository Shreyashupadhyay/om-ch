package com.strangerchat.dto;

import java.time.Instant;

public class WsOutgoingMessage {
    private String roomId;
    private String id;
    private String senderLabel;
    private boolean fromMe;
    private String content;
    private Instant timestamp;

    public WsOutgoingMessage() {}

    public WsOutgoingMessage(String roomId, String id, String senderLabel, boolean fromMe, String content, Instant timestamp) {
        this.roomId = roomId;
        this.id = id;
        this.senderLabel = senderLabel;
        this.fromMe = fromMe;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderLabel() { return senderLabel; }
    public void setSenderLabel(String senderLabel) { this.senderLabel = senderLabel; }

    public boolean isFromMe() { return fromMe; }
    public void setFromMe(boolean fromMe) { this.fromMe = fromMe; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String roomId;
        private String id;
        private String senderLabel;
        private boolean fromMe;
        private String content;
        private Instant timestamp;

        public Builder roomId(String roomId) { this.roomId = roomId; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder senderLabel(String senderLabel) { this.senderLabel = senderLabel; return this; }
        public Builder fromMe(boolean fromMe) { this.fromMe = fromMe; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public WsOutgoingMessage build() {
            return new WsOutgoingMessage(roomId, id, senderLabel, fromMe, content, timestamp);
        }
    }
}
