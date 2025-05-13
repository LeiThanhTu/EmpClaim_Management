package com.cq.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cq.dto.ChatMessageDTO;
import com.cq.dto.UserStatusDTO;
import com.cq.entity.ChatMessage;
import com.cq.entity.ChatMessage.MessageType;
import com.cq.entity.Employee;
import com.cq.reposetory.ChatMessageRepo;
import com.cq.reposetory.EmployeeRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatMessageRepo chatMessageRepo;
    
    @Autowired
    private EmployeeRepo employeeRepo;
    
    // Map để lưu trữ người dùng đang trực tuyến
    private final Map<String, UserStatusDTO> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * Xử lý việc gửi tin nhắn thông qua WebSocket
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO) {
        // Tạo đối tượng tin nhắn từ DTO
        ChatMessage chatMessage = new ChatMessage(
            chatMessageDTO.getSenderId(),
            chatMessageDTO.getSenderName(), 
            chatMessageDTO.getReceiverId(), 
            chatMessageDTO.getContent()
        );
        
        // Lưu tin nhắn vào database
        chatMessageRepo.save(chatMessage);
        
        // Định dạng lại thời gian cho DTO
        chatMessageDTO.setSentTime(chatMessage.getSentTime());
        
        // Gửi tin nhắn đến người nhận cụ thể
        messagingTemplate.convertAndSendToUser(
            chatMessageDTO.getReceiverId().toString(),
            "/queue/messages",
            chatMessageDTO
        );
        
        // Gửi tin nhắn về lại người gửi để đồng bộ
        messagingTemplate.convertAndSendToUser(
            chatMessageDTO.getSenderId().toString(),
            "/queue/messages",
            chatMessageDTO
        );
    }
    
    /**
     * Xử lý việc gửi tin nhắn nhóm thông qua WebSocket
     */
    @MessageMapping("/chat.sendGroupMessage")
    public void sendGroupMessage(@Payload ChatMessageDTO chatMessageDTO) {
        // Đặt thời gian gửi
        LocalDateTime now = LocalDateTime.now();
        
        // Log thông tin tin nhắn để debug
        System.out.println("Received group message: " + chatMessageDTO.getContent() + 
                          " from user: " + chatMessageDTO.getSenderName() + 
                          " (ID: " + chatMessageDTO.getSenderId() + ")");
        
        try {
            // Tạo đối tượng tin nhắn từ DTO
            ChatMessage message = new ChatMessage();
            message.setSenderId(chatMessageDTO.getSenderId());
            message.setSenderName(chatMessageDTO.getSenderName());
            message.setContent(chatMessageDTO.getContent());
            message.setType(MessageType.GROUP);
            message.setSentTime(now);
            message.setGroupMessage(true);
            
            // Lưu tin nhắn vào database
            chatMessageRepo.save(message);
            
            // Cập nhật thời gian trong DTO để trả về cho client
            chatMessageDTO.setSentTime(now);
            
            // Cập nhật trạng thái người dùng
            updateUserStatus(chatMessageDTO.getSenderId(), chatMessageDTO.getSenderName());
            
            // Gửi tin nhắn đến tất cả người dùng NGOẠI TRỪ người gửi
            // Không gửi lại tin nhắn cho người gửi để tránh hiển thị trùng lặp
            String senderId = chatMessageDTO.getSenderId().toString();
            
            // Lấy danh sách người dùng đang online
            List<String> recipients = new ArrayList<>();
            for (String userId : onlineUsers.keySet()) {
                recipients.add(userId);
            }
            
            // Gửi tin nhắn cho từng người dùng, ngoại trừ người gửi
            for (String userId : recipients) {
                if (!userId.equals(senderId)) {
                    messagingTemplate.convertAndSendToUser(
                        userId, 
                        "/queue/messages", 
                        chatMessageDTO
                    );
                }
            }
            
            // Gửi tin nhắn đến kênh chung cho những người không có trong danh sách onlineUsers
            messagingTemplate.convertAndSend("/topic/group", chatMessageDTO);
            
        } catch (Exception e) {
            System.err.println("Error processing group message: " + e.getMessage());
            e.printStackTrace();
            
            // Vẫn trả về tin nhắn để client biết rằng tin nhắn đã được nhận
            chatMessageDTO.setSentTime(now);
            messagingTemplate.convertAndSend("/topic/group", chatMessageDTO);
        }
    }

    @GetMapping("/api/chat/group-history")
    @ResponseBody
    public List<ChatMessageDTO> getGroupChatHistory(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return List.of();
        }
        
        // Log thông tin người dùng để debug
        System.out.println("Getting group chat history for user ID: " + userId);
        
        // Lấy 50 tin nhắn gần nhất, sắp xếp theo thời gian giảm dần
        List<ChatMessage> messages = chatMessageRepo.findGroupMessagesByOrderBySentTimeDesc(PageRequest.of(0, 50));
        
        // Chuyển đổi từ entity sang DTO
        List<ChatMessageDTO> dtos = messages.stream()
            .map(msg -> new ChatMessageDTO(
                msg.getSenderId(),
                msg.getSenderName(),
                msg.getContent(),
                msg.getSentTime(),
                MessageType.GROUP,
                true
            ))
            .collect(Collectors.toList());
        
        System.out.println("Found " + dtos.size() + " group messages");
        return dtos;
    }
    
    /**
     * Xử lý cập nhật trạng thái người dùng trực tuyến
     */
    @MessageMapping("/user.online")
    public void handleUserOnlineStatus(@Payload UserStatusDTO statusDTO) {
        if (statusDTO.getUserId() == null || statusDTO.getUsername() == null) {
            return;
        }
        
        if (statusDTO.isOnline()) {
            // Thêm người dùng vào danh sách trực tuyến
            onlineUsers.put(statusDTO.getUserId().toString(), statusDTO);
        } else {
            // Xóa người dùng khỏi danh sách trực tuyến
            onlineUsers.remove(statusDTO.getUserId().toString());
        }
        
        // Gửi danh sách người dùng trực tuyến đến tất cả người dùng
        broadcastOnlineUsers();
    }
    
    /**
     * Cập nhật trạng thái người dùng
     */
    private void updateUserStatus(Integer userId, String username) {
        if (userId == null || username == null) {
            return;
        }
        
        UserStatusDTO statusDTO = new UserStatusDTO();
        statusDTO.setUserId(userId);
        statusDTO.setUsername(username);
        statusDTO.setOnline(true);
        statusDTO.setLastActive(LocalDateTime.now());
        
        // Cập nhật hoặc thêm người dùng vào danh sách trực tuyến
        onlineUsers.put(userId.toString(), statusDTO);
        
        // Gửi danh sách người dùng trực tuyến đến tất cả người dùng
        broadcastOnlineUsers();
    }
    
    /**
     * Gửi danh sách người dùng trực tuyến đến tất cả người dùng
     */
    private void broadcastOnlineUsers() {
        List<UserStatusDTO> userList = new ArrayList<>(onlineUsers.values());
        messagingTemplate.convertAndSend("/topic/online-users", userList);
    }
    
    /**
     * Lấy danh sách người dùng trực tuyến
     */
    @GetMapping("/api/chat/online-users")
    @ResponseBody
    public List<UserStatusDTO> getOnlineUsers() {
        return new ArrayList<>(onlineUsers.values());
    }
    
    /**
     * Lấy danh sách tất cả người dùng
     */
    @GetMapping("/api/users")
    @ResponseBody
    public List<Employee> getAllUsers() {
        return employeeRepo.findAll();
    }
    
    /**
     * Tải lịch sử cuộc trò chuyện giữa hai người dùng
     */
    @GetMapping("/api/chat/history/{receiverId}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable Integer receiverId, HttpSession session) {
        Integer senderId = (Integer) session.getAttribute("userId");
        if (senderId == null) {
            return List.of(); // Trả về danh sách rỗng nếu chưa đăng nhập
        }
        
        return chatMessageRepo.findConversation(senderId, receiverId);
    }
    
    /**
     * Hiển thị trang chat với một người dùng cụ thể
     */
    @GetMapping("/chat/{receiverId}")
    public String showChatPage(@PathVariable Integer receiverId, Model model, HttpSession session) {
        Integer senderId = (Integer) session.getAttribute("userId");
        if (senderId == null) {
            return "redirect:/login";
        }
        
        Employee currentUser = employeeRepo.findById(senderId).orElse(null);
        Employee receiver = employeeRepo.findById(receiverId).orElse(null);
        
        if (currentUser == null || receiver == null) {
            return "redirect:/all-employee";
        }
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("receiver", receiver);
        model.addAttribute("chatHistory", chatMessageRepo.findConversation(senderId, receiverId));
        
        return "chat";
    }
    
    /**
     * Hiển thị trang chat nhóm
     */
    @GetMapping("/group-chat")
    public String showGroupChatPage(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Employee currentUser = employeeRepo.findById(userId).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("groupChatHistory", chatMessageRepo.findRecentGroupMessages(PageRequest.of(0, 50)).getContent());
        
        return "group-chat";
    }
    
    /**
     * Lấy danh sách những người đã nhắn tin với người dùng hiện tại
     */
    @GetMapping("/api/chat/contacts")
    @ResponseBody
    public List<Employee> getChatContacts(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return List.of();
        }
        
        // Lấy ID của những người đã gửi tin nhắn cho người dùng hiện tại
        List<Integer> senderIds = chatMessageRepo.findDistinctSendersByReceiverId(userId);
        
        // Lấy ID của những người mà người dùng hiện tại đã gửi tin nhắn
        List<Integer> receiverIds = chatMessageRepo.findDistinctReceiversBySenderId(userId);
        
        // Gộp hai danh sách và loại bỏ trùng lặp
        senderIds.addAll(receiverIds);
        List<Integer> uniqueContactIds = senderIds.stream().distinct().toList();
        
        // Lấy thông tin chi tiết của các liên hệ
        return employeeRepo.findAllById(uniqueContactIds);
    }
    
    /**
     * Lấy thông tin người dùng hiện tại
     */
    @GetMapping("/api/current-user")
    @ResponseBody
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> userInfo = new HashMap<>();
        
        try {
            // Check for employee user
            Employee emp = (Employee) session.getAttribute("employee");
            if (emp != null) {
                // Log thông tin người dùng để debug
                System.out.println("Current employee user info - ID: " + emp.getId() + 
                                  ", Name: " + emp.getEmployeeName() + ", Role: " + emp.getDesignation());
                
                userInfo.put("id", emp.getId());
                userInfo.put("name", emp.getEmployeeName());
                userInfo.put("email", emp.getEmail());
                userInfo.put("designation", emp.getDesignation());
                return userInfo;
            }
            
            // Check for admin user (stored differently in session)
            Integer userId = (Integer) session.getAttribute("userId");
            String name = (String) session.getAttribute("name");
            String email = (String) session.getAttribute("userEmail");
            String designation = (String) session.getAttribute("desg");
            
            if (userId != null && name != null) {
                // Log thông tin người dùng để debug
                System.out.println("Current admin user info - ID: " + userId + 
                                  ", Name: " + name + ", Role: " + designation);
                
                userInfo.put("id", userId);
                userInfo.put("name", name);
                userInfo.put("email", email);
                userInfo.put("designation", designation);
                return userInfo;
            }
            
            // No valid user found in session
            System.out.println("No user found in session");
            userInfo.put("error", "No user found in session");
            return userInfo;
            
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            
            userInfo.put("error", "Error retrieving user information: " + e.getMessage());
            return userInfo;
        }
    }
}