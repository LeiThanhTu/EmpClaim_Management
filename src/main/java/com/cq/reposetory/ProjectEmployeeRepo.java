package com.cq.reposetory;

import com.cq.entity.Employee;
import com.cq.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Long>{


}
