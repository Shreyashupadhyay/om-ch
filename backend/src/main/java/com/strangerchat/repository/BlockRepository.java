package com.strangerchat.repository;

import com.strangerchat.entity.Block;
import com.strangerchat.entity.BlockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlockRepository extends JpaRepository<Block, BlockId> {

    @Query("""
        SELECT COUNT(b) > 0 FROM Block b
        WHERE (b.id.blockerUserId = :userA AND b.id.blockedUserId = :userB)
           OR (b.id.blockerUserId = :userB AND b.id.blockedUserId = :userA)
    """)
    boolean isBlockedEitherWay(@Param("userA") UUID userA, @Param("userB") UUID userB);

    boolean existsByIdBlockerUserIdAndIdBlockedUserId(UUID blockerUserId, UUID blockedUserId);
}
