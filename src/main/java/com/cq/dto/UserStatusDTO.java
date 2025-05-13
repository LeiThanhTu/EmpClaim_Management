package com.cq.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để theo dõi trạng thái trực tuyến của người dùng trong chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDTO {
    private Integer userId;
    private String username;
    private String userRole;
    private boolean online;
    private LocalDateTime lastActive;
    
    // Phương thức tiện ích để kiểm tra xem người dùng có đang hoạt động không
    public boolean isActive() {
        if (!online) {
            return false;
        }
        
        if (lastActive == null) {
            return false;
        }
        
        // Người dùng được coi là đang hoạt động nếu họ đã hoạt động trong 5 phút qua
        return lastActive.plusMinutes(5).isAfter(LocalDateTime.now());
    }
}
