package com.cq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "projects")
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String clientName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private Integer leadEmployeeId;
    private String contactPerson;
    private String contactNo;

    @Column(name = "budget_allocation")
    private Double budgetNo;
    private String emailId;
    private String status;


  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectEmployee> projectEmployees;


    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", leadEmployeeId=" + leadEmployeeId +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", budgetNo=" + budgetNo +
                ", emailId='" + emailId + '\'' +
                ", status='" + status + '\'' +
                ", projectEmployees=" + projectEmployees +
                '}';
    }
}

