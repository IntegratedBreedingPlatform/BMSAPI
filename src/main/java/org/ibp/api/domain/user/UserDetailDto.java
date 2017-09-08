package org.ibp.api.domain.user;

import java.io.Serializable;

import org.generationcp.middleware.service.api.user.UserDto;

public class UserDetailDto implements Serializable, Comparable<UserDto> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1086700590088326865L;

	private Integer id;

	private String username;

	private String firstName;

	private String lastName;

	private String role;

	private String status;
	
	private String email;

	
	public Integer getId() {
		return id;
	}

	
	public void setId(Integer id) {
		this.id = id;
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

	@Override
	public int compareTo(UserDto o) {
		int compareId = o.getUserId();
		return Integer.valueOf(this.getId()).compareTo(compareId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.username == null ? 0 : this.username.hashCode());
		result = prime * result + (this.firstName == null ? 0 : this.firstName.hashCode());
		result = prime * result + (this.lastName == null ? 0 : this.lastName.hashCode());
		result = prime * result + (this.role == null ? 0 : this.role.hashCode());
		result = prime * result + (this.email == null ? 0 : this.email.hashCode());
		result = prime * result + (this.status == null ? 0 : this.status.hashCode());
		
		result = prime * result + this.id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		UserDetailDto other = (UserDetailDto) obj;
		if (this.id != other.id) {
			return false;
		}
		return true;
	}
	
	public String toString(){
		StringBuilder str= new StringBuilder();
		str.append("UserDetails ")
		.append("[ id= ").append(id)
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
