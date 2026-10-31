package com.strangerchat.dto;

import java.time.Instant;
import java.util.Map;

public class WsEventMessage {
    public enum EventType {
        WAITING_IN_QUEUE,
        MATCHED,
        PARTNER_TYPING,
        PARTNER_STOPPED_TYPING,
        PARTNER_LEFT,
        ROOM_ENDED,
        SYSTEM_NOTICE,
        ERROR
    }

    private EventType event;
    private String roomId;
    private String message;
    private Map<String, Object> payload;
    private Instant timestamp = Instant.now();

    public WsEventMessage() {}

    public WsEventMessage(EventType event, String roomId, String message, Map<String, Object> payload, Instant timestamp) {
        this.event = event;
        this.roomId = roomId;
        this.message = message;
        this.payload = payload;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public EventType getEvent() { return event; }
    public void setEvent(EventType event) { this.event = event; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private EventType event;
        private String roomId;
        private String message;
        private Map<String, Object> payload;
        private Instant timestamp = Instant.now();

        public Builder event(EventType event) { this.event = event; return this; }
        public Builder roomId(String roomId) { this.roomId = roomId; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder payload(Map<String, Object> payload) { this.payload = payload; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public WsEventMessage build() {
            return new WsEventMessage(event, roomId, message, payload, timestamp);
        }
    }
}
