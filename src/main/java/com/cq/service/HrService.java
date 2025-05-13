package com.cq.service;

import java.util.List;

import com.cq.entity.Project;
import com.cq.entity.ProjectEmployee;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cq.entity.CreatePost;
import com.cq.entity.Employee;
import com.cq.reposetory.CreatePostRepo;
import com.cq.reposetory.EmployeeRepo;
import com.cq.reposetory.ProjectRepo;

import jakarta.transaction.Transactional;

import com.cq.reposetory.ProjectEmployeeRepo;

@Service
public class HrService {

	@Autowired
	private EmployeeRepo employeeRepo;

	@Autowired
	private CreatePostRepo createPostRepo;

	@Autowired
	private ProjectRepo projectRepo;

	@Autowired
	private ProjectEmployeeRepo projectEmployeeRepo;

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

	// Project Management Methods
	public List<Project> getAllProjects() {
		return projectRepo.findAll();
	}

	public Project addProject(Project project) {
		return projectRepo.save(project);
	}

	public Project updateProject(Project project) {
		if (projectRepo.existsById(project.getId())) {
			return projectRepo.save(project);
		}
		throw new RuntimeException("Project not found with id: " + project.getId());
	}

	public void deleteProject(Long id) {
		if (projectRepo.existsById(id)) {
			projectRepo.deleteById(id);
		} else {
			throw new RuntimeException("Project not found with id: " + id);
		}
	}

	// Project-Employee Management Methods
	public List<ProjectEmployee> getAllProjectEmployees() {
		return projectEmployeeRepo.findAll();
	}

	public ProjectEmployee assignEmployeeToProject(ProjectEmployee projectEmployee) {
		Project project = projectRepo.findById(projectEmployee.getProjectId())
		.orElseThrow(() -> new RuntimeException("Project not found"));
		
Employee employee = employeeRepo.findById(projectEmployee.getEmployeeId())
		.orElseThrow(() -> new RuntimeException("Employee not found"));

// Validate dates
if (projectEmployee.getEndDate() != null && 
	projectEmployee.getEndDate().isBefore(projectEmployee.getAssignedDate())) {
	throw new RuntimeException("End date cannot be before assigned date");
}

// Validate budget allocation
if (projectEmployee.getBudgetAllocation() != null && projectEmployee.getBudgetAllocation() < 0) {
	throw new RuntimeException("Budget allocation cannot be negative");
}

return projectEmployeeRepo.save(projectEmployee);
	}

	@Transactional
public ProjectEmployee updateProjectEmployee(ProjectEmployee projectEmployee) {
    ProjectEmployee existing = projectEmployeeRepo.findById(projectEmployee.getId())
            .orElseThrow(() -> new RuntimeException("Project assignment not found"));
            
    // Validate dates
    if (projectEmployee.getEndDate() != null && 
        projectEmployee.getEndDate().isBefore(projectEmployee.getAssignedDate())) {
        throw new RuntimeException("End date cannot be before assigned date");
    }
    
    // Validate budget allocation
    if (projectEmployee.getBudgetAllocation() != null && projectEmployee.getBudgetAllocation() < 0) {
        throw new RuntimeException("Budget allocation cannot be negative");
    }
    
    return projectEmployeeRepo.save(projectEmployee);
}

	public void deleteProjectEmployee(Long id) {
		if (projectEmployeeRepo.existsById(id)) {
			projectEmployeeRepo.deleteById(id);
		} else {
			throw new RuntimeException("Project assignment not found with id: " + id);
		}
	}

	// Additional Helper Methods
	public Project getProjectById(Long id) {
		return projectRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
	}

	public ProjectEmployee getProjectEmployeeById(Long id) {
		return projectEmployeeRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Project assignment not found with id: " + id));
	}

	public List<ProjectEmployee> getProjectEmployeesByProjectId(Long projectId) {
		return projectEmployeeRepo.findByProjectId(projectId);
	}

	public List<ProjectEmployee> getProjectEmployeesByEmployeeId(Integer employeeId) {
		return projectEmployeeRepo.findByEmployeeId(employeeId);
	}

	// Add these methods to HrService
	public List<Project> getAllProjects(Long projectId) {
		return projectRepo.findAll();
	}


	public Project updateProject(Long id, Project project) {
		project.setId(id);
		return projectRepo.save(project);
	}
}
