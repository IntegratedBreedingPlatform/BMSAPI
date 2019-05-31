package org.ibp.api.domain.role;

public class RoleDto {

	private int id;

	private String name;

	public RoleDto(int id, String name) {
		super();
		this.id = id;
		this.name = name;
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
