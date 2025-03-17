package com.cq.reposetory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cq.entity.Employee;

public interface EmployeeRepo extends JpaRepository<Employee, Integer>{
	
	public Employee findByIdAndPassword(int empId, String password ) ;

}
