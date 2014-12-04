
package org.generationcp.bms.domain;

public class StandardVariableBasicInfo {

	private int id;
	private String name;
	private String property;
	private String method;
	private String scale;
	private String dataType;
	private String role;
	private String traitClass;
	
	private String detailsUrl;

	public StandardVariableBasicInfo(int id, String name, String property, String method, String scale) {
		this.id = id;
		this.name = name;
		this.property = property;
		this.method = method;
		this.scale = scale;
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

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getRole() {
		return role;
	}

	public String getTraitClass() {
		return traitClass;
	}

	
	public void setTraitClass(String traitClass) {
		this.traitClass = traitClass;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}

	
	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}

}
