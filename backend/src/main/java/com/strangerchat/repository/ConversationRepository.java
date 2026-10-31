package com.strangerchat.repository;

import com.strangerchat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.userOne.id = :userId AND c.userOneDeleted = false)
           OR (c.userTwo.id = :userId AND c.userTwoDeleted = false)
        ORDER BY c.startedAt DESC
    """)
    Page<Conversation> findUserConversations(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
        SELECT c FROM Conversation c
        LEFT JOIN FETCH c.messages
        WHERE c.id = :conversationId
          AND ((c.userOne.id = :userId AND c.userOneDeleted = false)
            OR (c.userTwo.id = :userId AND c.userTwoDeleted = false))
    """)
    Optional<Conversation> findUserConversationWithMessages(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}
