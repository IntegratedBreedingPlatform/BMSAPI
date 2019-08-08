package org.ibp.api.domain.role;

import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.service.api.user.UserRoleDto;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AutoProperty
public class RoleDto {

	private int id;

	private String name;

	private String description;

	private RoleTypeDto roleType;

	private List<PermissionDto> permissions;

	private List<UserRoleDto> userRoles = new ArrayList<>();

	private boolean active;

	private boolean editable;

	private boolean assignable;

	public RoleDto(final Role role) {
		super();
		this.id = role.getId();
		this.name = role.getName();
		this.description = role.getDescription();
		this.roleType = new RoleTypeDto(role.getRoleType());
		this.permissions = role.getPermissions().stream()
			.map(permission -> new PermissionDto(permission))
			.collect(Collectors.toList());
		this.active = role.getActive();
		this.editable = role.getEditable();
		this.assignable = role.getAssignable();
		role.getUserRoles().forEach(userRole -> userRoles.add(new UserRoleDto(userRole)));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public RoleTypeDto getRoleType() {
		return roleType;
	}

	public void setRoleType(final RoleTypeDto roleType) {
		this.roleType = roleType;
	}

	public List<PermissionDto> getPermissions() {
		return permissions;
	}

	public void setPermissions(final List<PermissionDto> permissions) {
		this.permissions = permissions;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(final boolean editable) {
		this.editable = editable;
	}

	public boolean isAssignable() {
		return assignable;
	}

	public void setAssignable(final boolean assignable) {
		this.assignable = assignable;
	}

	public List<UserRoleDto> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(final List<UserRoleDto> userRoles) {
		this.userRoles = userRoles;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
