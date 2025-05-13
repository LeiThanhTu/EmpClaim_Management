package com.cq.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.cq.entity.ChatMessage.MessageType;
import lombok.Data;

@Data
public class ChatMessageDTO {
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private String content;
    private String sentTime;
    private MessageType type;
    private boolean isGroupMessage;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Default constructor
    public ChatMessageDTO() {}

    // Constructor for group messages
    public ChatMessageDTO(Integer senderId, String senderName, String content, 
                         LocalDateTime sentTime, MessageType type, boolean isGroupMessage) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.setSentTime(sentTime);
        this.type = type;
        this.isGroupMessage = isGroupMessage;
    }

    // Constructor for direct messages
    public ChatMessageDTO(Integer senderId, String senderName, Integer receiverId, 
                         String receiverName, String content, LocalDateTime sentTime, 
                         MessageType type) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.setSentTime(sentTime);
        this.type = type;
        this.isGroupMessage = false;
    }

    public void setSentTime(LocalDateTime time) {
        if (time != null) {
            this.sentTime = time.format(TIME_FORMATTER);
        }
    }
}