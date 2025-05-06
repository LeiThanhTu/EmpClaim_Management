package com.cq.controller;

import com.cq.entity.Employee;
import com.cq.entity.Project;
import com.cq.entity.ProjectEmployee;
import com.cq.reposetory.EmployeeRepo;
import com.cq.reposetory.ProjectRepo;
import com.cq.reposetory.ProjectEmployeeRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ProjectAssignmentController {

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private ProjectEmployeeRepo projectEmployeeRepo;

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
                            @RequestParam String role,
                            RedirectAttributes redirectAttributes) {
        try {
            ProjectEmployee assignment = new ProjectEmployee();
            assignment.setProjectId(id);
            assignment.setEmployeeId(employeeId.longValue());
            assignment.setAssignedDate(LocalDate.parse(assignedDate));
            assignment.setRole(role);

            projectEmployeeRepo.save(assignment);
            redirectAttributes.addFlashAttribute("success", "Staff assigned successfully");
            return "redirect:/project-employee";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to assign staff: " + e.getMessage());
            return "redirect:/projects";
        }
    }

    @GetMapping("/project-employee")
    public String showAssignments(Model model) {
        try {
            List<ProjectEmployee> assignments = projectEmployeeRepo.findAll();
            
            // Prepare data for each assignment
            for (ProjectEmployee assignment : assignments) {
                Project project = projectRepo.findById(assignment.getProjectId())
                        .orElse(new Project()); // fallback to empty project if not found
                Employee employee = employeeRepo.findById(assignment.getEmployeeId().intValue())
                        .orElse(new Employee()); // fallback to empty employee if not found
                
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

    // @PostMapping("/project-employee/update/{id}")
    // public String updateAssignment(@PathVariable Long id,
    //                              @RequestParam Integer employeeId,
    //                              @RequestParam String assignedDate,
    //                              @RequestParam String role,
    //                              RedirectAttributes redirectAttributes) {
    //     try {
    //         ProjectEmployee assignment = projectEmployeeRepo.findById(id)
    //                 .orElseThrow(() -> new RuntimeException("Assignment not found"));

    //         assignment.setEmployeeId(employeeId.longValue());
    //         assignment.setAssignedDate(LocalDate.parse(assignedDate));
    //         assignment.setRole(role);

    //         projectEmployeeRepo.save(assignment);
    //         redirectAttributes.addFlashAttribute("success", "Assignment updated successfully");
    //         return "redirect:/project-employee";
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         redirectAttributes.addFlashAttribute("error", "Failed to update assignment: " + e.getMessage());
    //         return "redirect:/project-employee";
    //     }
    // }
} 