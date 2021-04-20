package org.ibp.api.domain.user;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.generationcp.middleware.domain.workbench.CropDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserRoleDto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDetailDto implements Serializable, Comparable<UserDto> {

	private static final long serialVersionUID = -1086700590088326865L;

	private Integer id;
	private String username;
	private String firstName;
	private String lastName;
	private List<UserRoleDto> userRoles;
	private String status;
	private String email;
	private Set<CropDto> crops;
	private Set<String> authorities;
	private String selectedCropName;
	private String selectedProgramUUID;

	public Integer getId() {
		return this.id;
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

	public List<UserRoleDto> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(final List<UserRoleDto> userRoles) {
		this.userRoles = userRoles;
	}

	public Set<String> getAuthorities() {
		return this.authorities;
	}

	public void setAuthorities(final Set<String> authorities) {
		this.authorities = authorities;
	}

	public String getSelectedCropName() {
		return this.selectedCropName;
	}

	public void setSelectedCropName(final String selectedCropName) {
		this.selectedCropName = selectedCropName;
	}

	public String getSelectedProgramUUID() {
		return this.selectedProgramUUID;
	}

	public void setSelectedProgramUUID(final String selectedProgramUUID) {
		this.selectedProgramUUID = selectedProgramUUID;
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
		result = prime * result + (this.userRoles == null ? 0 : this.userRoles.hashCode());

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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("username", username)
			.append("firstName", firstName)
			.append("lastName", lastName)
			.append("status", status)
			.append("email", email)
			.append("crops", crops)
			.append("userRoles", userRoles)
			.toString();
	}

	public Set<CropDto> getCrops() {
		return crops;
	}

	public void setCrops(final Set<CropDto> crops) {
		this.crops = crops;
	}
}
