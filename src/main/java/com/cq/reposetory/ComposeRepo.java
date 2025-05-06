package com.cq.reposetory;

import com.cq.entity.Compose;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository  
public interface ComposeRepo extends JpaRepository<Compose, Integer> {
    
    List<Compose> findByParentUkid(Integer parentUkid);

    // Sửa lại query để match với field trong entity
    @Query("SELECT COUNT(c) FROM Compose c WHERE UPPER(c.status) = UPPER(?1)") 
    Long countByStatus(String status);

    // Thêm annotation để xử lý date
    @Query("SELECT c FROM Compose c ORDER BY c.addedDate DESC")
    List<Compose> findTop10ByOrderByAddedDateDesc();
}



