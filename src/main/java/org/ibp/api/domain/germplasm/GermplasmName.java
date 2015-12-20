
package org.ibp.api.domain.germplasm;

public class GermplasmName {

	private String name;
	private String nameTypeCode;
	private String nameTypeDescription;

	public GermplasmName() {

	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getNameTypeCode() {
		return nameTypeCode;
	}

	public void setNameTypeCode(String nameTypeCode) {
		this.nameTypeCode = nameTypeCode;
	}

	public String getNameTypeDescription() {
		return nameTypeDescription;
	}

	public void setNameTypeDescription(String nameTypeDescription) {
		this.nameTypeDescription = nameTypeDescription;
	}
}
