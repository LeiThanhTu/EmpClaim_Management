package com.cq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.cq.reposetory")
public class ClaimManagementProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimManagementProjectApplication.class, args);
		System.out.println("ClaimManagementProjectApplication.class --> Running");
	}

}
