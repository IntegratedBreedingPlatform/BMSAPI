package org.ibp.api.domain.role;

import org.generationcp.middleware.pojos.workbench.Permission;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class PermissionDto {

	private Integer id;

	private String description;

	public PermissionDto() {
	}

	public PermissionDto(final Permission permission) {
		this.id = permission.getPermissionId();
		this.description = permission.getDescription();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
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
