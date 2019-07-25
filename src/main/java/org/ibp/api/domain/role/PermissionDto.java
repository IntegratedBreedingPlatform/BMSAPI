package org.ibp.api.domain.role;

import org.generationcp.middleware.pojos.workbench.Permission;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class PermissionDto {

	private Integer id;

	private String description;

	private String name;

	private PermissionDto parent;

	public PermissionDto() {
	}

	public PermissionDto(final Permission permission) {
		this.id = permission.getPermissionId();
		this.description = permission.getDescription();
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public PermissionDto getParent() {
		return this.parent;
	}

	public void setParent(final PermissionDto parent) {
		this.parent = parent;
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
