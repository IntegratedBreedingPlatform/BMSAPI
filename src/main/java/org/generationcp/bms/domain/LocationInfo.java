package org.generationcp.bms.domain;

public class LocationInfo {

	private final Integer id;
	private final String name;

	private String label1;
	private String label2;
	private String label3;

	public LocationInfo(Integer locationId, String locationName) {

		this.id = locationId;
		this.name = locationName;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public String getLabel3() {
		return label3;
	}

	public void setLabel3(String label3) {
		this.label3 = label3;
	}

}
