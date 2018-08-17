package org.ibp.api.brapi.v1.germplasm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"donorAccessionNumber", "donorInstituteCode", "germplasmPUI"})
public class Donor {

	private String donorAccessionNumber;

	private String donorInstituteCode;

	private String germplasmPUI;

	public Donor() {
	}

	public Donor(final String donorAccessionNumber, final String donorInstituteCode, final String germplasmPUI) {
		this.donorAccessionNumber = donorAccessionNumber;
		this.donorInstituteCode = donorInstituteCode;
		this.germplasmPUI = germplasmPUI;
	}

	public String getDonorAccessionNumber() {
		return donorAccessionNumber;
	}

	public void setDonorAccessionNumber(final String donorAccessionNumber) {
		this.donorAccessionNumber = donorAccessionNumber;
	}

	public String getDonorInstituteCode() {
		return donorInstituteCode;
	}

	public void setDonorInstituteCode(final String donorInstituteCode) {
		this.donorInstituteCode = donorInstituteCode;
	}

	public String getGermplasmPUI() {
		return germplasmPUI;
	}

	public void setGermplasmPUI(final String germplasmPUI) {
		this.germplasmPUI = germplasmPUI;
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
