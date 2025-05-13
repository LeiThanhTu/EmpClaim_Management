package com.cq.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private String content;
    
    @Column(name = "sent_time")
    private LocalDateTime sentTime;
    
    private MessageType type;
    
    @Column(name = "is_group_message")
    private boolean isGroupMessage;
    
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        GROUP
    }
    
    // Constructor cho tin nhắn mới
    public ChatMessage(Integer senderId, String senderName, Integer receiverId, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = null; // Không có tên người nhận cụ thể cho tin nhắn mới
        this.content = content;
        this.sentTime = LocalDateTime.now();
        this.type = MessageType.CHAT;
        this.isGroupMessage = false;
    }
    
    // Constructor cho tin nhắn nhóm
    public ChatMessage(Integer senderId, String senderName, String content, boolean isGroupMessage) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = null; // Không có người nhận cụ thể cho tin nhắn nhóm
        this.receiverName = null; // Không có tên người nhận cụ thể cho tin nhắn nhóm
        this.content = content;
        this.sentTime = LocalDateTime.now();
        this.type = MessageType.GROUP;
        this.isGroupMessage = true;
    }
}