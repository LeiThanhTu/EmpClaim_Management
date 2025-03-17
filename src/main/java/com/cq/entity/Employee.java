package com.cq.entity;

import java.sql.Date;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "EMPLOYEE")
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column (name="EMPLOYYE_NAME", length = 100)
	@NotNull
	private String employeeName;
	
	@Email
	private String email; 
	
	private String gender;
	
	private String dateOfBirth;
	
	private String joinDate;
	
	private String mobileNumber;
	
	private String aadhaarNumb;
	
	private String accountNumber;
	
	private String department;
	
	private String designation;
	
	private String previousCompany;
	
	private String pfNumber;
	
	private String salary;
	
	@Size (max = 1000, min = 10)
	private String currentAddress;
	
	@Size (max = 1000, min = 10)
	private String permanrntAddress;
	
	private boolean active=true;
	
	@CreationTimestamp
	private LocalDateTime createdDate;
	
	@UpdateTimestamp
	private LocalDateTime updateDate;

	private String password;
	
	public Employee() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getAadhaarNumb() {
		return aadhaarNumb;
	}

	public void setAadhaarNumb(String aadhaarNumb) {
		this.aadhaarNumb = aadhaarNumb;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getPreviousCompany() {
		return previousCompany;
	}

	public void setPreviousCompany(String previousCompany) {
		this.previousCompany = previousCompany;
	}

	public String getPfNumber() {
		return pfNumber;
	}

	public void setPfNumber(String pfNumber) {
		this.pfNumber = pfNumber;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	public String getCurrentAddress() {
		return currentAddress;
	}

	public void setCurrentAddress(String currentAddress) {
		this.currentAddress = currentAddress;
	}

	public String getPermanrntAddress() {
		return permanrntAddress;
	}

	public void setPermanrntAddress(String permanrntAddress) {
		this.permanrntAddress = permanrntAddress;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	
//	@CreatedDate
//	@Column(name="create_at", nullable=false, updatable=false)
//	private Date createAt;
//	
//	@LastModifiedDate
//	@Column(name="update_at")
//	private LocalDateTime updateAt;
	
	
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}
	
	

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "Employee [id=" + id + ", employeeName=" + employeeName + ", email=" + email + ", gender=" + gender
				+ ", dateOfBirth=" + dateOfBirth + ", joinDate=" + joinDate + ", mobileNumber=" + mobileNumber
				+ ", aadhaarNumb=" + aadhaarNumb + ", accountNumber=" + accountNumber + ", department=" + department
				+ ", designation=" + designation + ", previousCompany=" + previousCompany + ", pfNumber=" + pfNumber
				+ ", salary=" + salary + ", currentAddress=" + currentAddress + ", permanrntAddress=" + permanrntAddress
				+ ", active=" + active + "]";
	}


	
	
	
}
