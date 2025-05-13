package com.cq.reposetory;

import com.cq.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepo extends JpaRepository<Request, Long> {
    List<Request> findByEmployeeIdOrderByCreatedDateDesc(Integer employeeId);
    
    // Add method to find by employee relationship
    List<Request> findByEmployee_IdOrderByCreatedDateDesc(Integer employeeId);
    
    // Add these new methods
    Long countByStatus(String status);
    
    List<Request> findTop5ByOrderByCreatedDateDesc();
    Long countByEmployeeIdAndStatus(int employeeId, String status);
    Long countByEmployeeId(int employeeId);
    List<Request> findTop5ByEmployeeIdOrderByCreatedDateDesc(int employeeId);
    // Existing methods...
}
