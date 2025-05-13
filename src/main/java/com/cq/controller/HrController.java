package com.cq.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.cq.entity.*;
import com.cq.reposetory.*;
import jakarta.persistence.Column;
import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.cq.service.HrService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// Add these imports at the top
import com.cq.entity.Project;
// Thêm imports
import com.cq.service.RequestService;
import com.cq.reposetory.RequestRepo;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;



@Controller
public class HrController {
	
	@Autowired
	private HrService service;
	
	@Autowired
	private CreatePostRepo createPostRepo;
	
	@Autowired
	private EmployeeRepo employeeRepo;

	// @Autowired
	// private ComposeRepo composeRepo;

	@Autowired
	private ProjectRepo projectRepo;

	@Autowired
	private ProjectEmployeeRepo projectEmployeeRepo;

	@Autowired
	private RequestService requestService;

	@Autowired
	private RequestRepo requestRepo;

	@Autowired
	private JavaMailSender mailSender;


	@Column(name = "created_date")
	private LocalDateTime createdDate;



	@GetMapping("/projects/assign-staff/{id}")
	public String showAssignStaffForm(@PathVariable Long id, Model model) {
		try {
			Project project = projectRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Project not found"));
			List<Employee> employees = employeeRepo.findAll();

			model.addAttribute("project", project);
			model.addAttribute("employees", employees);
			return "project/assign-staff";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/projects?error=Failed to load assignment form";
		}
	}

	@PostMapping("/projects/assign-staff/{id}")
	public String assignStaff(@PathVariable Long id,
							  @RequestParam Integer employeeId,
							  @RequestParam String assignedDate,
							  @RequestParam String endDate,
							  @RequestParam Double budgetAllocation,
							  @RequestParam String role,
							  RedirectAttributes redirectAttributes) {
		try {
			Project project = projectRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Project not found"));
			Employee employee = employeeRepo.findById(employeeId)
					.orElseThrow(() -> new RuntimeException("Employee not found"));

			ProjectEmployee assignment = new ProjectEmployee();
			assignment.setProjectId(id);
			assignment.setEmployeeId(employeeId);
			assignment.setAssignedDate(LocalDate.parse(assignedDate));
			assignment.setEndDate(LocalDate.parse(endDate));
			assignment.setBudgetAllocation(budgetAllocation);
			assignment.setRole(role);
			assignment.setProject(project);
			assignment.setEmployee(employee);

			projectEmployeeRepo.save(assignment);
			redirectAttributes.addFlashAttribute("success", "Staff assigned successfully");
			return "redirect:/project-employee";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Failed to assign staff: " + e.getMessage());
			return "redirect:/projects/assign-staff/" + id;
		}
	}

	// Change this mapping
	@PostMapping("/project-employee/edit/update/{id}")  // Changed from /project-employee/update/{id}
	public String updateAssignment(@PathVariable Long id,
								   @RequestParam Integer employeeId,
								   @RequestParam String assignedDate,
								   @RequestParam String endDate,
								   @RequestParam Double budgetAllocation,
								   @RequestParam String role,
								   RedirectAttributes redirectAttributes) {
		try {
			ProjectEmployee assignment = projectEmployeeRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Assignment not found"));

			assignment.setEmployeeId(employeeId);
			assignment.setAssignedDate(LocalDate.parse(assignedDate));
			assignment.setEndDate(LocalDate.parse(endDate));
			assignment.setBudgetAllocation(budgetAllocation);
			assignment.setRole(role);

			projectEmployeeRepo.save(assignment);
			redirectAttributes.addFlashAttribute("success", "Assignment updated successfully");
			return "redirect:/project-employee";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Failed to update assignment: " + e.getMessage());
			return "redirect:/project-employee";
		}
	}

	@GetMapping("/project-employee")
	public String showAssignments(Model model) {
		try {
			List<ProjectEmployee> assignments = projectEmployeeRepo.findAll();

			// Prepare data for each assignment
			for (ProjectEmployee assignment : assignments) {
				Project project = projectRepo.findById(assignment.getProjectId())
						.orElse(new Project());
				Employee employee = employeeRepo.findById(assignment.getEmployeeId()) // Removed .intValue()
						.orElse(new Employee());

				assignment.setProject(project);
				assignment.setEmployee(employee);
			}

			model.addAttribute("assignments", assignments);
			return "project/project-employee";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Failed to load assignments");
			return "project/project-employee";
		}
	}

