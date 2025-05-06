package com.cq.reposetory;

import com.cq.entity.Project;
import com.cq.entity.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectEmployeeRepo extends JpaRepository<ProjectEmployee, Long> {
    List<ProjectEmployee> findByProjectId(Long projectId);
    List<ProjectEmployee> findByEmployeeId(Long employeeId);
}
