package com.strangerchat.dto;

import java.util.UUID;

public class BlockRequest {
    private UUID conversationId;
    private String roomId;

    public BlockRequest() {}

    public BlockRequest(UUID conversationId, String roomId) {
        this.conversationId = conversationId;
        this.roomId = roomId;
    }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID conversationId;
        private String roomId;

        public Builder conversationId(UUID conversationId) { this.conversationId = conversationId; return this; }
        public Builder roomId(String roomId) { this.roomId = roomId; return this; }

        public BlockRequest build() {
            return new BlockRequest(conversationId, roomId);
        }
    }
}