	@GetMapping("/project-employee/edit/{id}")
	public String showEditAssignmentForm(@PathVariable Long id, Model model) {
		try {
			ProjectEmployee assignment = projectEmployeeRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Assignment not found"));

			// Load project and employee details
			Project project = projectRepo.findById(assignment.getProjectId())
					.orElse(new Project());
			Employee employee = employeeRepo.findById(assignment.getEmployeeId().intValue())
					.orElse(new Employee());

			assignment.setProject(project);
			assignment.setEmployee(employee);

			List<Employee> employees = employeeRepo.findAll();

			model.addAttribute("assignment", assignment);
			model.addAttribute("employees", employees);
			return "project/edit-assignment";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/project-employee?error=Failed to load edit form";
		}
	}

	@GetMapping("/request/{id}")
	public String viewRequestDetails(@PathVariable Long id, Model model, HttpSession session) {
		try {
			Request request = requestRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Request not found"));

			// Kiểm tra quyền truy cập (chỉ admin hoặc chính nhân viên đó mới được xem)
			Integer userId = (Integer) session.getAttribute("userId");
			Employee employee = employeeRepo.findById(userId).orElse(null);

			if (employee != null && (employee.getRole().equals("ADMIN") ||
					(request.getEmployeeId() != null && request.getEmployeeId().equals(userId)))) {
				model.addAttribute("request", request);
				return "user-request-details";
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

	@GetMapping("/request/{id}/edit")
	public String showRequestEditForm(@PathVariable Long id, Model model, HttpSession session) {
		try {
			Request request = requestRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Request not found"));

			// Kiểm tra quyền truy cập (chỉ nhân viên tạo yêu cầu và trạng thái PENDING được sửa)
			Integer userId = (Integer) session.getAttribute("userId");

			if (request.getEmployeeId() != null && request.getEmployeeId().equals(userId)
					&& request.getStatus().equals("PENDING")) {
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

	@PostMapping("/request/{id}/update")
	public String updateRequest(@PathVariable Long id,
								@RequestParam String subject,
								@RequestParam String text,
								@RequestParam(required = false) MultipartFile file,
								RedirectAttributes redirectAttributes,
								HttpSession session) {
		try {
			Request request = requestRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Request not found"));

			// Kiểm tra quyền truy cập
			Integer userId = (Integer) session.getAttribute("userId");

			if (request.getEmployeeId() != null && request.getEmployeeId().equals(userId)
					&& request.getStatus().equals("PENDING")) {
				request.setSubject(subject);
				request.setDescription(text);

				if (file != null && !file.isEmpty()) {
					request.setAttachment(file.getBytes());
					request.setAttachmentName(file.getOriginalFilename());
				}

				requestRepo.save(request);
				redirectAttributes.addFlashAttribute("message", "Yêu cầu đã được cập nhật thành công!");
			} else {
				redirectAttributes.addFlashAttribute("error", "Không thể cập nhật yêu cầu này!");
			}

			return "redirect:/status-request";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật yêu cầu: " + e.getMessage());
			return "redirect:/status-request";
		}
	}
	@GetMapping("/status-request")
	public String showUserRequests(Model model, HttpSession session) {
		try {
			int userId = Integer.parseInt(session.getAttribute("userId").toString());
			List<Request> requests = requestService.getEmployeeRequests(userId);
			model.addAttribute("requests", requests);
			return "status-request";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Lỗi khi tải danh sách yêu cầu: " + e.getMessage());
			return "error";
		}
	}
	// @GetMapping("/status")
	// public String showAdminRequests(Model model) {
	//     try {
	//         List<Request> requests = requestService.getAllRequests();
	//         model.addAttribute("statusList", requests);
	//         return "status";
	//     } catch (Exception e) {
	//         e.printStackTrace();
	//         model.addAttribute("error", "Lỗi: " + e.getMessage());
	//         return "error";
	//     }
	// }
	@GetMapping("/status")
	public String status(Model model) {
		try {
			List<Request> requests = requestService.getAllRequests();
			model.addAttribute("statusList", requests);
			return "status";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Error loading requests: " + e.getMessage());
			return "error";
		}
	}
	@PostMapping("/request/compose")
	public String createRequest(@RequestParam String subject,
								@RequestParam String text,
								@RequestParam(required = false) MultipartFile file,
								HttpSession session,
								RedirectAttributes redirectAttributes) {
		try {
			int userId = Integer.parseInt(session.getAttribute("userId").toString());
			Employee employee = employeeRepo.findById(userId)
					.orElseThrow(() -> new RuntimeException("Employee not found"));

			Request request = new Request();
			request.setEmployeeId(userId);
			request.setEmployeeName(employee.getEmployeeName());
			request.setEmail(employee.getEmail());
			request.setPosition(employee.getDesignation());
			request.setSubject(subject);
			request.setDescription(text);
			request.setStatus("PENDING");
			request.setCreatedDate(LocalDateTime.now());

			if (file != null && !file.isEmpty()) {
				request.setAttachment(file.getBytes());
				request.setAttachmentName(file.getOriginalFilename());
			}

			requestRepo.save(request);

			redirectAttributes.addFlashAttribute("message", "Yêu cầu đã được gửi thành công!");
			return "redirect:/status-request";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
			return "redirect:/request/compose";
		}
	}

	@GetMapping("/request/compose")
	public String showComposeForm(Model model, HttpSession session) {
		// Add user information to the model
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId != null) {
			Employee employee = employeeRepo.findById(userId).orElse(null);
			if (employee != null) {
				session.setAttribute("userEmail", employee.getEmail());
				session.setAttribute("userName", employee.getEmployeeName());
			}
		}
		return "user-compose";
	}

	@PostMapping("/request/compose/process")
	public String processComposeForm(@RequestParam String subject,
									 @RequestParam String text,
									 @RequestParam(required = false) MultipartFile file,
									 HttpSession session,
									 RedirectAttributes redirectAttributes) {
		return createRequest(subject, text, file, session, redirectAttributes);
	}

	@GetMapping("/request/{id}/attachment")
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

	@GetMapping("/request/update-status")
	public String updateRequestStatus(@RequestParam Long id,
									  @RequestParam String status,
									  RedirectAttributes redirectAttributes) {
		try {
			Request request = requestService.updateStatus(id, status);
			redirectAttributes.addFlashAttribute("message", "Trạng thái yêu cầu đã được cập nhật thành " + status);
			return "redirect:/status";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
			return "redirect:/status";
		}
	}

	@GetMapping("/admin/manage-requests")
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

	@GetMapping("/approve-byId")
	public String approve(@RequestParam("id") Long id, @RequestParam("type") String type, RedirectAttributes redirectAttributes) {
		try {
			requestService.updateStatus(id, type);
			redirectAttributes.addFlashAttribute("success", "Trạng thái yêu cầu đã được cập nhật thành công!");
			return "redirect:/status";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
			return "redirect:/status";
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

	@GetMapping("/login")
	public String login(HttpSession session) {
		// Clear any existing session
		session.invalidate();
		return "login";
	}
	// Add this method near the login-related methods
	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
		try {
			// Clear all session attributes
			session.invalidate();
			redirectAttributes.addFlashAttribute("message", "Logged out successfully");
			return "redirect:/login";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Error during logout");
			return "redirect:/login";
		}
	}
	@GetMapping("/forgot-password")
	public String forgotPassword() {
		return "forgot-password";
	}
	@GetMapping("/home")
	public String home(@RequestParam("username") String username, @RequestParam("password") String password, Model model, HttpSession session) {
		System.out.println("User Name and Password: "+username+" - "+password);
		
		try {
			String empId = username.substring(3);
			System.out.println("empId:- "+empId);
			Employee employee = employeeRepo.findByIdAndPassword(Integer.parseInt(empId), password);
			
			if(employee != null) {
				model.addAttribute("error", false);
				session.setAttribute("userId", Integer.parseInt(empId));
				session.setAttribute("name", employee.getEmployeeName());
				session.setAttribute("desg", employee.getDesignation());
				
				
				if(employee.getRole().equals("USER")) {
					System.out.println("Enter in user "+empId);
					return "redirect:/user-dash-board";
					
				}else if(employee.getRole().equals("ADMIN")) {
					List<Request> findAll = requestRepo.findAll();
					System.out.println("List"+findAll);
					findAll.stream()
					.forEach(k->{
						if (k.getEmployeeId() != null) {
							try {
								int id = k.getEmployeeId();
								Employee emp = employeeRepo.findById(id).orElse(null);
								if (emp != null) {
									k.setDepartment(emp.getDesignation());
								}
							} catch (Exception e) {
								System.err.println("Lỗi khi lấy thông tin nhân viên: " + e.getMessage());
								k.setDepartment("Unknown");
							}
						} else {
							k.setDepartment("Unknown");
						}
					});
					
					model.addAttribute("statusList", findAll);
					return "dash-board";
					
				}else {
					return "redirect:/login";
				}
				
				
			}else {
				model.addAttribute("error", true);
				return "login";
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.err.println(e.getMessage());
			return "redirect:/login";
		}
		
		
	}
	@GetMapping("/dash-board")
	public String dashBoard(Model model) {
		try {
			// Department counts
			model.addAttribute("developmentCount", employeeRepo.countByDepartment("Development"));
			model.addAttribute("qaCount", employeeRepo.countByDepartment("QA & Automation Testing")); 
			model.addAttribute("networkingCount", employeeRepo.countByDepartment("Networking"));
			model.addAttribute("hrCount", employeeRepo.countByDepartment("HR Team"));
			model.addAttribute("securityCount", employeeRepo.countByDepartment("Security"));
			model.addAttribute("salesCount", employeeRepo.countByDepartment("Sales Marketing"));

			// Status counts
			model.addAttribute("pendingCount", requestRepo.countByStatus("PENDING"));
			model.addAttribute("approvedCount", requestRepo.countByStatus("APPROVED"));
			model.addAttribute("canceledCount", requestRepo.countByStatus("CANCELED"));
			model.addAttribute("deniedCount", requestRepo.countByStatus("DENIED"));
			model.addAttribute("allCount", requestRepo.count());

			// Recent Claims with position update
			List<Request> recentRequests = requestRepo.findTop5ByOrderByCreatedDateDesc();
			// Cập nhật position cho mỗi request
			for (Request request : recentRequests) {
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
			}
			model.addAttribute("statusList", recentRequests);

			// In the dashBoard method, format the month as a two-digit string
			LocalDate now = LocalDate.now();
			String currentMonth = String.format("%02d", now.getMonthValue());
			List<Employee> birthdays = employeeRepo.findByBirthMonth(currentMonth);
			model.addAttribute("upcomingBirthdays", birthdays);

			return "dash-board";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
			return "error";
		}
	}
	@GetMapping("/add-employee")
	public String addEmployee(Model model) {
		model.addAttribute("employee", new Employee());


		return "add-employee";
	}
	
	@GetMapping("/all-employee")
	public  String allEmployee(Model model) {
		
		// get all record
		List<Employee> allEmployee = service.getAllEmployee();
		model.addAttribute("allEmployee", allEmployee);
		return "all-employee";
	}
	
	@GetMapping("/create-post")
	public String createPost(Model model) {
		
		List<CreatePost> findAll = createPostRepo.findAll();
		model.addAttribute("post", findAll);
		
		return "create-post";
	}

	// @GetMapping("/status")
	// public String status(Model model) {
		
	// 	List<Compose> findAll = composeRepo.findAll();
		
	// 	findAll.stream()
	// 	.forEach(k->{
	// 		int id = k.getParentUkid();
	// 		String designation = employeeRepo.findById(id).get().getDesignation();
	// 		k.setPosition(designation);
	// 	});
		
	// 	model.addAttribute("statusList", findAll);
		
	// 	return "status";
	// }
// 	@GetMapping("/status")
// public String status(Model model) {
//     try {
//         List<Request> requests = requestService.getAllRequests();
//         requests.forEach(request -> {
//             Employee employee = employeeRepo.findById(request.getEmployeeId()).orElse(null);
//             if (employee != null) {
//                 request.setPosition(employee.getDesignation());
//             }
//         });
//         model.addAttribute("statusList", requests);
//         return "status";
//     } catch (Exception e) {
//         e.printStackTrace();
//         model.addAttribute("error", "Error loading requests: " + e.getMessage());
//         return "error";
//     }
// }


	// @GetMapping("/status-request")
	// public String statusRequest(Model model, HttpSession session) {
	// 	try {
	// 		int userId = Integer.parseInt(session.getAttribute("userId").toString());
	// 		List<Request> requests = requestService.getEmployeeRequests(userId);
	// 		model.addAttribute("requests", requests);
	// 		return "status-request";
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		model.addAttribute("error", "Error loading requests: " + e.getMessage());
	// 		return "error";
	// 	}
	// }
	
	@GetMapping("/my-profile")
	public String myProfile(HttpSession session, Model model) {
		
		Object attribute = session.getAttribute("userId");
		
		int userId = Integer.parseInt(attribute.toString());
		
		Employee employee = employeeRepo.findById(userId).get();
		model.addAttribute("employee", employee);
		
		
		System.out.println("Object:- "+attribute);
		
		return "my-profile";
	}
	
	@GetMapping("/setting")
	public String setting() {
		
		return "setting";
	}
	
	@PostMapping("/save-employee")
	public String saveEmployee(@Valid @ModelAttribute Employee employee, BindingResult result, Model model) {
		if(result.hasErrors()){
			model.addAttribute("employee", employee);

			return "add-employee";
		}
		employee.setPassword(employee.getDateOfBirth());
		
		
		Employee save = service.addEmployee(employee);
		return "redirect:/all-employee";
	}
	
	@PostMapping("/save-post")
	public String savePost(@ModelAttribute CreatePost createPost) {
		
		createPost.setAddedDate(new Date().toString());
		CreatePost addPost = service.addPost(createPost);
		return "redirect:/create-post";
		
	}
	@PostMapping("/update-password")
	public String updatePassword(@RequestParam("password")String password,@RequestParam("newPassword1")String newPassword1, @RequestParam("newPassword2")String newPassword2, HttpSession session, Model model) {
		System.out.println(password+" - "+newPassword1+" - "+newPassword2);
		Object attribute = session.getAttribute("userId");

		int userId = Integer.parseInt(attribute.toString());

		Employee employee = employeeRepo.findByIdAndPassword(userId, password);

		if(employee != null && newPassword1.equals(newPassword2)) {
 			employee.setPassword(newPassword2);
			 employeeRepo.save(employee);
			model.addAttribute("error", false);
		}else{
			model.addAttribute("error", true);
			return "setting";
		}


		return "redirect:/login";
	}

	@GetMapping("/edit-record")
	public String editRecord(@RequestParam("id") int id, Model model){

		System.out.println("ID: " +id);
		Employee employee = employeeRepo.findById(id).get();
		model.addAttribute("employee", employee);
	return "edit-record";
	}
	@PostMapping("/edit-employee")
	public String updateRecord(@ModelAttribute("employee") Employee employee, RedirectAttributes redirectAttributes) {
			try {
					Employee existingEmp = employeeRepo.findById(employee.getId()).orElse(null);
					if (existingEmp != null) {
							// Preserve existing values
							employee.setPassword(existingEmp.getPassword());
							employee.setRole(existingEmp.getRole());
							employee.setActive(existingEmp.isActive());
							
							// Save updated employee
							employeeRepo.save(employee);
							redirectAttributes.addFlashAttribute("message", "Employee updated successfully!");
					}
			} catch (Exception e) {
					redirectAttributes.addFlashAttribute("error", "Failed to update employee: " + e.getMessage());
					e.printStackTrace();
			}
			return "redirect:/all-employee";
	}
	
	@GetMapping("/deleteRecord-byId")
	public String deleteRecordById(@RequestParam("id") int id) {
		
		employeeRepo.deleteById(id);
		
		return "redirect:/all-employee";
		
	}
	
	@GetMapping("/user-dash-board")
    public String userDashBoard(Model model, HttpSession session) {
        try {
            // Get current user ID from session
            int userId = (int) session.getAttribute("userId");

            // Department counts
            model.addAttribute("developmentCount", employeeRepo.countByDepartment("Development"));
            model.addAttribute("qaCount", employeeRepo.countByDepartment("QA Testing"));
            model.addAttribute("networkingCount", employeeRepo.countByDepartment("Networking"));
            model.addAttribute("hrCount", employeeRepo.countByDepartment("HR Team"));
            model.addAttribute("securityCount", employeeRepo.countByDepartment("Security"));
            model.addAttribute("salesCount", employeeRepo.countByDepartment("Sales Marketing"));

            // User's request counts by status
            model.addAttribute("pendingCount", requestRepo.countByEmployeeIdAndStatus(userId, "PENDING"));
            model.addAttribute("approvedCount", requestRepo.countByEmployeeIdAndStatus(userId, "APPROVED"));
            model.addAttribute("canceledCount", requestRepo.countByEmployeeIdAndStatus(userId, "CANCELED"));
            model.addAttribute("deniedCount", requestRepo.countByEmployeeIdAndStatus(userId, "DENIED"));
            model.addAttribute("allCount", requestRepo.countByEmployeeId(userId));
            model.addAttribute("reminderCount", requestRepo.countByEmployeeIdAndStatus(userId, "REMINDER"));

            // Recent claims for the user with position update
            List<Request> userRequests = requestRepo.findTop5ByEmployeeIdOrderByCreatedDateDesc(userId);
            
            // Cập nhật position cho từng request
            Employee currentEmployee = employeeRepo.findById(userId).orElse(null);
            if (currentEmployee != null) {
                String department = currentEmployee.getDepartment();
                String email = currentEmployee.getEmail();
                String name = currentEmployee.getEmployeeName();
                
                for (Request request : userRequests) {
                    request.setPosition(department);
                    request.setDepartment(department);
                    request.setEmail(email);
                    request.setEmployeeName(name);
                }
            }
            
            model.addAttribute("statusList", userRequests);

            // Upcoming birthdays
            LocalDate now = LocalDate.now();
            List<Employee> birthdays = employeeRepo.findByBirthMonth(String.format("%02d", now.getMonthValue()));
            model.addAttribute("upcomingBirthdays", birthdays);

            return "user-dash-board";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }
	@GetMapping("/user-project")
public String userProjects(Model model, HttpSession session) {
    try {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get employee assignments and their roles
        List<ProjectEmployee> assignments = projectEmployeeRepo.findByEmployeeId(userId);
        
        // Get all projects where user is either assigned or is lead
        List<Project> userProjects = new ArrayList<>();
        
        // Add projects where user is assigned
        for (ProjectEmployee assignment : assignments) {
            Project project = projectRepo.findById(assignment.getProjectId()).orElse(null);
            if (project != null) {
                // Set project-specific details from assignment
                project.setEndDate(assignment.getEndDate());
                if (assignment.getBudgetAllocation() != null) {
                    project.setBudgetNo(Double.valueOf(assignment.getBudgetAllocation()));
                }
                userProjects.add(project);
            }
        }
        
        // Add projects where user is lead (if not already added)
        List<Project> leadProjects = projectRepo.findByLeadEmployeeId(userId);
        if (leadProjects != null) {
            for (Project leadProject : leadProjects) {
                if (!userProjects.contains(leadProject)) {
                    userProjects.add(leadProject);
                }
            }
        }
        
        Employee employee = employeeRepo.findById(userId).orElse(null);
        
        model.addAttribute("projects", userProjects);
        model.addAttribute("employee", employee);
        model.addAttribute("assignments", assignments);
        
        return "user-project";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Error loading projects: " + e.getMessage());
        return "error";
    }
}
	@GetMapping("/user-profile")
	public String userProfile(HttpSession session, Model model) {

		Object attribute = session.getAttribute("userId");

		int userId = Integer.parseInt(attribute.toString());

		Employee employee = employeeRepo.findById(userId).get();
		model.addAttribute("employee", employee);


		System.out.println("Object:- "+attribute);

		return "user-profile";
	}

	@GetMapping("/user-setting")
	public String userSetting() {

		return "user-setting";
	}
	@GetMapping("/user-compose")
	public String compose(){
		return "user-compose";
}
// 	@PostMapping("/compose")
// 	public String addCompose(@RequestParam()String subject, @RequestParam("text") String text, HttpSession session){
//         try {
//             Object attribute = session.getAttribute("userId");

//             int userId = Integer.parseInt(attribute.toString());

//             Employee employee = employeeRepo.findById(userId).get();

//             Compose com = new Compose();
//             com.setEmpName(employee.getEmployeeName());
//             com.setSubject(subject);
//             com.setText(text);
//             com.setParentUkid(userId);
//             com.setAddedDate(new Date().toString());
//             com.setStatus("PENDING");
//             Compose save = composeRepo.save(com);
//         } catch (NumberFormatException e) {
//             throw new RuntimeException(e);
//         }
//         return "redirect:/user-compose";
// }

// @PostMapping("/compose")
// public String addCompose(@RequestParam String subject,
//                         @RequestParam String text,
//                         @RequestParam(required = false) MultipartFile file,
//                         HttpSession session,
//                         RedirectAttributes redirectAttributes) {
//     try {
//         int userId = Integer.parseInt(session.getAttribute("userId").toString());
//         Employee employee = employeeRepo.findById(userId).get();

//         Request request = new Request();
//         request.setEmployeeId(userId);  // Set employeeId trực tiếp
//         request.setEmployeeName(employee.getEmployeeName());
//         request.setEmail(employee.getEmail());
//         request.setPosition(employee.getDesignation());
//         request.setSubject(subject);
//         request.setDescription(text);
//         // status và createdDate được set trong constructor

//         requestService.createRequest(request, file);
        
//         redirectAttributes.addFlashAttribute("message", "Request created successfully!");
//         return "redirect:/status-request";
//     } catch (Exception e) {
//         e.printStackTrace();
//         redirectAttributes.addFlashAttribute("error", "Failed to create request: " + e.getMessage());
//         return "redirect:/user-compose";
//     }
// }
// @GetMapping("/approve-byId")
// public String approve(@RequestParam("id") Long id, @RequestParam("type") String type) {
//     try {
//         requestService.updateStatus(id, type);
//         return "redirect:/status";
//     } catch (Exception e) {
//         e.printStackTrace();
//         return "redirect:/error";
//     }
// }
//	@GetMapping("/approve-byId")
//	public String approve(@RequestParam("id") int id, @RequestParam("type") String type) {
//
//		System.out.println(id+" ==== "+type);
//		Compose compose = composeRepo.findById(id).get();
//		compose.setStatus(type);
//
//		composeRepo.save(compose);
//
//		return "redirect:/status";
//	}
//	// Project Management Methods
//	@GetMapping("/projects")
//	public String showProjects(Model model) {
//		model.addAttribute("projects", projectRepo.findAll());
//		model.addAttribute("employees", employeeRepo.findAll());
//		return "project";
//	}
	//Add this mapping method
	@GetMapping("/projects")
	public String showProjects(Model model, HttpSession session) {
		try {
			// Kiểm tra xem người dùng đã đăng nhập chưa
            if (session.getAttribute("userId") == null) {
                return "redirect:/login";
            }
			
			List<Project> projects = service.getAllProjects();
			List<Employee> employees = service.getAllEmployee();

			model.addAttribute("projects", projects);
			model.addAttribute("employees", employees);
			return "/project/project";
		} catch (Exception e) {
			model.addAttribute("error", "Error loading projects: " + e.getMessage());
			return "/project/project";
		}
	}
	@PostMapping("/projects/add")
	public String addProject(@ModelAttribute Project project, RedirectAttributes redirectAttributes) {
		try {
			projectRepo.save(project);
			redirectAttributes.addFlashAttribute("message", "Project added successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to add project: " + e.getMessage());
		}
		return "redirect:/projects";
	}

	@PostMapping("/projects/update/{id}")
public String updateProject(@PathVariable Long id, @ModelAttribute Project project, RedirectAttributes redirectAttributes) {
    try {
        Project existingProject = projectRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Update only the fields that should be updated
        BeanUtils.copyProperties(project, existingProject, "id", "createdDate");
        
        projectRepo.save(existingProject);
        redirectAttributes.addFlashAttribute("success", "Project updated successfully");
        return "redirect:/projects";
    } catch (Exception e) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Failed to update project: " + e.getMessage());
        return "redirect:/projects";
    }
}

	@DeleteMapping("/projects/delete/{id}")
	@ResponseBody
	public ResponseEntity<?> deleteProject(@PathVariable Long id) {
		try {
			projectRepo.deleteById(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Failed to delete project");
		}
	}

	// Project-Employee Management Methods
	@PostMapping("/project-employee/add")
	public String addProjectEmployee(@ModelAttribute ProjectEmployee projectEmployee,
									 RedirectAttributes redirectAttributes) {
		try {
			projectEmployeeRepo.save(projectEmployee);
			redirectAttributes.addFlashAttribute("success", "Employee assigned successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to assign employee");
		}
		return "redirect:/projects";
	}

	@PostMapping("/project-employee/update/{id}")
	public String updateProjectEmployee(@PathVariable Long id,
										@ModelAttribute ProjectEmployee projectEmployee,
										RedirectAttributes redirectAttributes) {
		try {
			projectEmployee.setId(id);
			projectEmployeeRepo.save(projectEmployee);
			redirectAttributes.addFlashAttribute("success", "Assignment updated successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to update assignment");
		}
		return "redirect:/projects";
	}

	@DeleteMapping("/project-employee/delete/{id}")
	@ResponseBody
	public ResponseEntity<?> deleteProjectEmployee(@PathVariable Long id) {
		try {
			projectEmployeeRepo.deleteById(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Failed to delete assignment");
		}
	}

	// Project Details Retrieval Methods
	@GetMapping("/projects/{id}")
	@ResponseBody
	public ResponseEntity<?> getProject(@PathVariable Long id) {
		try {
			Project project = projectRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Project not found"));
			return ResponseEntity.ok(project);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Failed to get project details");
		}
	}

	// @GetMapping("/project-employee/{id}")
	// @ResponseBody
	// public ResponseEntity<?> getProjectEmployee(@PathVariable Long id) {
	// 	try {
	// 		ProjectEmployee projectEmployee = projectEmployeeRepo.findById(id)
	// 				.orElseThrow(() -> new RuntimeException("Assignment not found"));
	// 		return ResponseEntity.ok(projectEmployee);
	// 	} catch (Exception e) {
	// 		return ResponseEntity.badRequest().body("Failed to get assignment details");
	// 	}
	// }
	// @GetMapping("/project-employee")
	// public String showProjectEmployee(Model model) {
	// 	model.addAttribute("projects", projectRepo.findAll());
	// 	model.addAttribute("employees", employeeRepo.findAll());
	// 	model.addAttribute("projectEmployees", projectEmployeeRepo.findAll());
	// 	return "/project/project-employee";
	// }

	@GetMapping("/add-project")
	public String showAddProjectForm(Model model, HttpSession session) {
	    // Kiểm tra xem người dùng đã đăng nhập chưa
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        
		List<Employee> showAddProjectForm = service.getAllEmployee();
		model.addAttribute("employees", showAddProjectForm);
		return "/project/add-project";
	}


	@GetMapping("/projects-list")
    public String showProjectsList(Model model, HttpSession session) {
        try {
            // Kiểm tra xem người dùng đã đăng nhập chưa
            if (session.getAttribute("userId") == null) {
                return "redirect:/login";
            }
            
            List<Project> projects = projectRepo.findAll();
            List<Employee> employees = employeeRepo.findAll();

            model.addAttribute("projects", projects);
            model.addAttribute("employees", employees);
            return "/project/project";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading projects: " + e.getMessage());
            return "/project/project";
        }
    }

    @GetMapping("/projects/edit/{id}")
    public String showEditProjectForm(@PathVariable Long id, Model model, HttpSession session) {
        try {
            // Kiểm tra xem người dùng đã đăng nhập chưa
            if (session.getAttribute("userId") == null) {
                return "redirect:/login";
            }
            
            // Lấy thông tin project
            Project project = projectRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
            
            // Lấy danh sách employees
            List<Employee> employees = employeeRepo.findAll();
            
            // Add vào model
            model.addAttribute("project", project);
            model.addAttribute("employees", employees);
            
            // Return view
            return "project/edit-project";
        } catch (Exception e) {
            // Log lỗi
            e.printStackTrace();
            // Redirect về trang projects với thông báo lỗi
            return "redirect:/projects?error=Failed to load project details";
        }
    }


}
