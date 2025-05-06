package com.cq.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.cq.entity.*;
import com.cq.reposetory.*;
import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.cq.service.HrService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// Add these imports at the top
import com.cq.entity.Project;


@Controller
public class HrController {
	
	@Autowired
	private HrService service;
	
	@Autowired
	private CreatePostRepo createPostRepo;
	
	@Autowired
	private EmployeeRepo employeeRepo;

	@Autowired
	private ComposeRepo composeRepo;

	@Autowired
	private ProjectRepo projectRepo;

	@Autowired
	private ProjectEmployeeRepo projectEmployeeRepo;
	
	@GetMapping("/login")
	// @ResponseBody
	public String login() {
		return "login";
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
					List<Compose> findAll = composeRepo.findAll();
					System.out.println("List"+findAll);
					findAll.stream()
					.forEach(k->{
						int id = k.getParentUkid();
						String designation = employeeRepo.findById(id).get().getDesignation();
						k.setPosition(designation);
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
			model.addAttribute("qaCount", employeeRepo.countByDepartment("QA Testing")); 
			model.addAttribute("networkingCount", employeeRepo.countByDepartment("Networking"));
			model.addAttribute("hrCount", employeeRepo.countByDepartment("HR Team"));
			model.addAttribute("securityCount", employeeRepo.countByDepartment("Security"));
			model.addAttribute("salesCount", employeeRepo.countByDepartment("Sales Market"));
	
			// Status counts
			model.addAttribute("pendingCount", composeRepo.countByStatus("PENDING"));
			model.addAttribute("approvedCount", composeRepo.countByStatus("APPROVE"));
			model.addAttribute("canceledCount", composeRepo.countByStatus("CANCELED"));
			model.addAttribute("deniedCount", composeRepo.countByStatus("DENIED"));
			model.addAttribute("reminderCount", composeRepo.countByStatus("REMINDER"));
			model.addAttribute("allCount", composeRepo.count());
	
			// Recent claims
			List<Compose> statusList = composeRepo.findTop10ByOrderByAddedDateDesc();
			model.addAttribute("statusList", statusList);
	
			// Upcoming birthdays - simplified query
			List<Employee> upcomingBirthdays = employeeRepo.findAll().stream()
				.filter(emp -> {
					try {
						String[] dateParts = emp.getDateOfBirth().split("-");
						return Integer.parseInt(dateParts[1]) == java.time.LocalDate.now().getMonthValue();
					} catch (Exception e) {
						return false;
					}
				})
				.collect(Collectors.toList());
			model.addAttribute("upcomingBirthdays", upcomingBirthdays);
	
			return "dash-board";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", e.getMessage());
			return "error";
		}
	}
	
	@GetMapping("/add-employee")
	public String addEmployee(Model model) {
		model.addAttribute("employee", new Employee());


		return "add-employee";
	}
	
	@GetMapping("/all-employee")
	public String allEmployee(Model model) {
		
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
	
	@GetMapping("/status")
	public String status(Model model) {
		
		List<Compose> findAll = composeRepo.findAll();
		
		findAll.stream()
		.forEach(k->{
			int id = k.getParentUkid();
			String designation = employeeRepo.findById(id).get().getDesignation();
			k.setPosition(designation);
		});
		
		model.addAttribute("statusList", findAll);
		
		return "status";
	}
	
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
	public String userDashBord(Model model, HttpSession session) {
		Object attribute = session.getAttribute("userId");
		
		int userId = Integer.parseInt(attribute.toString());
		
	List<Compose> findAll = composeRepo.findByParentUkid(userId);
		System.out.println("List"+findAll);
		findAll.stream()
		.forEach(k->{
			int id = k.getParentUkid();
			String designation = employeeRepo.findById(id).get().getDesignation();
			k.setPosition(designation);
		});
		
		model.addAttribute("statusList", findAll);
		return "user-dash-board";
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
	@PostMapping("/compose")
	public String addCompose(@RequestParam()String subject, @RequestParam("text") String text, HttpSession session){
        try {
            Object attribute = session.getAttribute("userId");

            int userId = Integer.parseInt(attribute.toString());

            Employee employee = employeeRepo.findById(userId).get();

            Compose com = new Compose();
            com.setEmpName(employee.getEmployeeName());
            com.setSubject(subject);
            com.setText(text);
            com.setParentUkid(userId);
            com.setAddedDate(new Date().toString());
            com.setStatus("PENDING");
            Compose save = composeRepo.save(com);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/user-compose";
}
	@GetMapping("/approve-byId")
	public String approve(@RequestParam("id") int id, @RequestParam("type") String type) {
		
		System.out.println(id+" ==== "+type);
		Compose compose = composeRepo.findById(id).get();
		compose.setStatus(type);
		
		composeRepo.save(compose);
		
		return "redirect:/status";
	}
//	// Project Management Methods
//	@GetMapping("/projects")
//	public String showProjects(Model model) {
//		model.addAttribute("projects", projectRepo.findAll());
//		model.addAttribute("employees", employeeRepo.findAll());
//		return "project";
//	}
	//Add this mapping method
	@GetMapping("/projects")
	public String showProjects(Model model) {
		try {
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
	public String updateProject(@PathVariable Long id, @ModelAttribute Project project,
								RedirectAttributes redirectAttributes) {
		try {
			Project existingProject = projectRepo.findById(id)
					.orElseThrow(() -> new RuntimeException("Project not found"));
			
			existingProject.setProjectName(project.getProjectName());
			existingProject.setClientName(project.getClientName());
			existingProject.setStartDate(project.getStartDate());
			existingProject.setEndDate(project.getEndDate());
			existingProject.setStatus(project.getStatus());
			existingProject.setBudgetNo(project.getBudgetNo());
			existingProject.setLeadEmployeeId(project.getLeadEmployeeId());
			existingProject.setContactPerson(project.getContactPerson());
			existingProject.setContactNo(project.getContactNo());
			existingProject.setEmailId(project.getEmailId());
			
			projectRepo.save(existingProject);
			redirectAttributes.addFlashAttribute("success", "Project updated successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to update project");
		}
		return "redirect:/projects";
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
	public String showAddProjectForm(Model model) {
		List<Employee> showAddProjectForm = service.getAllEmployee();
		model.addAttribute("employees", showAddProjectForm);
		return "/project/add-project";
	}


	@GetMapping("/projects-list")
public String showProjectsList(Model model) {
    try {
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
public String showEditProjectForm(@PathVariable Long id, Model model) {
    try {
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
