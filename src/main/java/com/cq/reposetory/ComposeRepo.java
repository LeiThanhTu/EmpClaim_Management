package com.cq.reposetory;

import com.cq.entity.Compose;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  
public interface ComposeRepo extends JpaRepository<Compose, Integer>{
	
	public List<Compose> findByParentUkid(Integer parentUkid);
	
}



