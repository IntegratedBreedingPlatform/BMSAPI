package org.ibp.api.brapi.v1.role;

public class RoleDto {

	private int id;

	private String description;

	public RoleDto(int id, String description) {
		super();
		this.id = id;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString(){
		StringBuffer str= new StringBuffer();
		str.append("UserDetails ")
		.append("[ id= ").append(id)
		.append(" ,description= ").append(description)		
		.append(" ]");
		return str.toString();
	}
}
