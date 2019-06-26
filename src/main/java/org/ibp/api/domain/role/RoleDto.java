package org.ibp.api.domain.role;

import org.generationcp.middleware.pojos.workbench.Role;

public class RoleDto {

	private int id;

	private String name;

	public RoleDto(final Role role) {
		super();
		this.id = role.getId();
		this.name = role.getName();
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

	public String toString(){
		StringBuffer str= new StringBuffer();
		str.append("RoleDto ")
		.append("[ id= ").append(id)
		.append(" ,description= ").append(name)
		.append(" ]");
		return str.toString();
	}
}
