package com.cq.entity;

import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", insertable = false, updatable = false)
    private Integer employeeId;

    @Column(name = "employee_name")
    private String employeeName;
    
     @Column(name = "email")
    private String email;

    @Column(name = "position")
    private String position;
    
    @Column(name = "department")
    private String department;
    
    private String subject;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private String status = "PENDING";

    @Column(name = "created_date")
    private LocalDateTime createdDate;


    @Lob
    private byte[] attachment;
    private String attachmentName;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

 
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        // Don't set employeeId here since it's managed by JPA
        if (employee != null) {
            this.employeeName = employee.getEmployeeName();
            this.email = employee.getEmail();
            this.position = employee.getDesignation();
            this.department = employee.getDepartment();
        }
    }

    public Integer getEmployeeId() {
        if (employeeId != null) {
            return employeeId;
        }
        if (employee != null && employee.getId() != null) {
            return employee.getId();
        }
        return null;
    }
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Request() {
        this.createdDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", employeeName='" + employeeName + '\'' +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
