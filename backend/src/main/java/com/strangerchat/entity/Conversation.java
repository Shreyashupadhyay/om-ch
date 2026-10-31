package com.strangerchat.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id")
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id")
    private User userTwo;

    @Column(name = "user_one_is_guest", nullable = false)
    private boolean userOneIsGuest = false;

    @Column(name = "user_two_is_guest", nullable = false)
    private boolean userTwoIsGuest = false;

    @Column(name = "user_one_deleted", nullable = false)
    private boolean userOneDeleted = false;

    @Column(name = "user_two_deleted", nullable = false)
    private boolean userTwoDeleted = false;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "ended_by", length = 50)
    private String endedBy;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt ASC")
    private List<Message> messages = new ArrayList<>();

    public Conversation() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUserOne() { return userOne; }
    public void setUserOne(User userOne) { this.userOne = userOne; }

    public User getUserTwo() { return userTwo; }
    public void setUserTwo(User userTwo) { this.userTwo = userTwo; }

    public boolean isUserOneIsGuest() { return userOneIsGuest; }
    public void setUserOneIsGuest(boolean userOneIsGuest) { this.userOneIsGuest = userOneIsGuest; }

    public boolean isUserTwoIsGuest() { return userTwoIsGuest; }
    public void setUserTwoIsGuest(boolean userTwoIsGuest) { this.userTwoIsGuest = userTwoIsGuest; }

    public boolean isUserOneDeleted() { return userOneDeleted; }
    public void setUserOneDeleted(boolean userOneDeleted) { this.userOneDeleted = userOneDeleted; }

    public boolean isUserTwoDeleted() { return userTwoDeleted; }
    public void setUserTwoDeleted(boolean userTwoDeleted) { this.userTwoDeleted = userTwoDeleted; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public String getEndedBy() { return endedBy; }
    public void setEndedBy(String endedBy) { this.endedBy = endedBy; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private User userOne;
        private User userTwo;
        private boolean userOneIsGuest = false;
        private boolean userTwoIsGuest = false;
        private boolean userOneDeleted = false;
        private boolean userTwoDeleted = false;
        private Instant startedAt;
        private Instant endedAt;
        private String endedBy;
        private List<Message> messages = new ArrayList<>();

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userOne(User userOne) { this.userOne = userOne; return this; }
        public Builder userTwo(User userTwo) { this.userTwo = userTwo; return this; }
        public Builder userOneIsGuest(boolean isGuest) { this.userOneIsGuest = isGuest; return this; }
        public Builder userTwoIsGuest(boolean isGuest) { this.userTwoIsGuest = isGuest; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder endedAt(Instant endedAt) { this.endedAt = endedAt; return this; }
        public Builder endedBy(String endedBy) { this.endedBy = endedBy; return this; }

        public Conversation build() {
            Conversation c = new Conversation();
            c.setId(id);
            c.setUserOne(userOne);
            c.setUserTwo(userTwo);
            c.setUserOneIsGuest(userOneIsGuest);
            c.setUserTwoIsGuest(userTwoIsGuest);
            c.setUserOneDeleted(userOneDeleted);
            c.setUserTwoDeleted(userTwoDeleted);
            c.setStartedAt(startedAt);
            c.setEndedAt(endedAt);
            c.setEndedBy(endedBy);
            c.setMessages(messages);
            return c;
        }
    }
}
