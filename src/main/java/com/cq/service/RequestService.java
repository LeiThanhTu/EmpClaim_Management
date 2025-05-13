package com.cq.service;

import com.cq.entity.Employee;
import com.cq.entity.Request;
import com.cq.reposetory.EmployeeRepo;
import com.cq.reposetory.RequestRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RequestService {
    @Autowired
    private RequestRepo requestRepo;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private EmployeeRepo employeeRepo;  // Add this to get employee email


    public List<Request> getAllRequests() {
        List<Request> requests = requestRepo.findAll();
        requests.forEach(request -> {
            if (request.getEmployeeId() != null) {
                Employee employee = employeeRepo.findById(request.getEmployeeId()).orElse(null);
                if (employee != null) {
                    request.setPosition(employee.getDepartment());
                    request.setDepartment(employee.getDepartment());
                    request.setEmail(employee.getEmail());
                    request.setEmployeeName(employee.getEmployeeName());
                } else {
                    request.setPosition("Unknown");
                    request.setDepartment("Unknown");
                }
            } else {
                request.setPosition("Unknown");
                request.setDepartment("Unknown");
            }
        });
        return requests;
    }
    public List<Request> getEmployeeRequests(int employeeId) {
        // Try to find by employee relationship first, then fall back to employeeId field
        List<Request> requests = requestRepo.findByEmployee_IdOrderByCreatedDateDesc(employeeId);
        
        // If no results found, try the old method
        if (requests == null || requests.isEmpty()) {
            requests = requestRepo.findByEmployeeIdOrderByCreatedDateDesc(employeeId);
        }
        
        return requests;
    }

    public Request createRequest(Request request, MultipartFile file) throws IOException {
        // Get employee details and set position
        Employee employee = employeeRepo.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        request.setPosition(employee.getDesignation());
        
        // Handle file attachment
        if (file != null && !file.isEmpty()) {
            request.setAttachment(file.getBytes());
            request.setAttachmentName(file.getOriginalFilename());
        }
        
        // Set initial status and save
        request.setStatus("PENDING");
        request.setCreatedDate(LocalDateTime.now());
        
        return requestRepo.save(request);
    }

    public Request updateStatus(Long requestId, String status) {
        Request request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        request.setStatus(status);
        Request updatedRequest = requestRepo.save(request);
        
        // Get employee email from EmployeeRepo
        if (request.getEmployeeId() != null) {
            try {
                Employee employee = employeeRepo.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
                request.setEmail(employee.getEmail()); // Set email from employee record
            } catch (Exception e) {
                System.err.println("Error getting employee email: " + e.getMessage());
            }
        }
        
        // Send email notification
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            sendStatusUpdateEmail(updatedRequest);
        } else {
            System.err.println("No email address found for request #" + requestId);
        }
        
        return updatedRequest;
    }

    private void sendStatusUpdateEmail(Request request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("lethithanhtu399@gmail.com"); 
            message.setTo(request.getEmail());
            message.setSubject("Thông báo cập nhật trạng thái yêu cầu - " + request.getSubject());
            
            String statusText = "";
            switch(request.getStatus().toUpperCase()) {
                case "APPROVED":
                    statusText = "đã được PHÊ DUYỆT";
                    break;
                case "DENIED":
                    statusText = "đã bị TỪ CHỐI";
                    break;
                case "CANCELED":
                    statusText = "đã bị HỦY BỎ";
                    break;
                default:
                    statusText = "đã được cập nhật sang trạng thái: " + request.getStatus();
            }
            
            String emailBody = String.format(
                "Kính gửi %s,\n\n" +
                "Yêu cầu #%d của bạn với tiêu đề '%s' %s.\n\n" +
                "Thời gian tạo: %s\n" +
                "Thời gian cập nhật: %s\n\n" +
                "Đây là email tự động, vui lòng không trả lời.\n" +
                "Để biết thêm chi tiết, vui lòng đăng nhập vào hệ thống.\n\n" +
                "Trân trọng,\nBộ phận Hỗ trợ",
                request.getEmployeeName(),
                request.getId(),
                request.getSubject(),
                statusText,
                request.getCreatedDate(),
                LocalDateTime.now()
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            System.out.println("Attempting to send email to: " + request.getEmail());
            mailSender.send(message);
            System.out.println("Email sent successfully to " + request.getEmail());
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}