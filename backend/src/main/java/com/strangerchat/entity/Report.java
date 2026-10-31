package com.strangerchat.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id")
    private User reporterUser;

    @Column(name = "reporter_guest_id", length = 100)
    private String reporterGuestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Report() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getReporterUser() { return reporterUser; }
    public void setReporterUser(User reporterUser) { this.reporterUser = reporterUser; }

    public String getReporterGuestId() { return reporterGuestId; }
    public void setReporterGuestId(String reporterGuestId) { this.reporterGuestId = reporterGuestId; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private User reporterUser;
        private String reporterGuestId;
        private Conversation conversation;
        private String reason;
        private String details;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder reporterUser(User reporterUser) { this.reporterUser = reporterUser; return this; }
        public Builder reporterGuestId(String reporterGuestId) { this.reporterGuestId = reporterGuestId; return this; }
        public Builder conversation(Conversation conversation) { this.conversation = conversation; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Report build() {
            Report r = new Report();
            r.setId(id);
            r.setReporterUser(reporterUser);
            r.setReporterGuestId(reporterGuestId);
            r.setConversation(conversation);
            r.setReason(reason);
            r.setDetails(details);
            r.setCreatedAt(createdAt);
            return r;
        }
    }
}
