package com.strangerchat.dto;

public class OnlineStatsDto {
    private int onlineUsers;
    private int activeRooms;
    private int queueLength;

    public OnlineStatsDto() {}

    public OnlineStatsDto(int onlineUsers, int activeRooms, int queueLength) {
        this.onlineUsers = onlineUsers;
        this.activeRooms = activeRooms;
        this.queueLength = queueLength;
    }

    public int getOnlineUsers() { return onlineUsers; }
    public void setOnlineUsers(int onlineUsers) { this.onlineUsers = onlineUsers; }

    public int getActiveRooms() { return activeRooms; }
    public void setActiveRooms(int activeRooms) { this.activeRooms = activeRooms; }

    public int getQueueLength() { return queueLength; }
    public void setQueueLength(int queueLength) { this.queueLength = queueLength; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int onlineUsers;
        private int activeRooms;
        private int queueLength;

        public Builder onlineUsers(int onlineUsers) { this.onlineUsers = onlineUsers; return this; }
        public Builder activeRooms(int activeRooms) { this.activeRooms = activeRooms; return this; }
        public Builder queueLength(int queueLength) { this.queueLength = queueLength; return this; }

        public OnlineStatsDto build() {
            return new OnlineStatsDto(onlineUsers, activeRooms, queueLength);
        }
    }
}
