package com.cq.reposetory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cq.entity.Employee;

public interface EmployeeRepo extends JpaRepository<Employee, Integer> {
    
    public Employee findByIdAndPassword(int empId, String password);

    public Employee findByIdAndPasswordAndRole(int empId, String password, String role);

    @Query("SELECT COUNT(e) FROM Employee e WHERE UPPER(e.department) = UPPER(?1)")
    Long countByDepartment(String department);

    // Updated to handle date format properly
    @Query(value = "SELECT * FROM employee WHERE DATE_FORMAT(date_of_birth, '%m') = :month", nativeQuery = true)
    List<Employee> findByBirthMonth(@Param("month") String month);
}
