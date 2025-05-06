package com.cq.entity;

import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
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

@Entity
@Table(name = "EMPLOYEE")
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column (name="EMPLOYYE_NAME", length = 100)
	@NotNull
	@Size(min = 3, max = 100, message = "Employee Name must be between 3 to 100 characters.")
	private String employeeName;
	
	@Email(message = "Please provide validate email")
	@NotNull(message = "Email is required")
	@NotBlank(message = "Email is required")
	private String email; 

	@Pattern(regexp = "^(Male | Famale)$", message = "Gender is required.")
	//@NotBlank
	//@NotNull (message = "Gender is required")
	private String gender;

	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth is required. of this format")
	@NotNull(message = "Date of birth is required.")
	@Column(name = "date_of_birth")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
	private String dateOfBirth;

	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Join Date is required. of this format")
	@NotNull(message = "Join date is required.")
	private String joinDate;

	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be start with 6 to 9. and length should be max 10 characters")
	private String mobileNumber;

	@Pattern(regexp = "^\\d{12}$", message = "ID Number must be 12 char")
	private String aadhaarNumb;

	@Pattern(regexp = "^\\d{9,18}$", message = "Account Number must be between 9 to 18 digit")
	private String accountNumber;

	@Column(name = "department")
	@NotNull(message = "Department name is required.")
	private String department;

	
	private String designation;

	@Size(min = 2, max = 100, message = "Name must be between 2 to 100 digit")
	private String previousCompany;

	// @Pattern(regexp = "^\\d{22}", message = "BHXH must be exactly 10 char")
	private String pfNumber;
	
	private String salary;
	
	@Size (max = 1000, min = 10, message = "Curent Address must be between 10 to 1000 char")
	private String currentAddress;
	
	@Size (max = 1000, min = 10, message = "Permanent address must be between 10 to 1000 char")
	private String permanrntAddress;
	
	private boolean active=true;
	
	@CreationTimestamp
	@Column(name = "created_date", updatable = false)
	private LocalDateTime createdDate;
	
	@UpdateTimestamp
	private LocalDateTime updateDate;

	private String password;

	@NotNull(message = "Role is required.")
	@Size(min = 3, max = 10, message = "Role must be between 3 to 10 char")
	private String role;
	
	
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

	
	
	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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
