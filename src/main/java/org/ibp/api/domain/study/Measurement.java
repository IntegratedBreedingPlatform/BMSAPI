
package org.ibp.api.domain.study;

public class Measurement {

	private Integer traitId;
	private String traitName;
	private String value;
	
	public Measurement(Integer traitId, String traitName, String value) {
		this.traitId = traitId;
		this.traitName = traitName;
		this.value = value;
	}

	public Integer getTraitId() {
		return traitId;
	}

	public void setTraitId(Integer traitId) {
		this.traitId = traitId;
	}

	public String getTraitName() {
		return traitName;
	}

	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
