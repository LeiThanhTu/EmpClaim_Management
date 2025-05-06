package com.cq.reposetory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cq.entity.Employee;

public interface EmployeeRepo extends JpaRepository<Employee, Integer>{
	
	public Employee findByIdAndPassword(int empId, String password ) ;

	public Employee findByIdAndPasswordAndRole(int empId, String password, String role ) ;

    @Query("SELECT COUNT(e) FROM Employee e WHERE UPPER(e.department) = UPPER(?1)")
    Long countByDepartment(String department);

@Query(value = "SELECT * FROM Employee WHERE MONTH(STR_TO_DATE(e.dateOfBirth, '%Y-%m-%d')) = MONTH(CURRENT_DATE) ORDER BY DAY(STR_TO_DATE(e.dateOfBirth, '%Y-%m-%d'))", nativeQuery = true)
List<Employee> findUpcomingBirthdays();
}
