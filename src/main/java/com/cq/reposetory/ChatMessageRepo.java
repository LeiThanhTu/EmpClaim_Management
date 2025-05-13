package com.cq.reposetory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cq.entity.ChatMessage;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.isGroupMessage = true ORDER BY m.sentTime DESC")
    List<ChatMessage> findGroupMessagesByOrderBySentTimeDesc(Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = ?1 AND m.receiverId = ?2) OR (m.senderId = ?2 AND m.receiverId = ?1) ORDER BY m.sentTime ASC")
    List<ChatMessage> findConversation(Integer user1Id, Integer user2Id);
    
    List<ChatMessage> findBySenderIdOrReceiverIdOrderBySentTimeDesc(Integer senderId, Integer receiverId);
    
    @Query("SELECT DISTINCT m.senderId FROM ChatMessage m WHERE m.receiverId = ?1 AND m.senderId <> ?1")
    List<Integer> findDistinctSendersByReceiverId(Integer userId);
    
    @Query("SELECT DISTINCT m.receiverId FROM ChatMessage m WHERE m.senderId = ?1 AND m.receiverId <> ?1")
    List<Integer> findDistinctReceiversBySenderId(Integer userId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.isGroupMessage = true ORDER BY m.sentTime ASC")
    List<ChatMessage> findAllGroupMessages();
    
    @Query("SELECT m FROM ChatMessage m WHERE m.isGroupMessage = true ORDER BY m.sentTime DESC")
    Page<ChatMessage> findRecentGroupMessages(Pageable pageable);
}