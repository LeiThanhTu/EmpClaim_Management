package com.cq.reposetory;

import com.cq.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepos extends JpaRepository<Employee, Integer>{
	
	public Employee findByIdAndPassword(int empId, String password ) ;

	public Employee findByIdAndPasswordAndRole(int empId, String password, String role ) ;
}
