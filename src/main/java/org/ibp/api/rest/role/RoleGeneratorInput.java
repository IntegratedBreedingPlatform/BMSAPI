package org.ibp.api.rest.role;

import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class RoleGeneratorInput {

	private String name;

	private String description;

	private Integer roleType;

	private List<Integer> permissions;

	private boolean active;

	private boolean editable;

	private boolean assignable;

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Integer getRoleType() {
		return this.roleType;
	}

	public void setRoleType(final Integer roleType) {
		this.roleType = roleType;
	}

	public List<Integer> getPermissions() {
		return this.permissions;
	}

	public void setPermissions(final List<Integer> permissions) {
		this.permissions = permissions;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean isEditable() {
		return this.editable;
	}

	public void setEditable(final boolean editable) {
		this.editable = editable;
	}

	public boolean isAssignable() {
		return this.assignable;
	}

	public void setAssignable(final boolean assignable) {
		this.assignable = assignable;
	}
}
