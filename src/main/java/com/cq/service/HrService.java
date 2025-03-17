package com.cq.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cq.entity.CreatePost;
import com.cq.entity.Employee;
import com.cq.reposetory.CreatePostRepo;
import com.cq.reposetory.EmployeeRepo;

@Service
public class HrService {

	@Autowired
	private EmployeeRepo employeeRepo;
	
	@Autowired
	private CreatePostRepo createPostRepo;
	
	public Employee addEmployee (Employee employee) {
		
		Employee save = employeeRepo.save(employee);
		
		return save;
		
	}
	
	public List<Employee> getAllEmployee() {
		List<Employee> findAll = employeeRepo.findAll();
		
		return findAll;
	}
	
	public CreatePost addPost(CreatePost createPost) {
		CreatePost save = createPostRepo.save(createPost);
		
		return save;
	}
	
}
