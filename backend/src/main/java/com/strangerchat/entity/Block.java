package com.strangerchat.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "blocks")
public class Block {

    @EmbeddedId
    private BlockId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blockerUserId")
    @JoinColumn(name = "blocker_user_id")
    private User blockerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blockedUserId")
    @JoinColumn(name = "blocked_user_id")
    private User blockedUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Block() {}

    public Block(BlockId id) {
        this.id = id;
    }

    public BlockId getId() { return id; }
    public void setId(BlockId id) { this.id = id; }

    public User getBlockerUser() { return blockerUser; }
    public void setBlockerUser(User blockerUser) { this.blockerUser = blockerUser; }

    public User getBlockedUser() { return blockedUser; }
    public void setBlockedUser(User blockedUser) { this.blockedUser = blockedUser; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
