package com.strangerchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ReportRequest {

    private UUID conversationId;
    private String partnerSessionOrGuestId;

    @NotBlank(message = "Reason is required")
    @Size(max = 100, message = "Reason cannot exceed 100 characters")
    private String reason;

    @Size(max = 1000, message = "Details cannot exceed 1000 characters")
    private String details;

    public ReportRequest() {}

    public ReportRequest(UUID conversationId, String partnerSessionOrGuestId, String reason, String details) {
        this.conversationId = conversationId;
        this.partnerSessionOrGuestId = partnerSessionOrGuestId;
        this.reason = reason;
        this.details = details;
    }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getPartnerSessionOrGuestId() { return partnerSessionOrGuestId; }
    public void setPartnerSessionOrGuestId(String partnerSessionOrGuestId) { this.partnerSessionOrGuestId = partnerSessionOrGuestId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID conversationId;
        private String partnerSessionOrGuestId;
        private String reason;
        private String details;

        public Builder conversationId(UUID conversationId) { this.conversationId = conversationId; return this; }
        public Builder partnerSessionOrGuestId(String partner) { this.partnerSessionOrGuestId = partner; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder details(String details) { this.details = details; return this; }

        public ReportRequest build() {
            return new ReportRequest(conversationId, partnerSessionOrGuestId, reason, details);
        }
    }
}
