package com.cq.controller;

import com.cq.entity.Employee;
import com.cq.entity.Request;
import com.cq.reposetory.EmployeeRepo;
import com.cq.reposetory.RequestRepo;
import com.cq.service.RequestService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/emp-request")
public class EmployeeRequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepo requestRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/view/{id}")
    public String viewRequestDetails(@PathVariable Long id, Model model, HttpSession session) {
        try {
            Request request = requestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));
            
            Integer userId = (Integer) session.getAttribute("userId");
            Employee employee = employeeRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean isAdmin = "ADMIN".equals(employee.getRole());
            boolean isOwner = false;
            
            // Kiểm tra an toàn cho employeeId
            if (request.getEmployee() != null && request.getEmployee().getId() != null) {
                isOwner = request.getEmployee().getId().equals(userId);
            } else if (request.getEmployeeId() != null) {
                isOwner = request.getEmployeeId().equals(userId);
            }
            
            // Cập nhật position từ employee nếu có
            if (request.getEmployeeId() != null) {
                Employee requestEmployee = employeeRepo.findById(request.getEmployeeId()).orElse(null);
                if (requestEmployee != null) {
                    request.setPosition(requestEmployee.getDepartment());
                    request.setDepartment(requestEmployee.getDepartment());
                    request.setEmail(requestEmployee.getEmail());
                    request.setEmployeeName(requestEmployee.getEmployeeName());
                }
            }
            
            if (isAdmin || isOwner) {
                model.addAttribute("request", request);
                model.addAttribute("isAdmin", isAdmin);
                return "request-details";
            } else {
                model.addAttribute("error", "Bạn không có quyền xem yêu cầu này");
                return "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi xem chi tiết yêu cầu: " + e.getMessage());
            return "error";
        }
    }
    @GetMapping("/user/view/{id}")
    public String viewUserRequest(@PathVariable Long id, Model model, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }
    
            Request request = requestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
                    
            // Check if the request belongs to the logged-in user
            if (request.getEmployeeId() != null && !userId.equals(request.getEmployeeId())) {
                model.addAttribute("error", "Bạn không có quyền xem yêu cầu này");
                return "redirect:/emp-request/list";
            }
            
            // Cập nhật position từ employee
            Employee requestEmployee = employeeRepo.findById(request.getEmployeeId()).orElse(null);
            if (requestEmployee != null) {
                request.setPosition(requestEmployee.getDepartment());
                request.setDepartment(requestEmployee.getDepartment());
                request.setEmail(requestEmployee.getEmail());
                request.setEmployeeName(requestEmployee.getEmployeeName());
            }
    
            model.addAttribute("request", request);
            return "user-request-details";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi xem yêu cầu: " + e.getMessage());
            return "redirect:/emp-request/list";
        }
    }
    @GetMapping("/edit/{id}")
    public String showRequestEditForm(@PathVariable Long id, Model model, HttpSession session) {
        try {
            Request request = requestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));
            
            // Kiểm tra quyền truy cập (chỉ nhân viên tạo yêu cầu và trạng thái PENDING được sửa)
            Integer userId = (Integer) session.getAttribute("userId");
            boolean isOwner = false;
            
            // Kiểm tra an toàn cho employeeId
            if (request.getEmployeeId() != null) {
                isOwner = request.getEmployeeId().equals(userId);
            }
            
            if (isOwner && "PENDING".equals(request.getStatus())) {
                model.addAttribute("request", request);
                return "edit-request";
            } else {
                model.addAttribute("error", "Bạn không có quyền chỉnh sửa yêu cầu này");
                return "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải form chỉnh sửa: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/update/{id}")
    public String updateRequest(@PathVariable Long id, 
                              @RequestParam String subject,
                              @RequestParam String description,
                              @RequestParam(required = false) MultipartFile file,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }
    
            Request request = requestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
                    
            // Verify ownership and PENDING status
            if (request.getEmployeeId() == null || !userId.equals(request.getEmployeeId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa yêu cầu này");
                return "redirect:/emp-request/list";
            }
            
            if (!"PENDING".equals(request.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể chỉnh sửa yêu cầu đang ở trạng thái chờ xử lý");
                return "redirect:/emp-request/list";
            }
    
            // Update fields
            request.setSubject(subject);
            request.setDescription(description);
            
            if (file != null && !file.isEmpty()) {
                request.setAttachment(file.getBytes());
                request.setAttachmentName(file.getOriginalFilename());
            }
            
            // Log before save
            System.out.println("Updating request: " + request.getId() + 
                              ", Subject: " + request.getSubject() + 
                              ", Description: " + request.getDescription());
            
            requestRepo.save(request);
            
            // Log after save
            System.out.println("Request updated successfully");
            
            redirectAttributes.addFlashAttribute("message", "Cập nhật yêu cầu thành công!");
            return "redirect:/emp-request/list";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật yêu cầu: " + e.getMessage());
            return "redirect:/emp-request/edit/" + id;
        }
    }
    
    @GetMapping("/list")
    public String showUserRequests(Model model, HttpSession session) {
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                model.addAttribute("error", "Vui lòng đăng nhập để xem danh sách yêu cầu");
                return "redirect:/login";
            }
            
            try {
                int userId = Integer.parseInt(userIdObj.toString());
                // Lấy danh sách yêu cầu và sắp xếp theo thời gian tạo (mới nhất lên đầu)
                List<Request> requests = requestService.getEmployeeRequests(userId);
                
                // In ra thông tin debug
                System.out.println("Found " + requests.size() + " requests for userId: " + userId);
                for (Request req : requests) {
                    System.out.println("Request ID: " + req.getId() + ", Subject: " + req.getSubject() + ", Status: " + req.getStatus());
                }
                
                model.addAttribute("requests", requests);
                return "status-request";
            } catch (NumberFormatException e) {
                model.addAttribute("error", "Lỗi: Thông tin người dùng không hợp lệ");
                return "redirect:/login";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải danh sách yêu cầu: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/admin/list")
    public String showAdminRequests(Model model) {
        try {
            // Lấy tất cả requests và đảm bảo position được cập nhật
            List<Request> requests = requestService.getAllRequests();
            
           
            
            model.addAttribute("statusList", requests);
            return "status";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải danh sách yêu cầu: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/compose")
    public String showComposeForm(Model model, HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            model.addAttribute("error", "Lỗi: Authentication failed. Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }
        
        try {
            int userId = Integer.parseInt(userIdObj.toString());
            Employee employee = employeeRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
                    
            // Add both email and name to session
            session.setAttribute("userEmail", employee.getEmail());
            session.setAttribute("userName", employee.getEmployeeName());
            
        } catch (Exception e) {
            System.out.println("Error getting employee info: " + e.getMessage());
            return "redirect:/login";
        }
        
        return "user-compose";
    }
    
    @PostMapping("/create")
    public String createRequest(@RequestParam String subject,
                              @RequestParam String text,
                              @RequestParam String email,
                              @RequestParam String employeeName,
                              @RequestParam(required = false) MultipartFile file,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            // Get userId from session
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
                return "redirect:/login";
            }
    
            // Get employee information
            Employee employee = employeeRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
    
            // Create new request
            Request request = new Request();
            request.setEmployeeId(userId);
            request.setEmployeeName(employee.getEmployeeName());
            request.setEmail(employee.getEmail());
            request.setPosition(employee.getDepartment());
            request.setDepartment(employee.getDepartment());
            request.setSubject(subject);
            request.setDescription(text);
            request.setStatus("PENDING");
            request.setCreatedDate(LocalDateTime.now());
            request.setEmployee(employee);
    
            // Handle file attachment
            if (file != null && !file.isEmpty()) {
                request.setAttachment(file.getBytes());
                request.setAttachmentName(file.getOriginalFilename());
            }
    
            // Save request
            requestRepo.save(request);
            
            redirectAttributes.addFlashAttribute("message", "Yêu cầu đã được gửi thành công!");
            return "redirect:/emp-request/list";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo yêu cầu: " + e.getMessage());
            return "redirect:/emp-request/compose";
        }
    }

    @GetMapping("/attachment/{id}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {
        try {
            Request request = requestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));
            
            if (request.getAttachment() == null || request.getAttachmentName() == null) {
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", request.getAttachmentName());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(request.getAttachment());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/update-status")
    public String updateRequestStatus(@RequestParam Long id, 
                                    @RequestParam String status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Updating request status for ID: " + id + " to status: " + status);
            
            // Get request and verify it exists
            Request request = requestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
            
            // Update status
            Request updatedRequest = requestService.updateStatus(id, status);
            System.out.println("Status updated successfully. Email sent to: " + updatedRequest.getEmail());
            
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái thành công thành: " + status);
            
            // Redirect to appropriate view based on source template
            if (session.getAttribute("userId") != null) {
                return "redirect:/emp-request/view/" + id;
            } else {
                return "redirect:/emp-request/admin/list";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error updating status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return "redirect:/emp-request/view/" + id;
        }
    }

    @GetMapping("/admin/manage")
    public String showAdminRequestsPage(Model model) {
        try {
            List<Request> requests = requestService.getAllRequests();
            model.addAttribute("requests", requests);
            return "admin/manage-requests";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải danh sách yêu cầu: " + e.getMessage());
            return "error";
        }
    }

    private void sendAdminNotification(Request request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@example.com"); // Replace with actual admin email
        message.setSubject("Yêu cầu mới từ " + request.getEmployeeName());
        message.setText("Có yêu cầu mới cần xử lý:\n" +
                       "Nhân viên: " + request.getEmployeeName() + "\n" +
                       "Tiêu đề: " + request.getSubject());
        mailSender.send(message);
    }
}
