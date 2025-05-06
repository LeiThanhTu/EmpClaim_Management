package com.cq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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
    private LocalDate endDate;
    private Long leadEmployeeId;
    private String contactPerson;
    private String contactNo;
    private String budgetNo;
    private String emailId;
    private String status;


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
                ", budgetNo='" + budgetNo + '\'' +
                ", emailId='" + emailId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

