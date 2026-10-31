package com.strangerchat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BlockId implements Serializable {

    @Column(name = "blocker_user_id", nullable = false)
    private UUID blockerUserId;

    @Column(name = "blocked_user_id", nullable = false)
    private UUID blockedUserId;

    public BlockId() {}

    public BlockId(UUID blockerUserId, UUID blockedUserId) {
        this.blockerUserId = blockerUserId;
        this.blockedUserId = blockedUserId;
    }

    public UUID getBlockerUserId() { return blockerUserId; }
    public void setBlockerUserId(UUID blockerUserId) { this.blockerUserId = blockerUserId; }

    public UUID getBlockedUserId() { return blockedUserId; }
    public void setBlockedUserId(UUID blockedUserId) { this.blockedUserId = blockedUserId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockId blockId)) return false;
        return Objects.equals(blockerUserId, blockId.blockerUserId) &&
                Objects.equals(blockedUserId, blockId.blockedUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockerUserId, blockedUserId);
    }
}
