package org.ibp.api.brapi.v1.user;


public class UserDetailDto {

	private Integer userId;

	private String username;

	private String firstName;

	private String lastName;

	private String role;

	private String status;
	
	private String email;

	
	public Integer getUserId() {
		return userId;
	}

	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	
	public String getUsername() {
		return username;
	}

	
	public void setUsername(String username) {
		this.username = username;
	}

	
	public String getFirstName() {
		return firstName;
	}

	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	
	public String getLastName() {
		return lastName;
	}

	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	
	public String getRole() {
		return role;
	}

	
	public void setRole(String role) {
		this.role = role;
	}

	
	public String getStatus() {
		return status;
	}

	
	public void setStatus(String status) {
		this.status = status;
	}

	
	public String getEmail() {
		return email;
	}

	
	public void setEmail(String email) {
		this.email = email;
	}

	
	public String toString(){
		StringBuffer str= new StringBuffer();
		str.append("UserDetails ")
		.append("[ userId= ").append(userId)
		.append(" ,username= ").append(username)
		.append(" ,firstName= ").append(firstName)
		.append(" ,lastName= ").append(lastName)
		.append(" ,role= ").append(role)
		.append(" ,status= ").append(status)
		.append(" ,email= ").append(email)
		.append(" ]");
		return str.toString();
	}
}
