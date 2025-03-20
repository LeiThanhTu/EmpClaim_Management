package com.cq.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cq.entity.CreatePost;
import com.cq.entity.Employee;
import com.cq.reposetory.CreatePostRepo;
import com.cq.reposetory.EmployeeRepo;
import com.cq.service.HrService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;


@Controller
public class HrController {
	
	@Autowired
	private HrService service;
	
	@Autowired
	private CreatePostRepo createPostRepo;
	
	@Autowired
	private EmployeeRepo employeeRepo;
	
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
		
		String empId = username.substring(3);
		System.out.println("empId:- "+empId);
		Employee employee = employeeRepo.findByIdAndPassword(Integer.parseInt(empId), password);
		
		if(employee != null) {
			model.addAttribute("error", false);
			session.setAttribute("userId", Integer.parseInt(empId));
			session.setAttribute("name", employee.getEmployeeName());
			session.setAttribute("desg", employee.getDesignation());
			return "dash-board";
		}else {
			model.addAttribute("error", true);
			return "login";
		}
		
		
	}
	
	@GetMapping("/dash-board")
	public String dashBard() {
		
		return "dash-board";	
	}
	
	@GetMapping("/add-employee")
	public String addEmployee() {
		
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
	public String status() {
		
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
	public String saveEmployee(@ModelAttribute Employee employee) {
		
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
	public String updateRecord(@ModelAttribute Employee employee) {
		int id = employee.getId();
		
		Employee getEmp = employeeRepo.findById(id).get();
		
		if(getEmp !=  null) {
			employeeRepo.save(employee);
		}
		
		return "redirect:/all-employee";
	}
	
	@GetMapping("/deleteRecord-byId")
	public String deleteRecordById(@RequestParam("id") int id) {
		
		employeeRepo.deleteById(id);
		
		return "redirect:/all-employee";
	}
}
