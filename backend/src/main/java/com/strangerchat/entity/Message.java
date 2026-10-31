package com.strangerchat.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id")
    private User senderUser;

    @Column(name = "sender_label", nullable = false, length = 50)
    private String senderLabel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    public Message() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public User getSenderUser() { return senderUser; }
    public void setSenderUser(User senderUser) { this.senderUser = senderUser; }

    public String getSenderLabel() { return senderLabel; }
    public void setSenderLabel(String senderLabel) { this.senderLabel = senderLabel; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private Conversation conversation;
        private User senderUser;
        private String senderLabel;
        private String content;
        private Instant sentAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder conversation(Conversation conversation) { this.conversation = conversation; return this; }
        public Builder senderUser(User senderUser) { this.senderUser = senderUser; return this; }
        public Builder senderLabel(String senderLabel) { this.senderLabel = senderLabel; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder sentAt(Instant sentAt) { this.sentAt = sentAt; return this; }

        public Message build() {
            Message m = new Message();
            m.setId(id);
            m.setConversation(conversation);
            m.setSenderUser(senderUser);
            m.setSenderLabel(senderLabel);
            m.setContent(content);
            m.setSentAt(sentAt);
            return m;
        }
    }
}
