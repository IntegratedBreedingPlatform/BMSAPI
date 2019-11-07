
package org.ibp.api.domain.inventory;

import io.swagger.annotations.ApiModel;

@ApiModel("Location Information")
public class InventoryLocation {

	private Integer id;
	private String name;

	private String label1;
	private String label2;
	private String label3;

	public InventoryLocation() {
	}

	public InventoryLocation(Integer locationId, String locationName) {

		this.id = locationId;
		this.name = locationName;
	}

	public Integer getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel1() {
		return this.label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return this.label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public String getLabel3() {
		return this.label3;
	}

	public void setLabel3(String label3) {
		this.label3 = label3;
	}

}
