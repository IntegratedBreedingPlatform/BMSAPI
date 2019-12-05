package org.ibp.api.rest.samplesubmission.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;

@AutoProperty
public class GOBiiSample {

	private String sampleName;

	private String sampleUuid;

	private String sampleNum;

	private String wellRow;

	private String wellCol;

	private String plateName;

	private GOBiiGermplasm germplasm;

	private Map<String, String> properties;

	public String getSampleUuid() {
		return sampleUuid;
	}

	public void setSampleUuid(final String sampleUuid) {
		this.sampleUuid = sampleUuid;
	}

	public String getWellRow() {
		return wellRow;
	}

	public void setWellRow(final String wellRow) {
		this.wellRow = wellRow;
	}

	public String getPlateName() {
		return plateName;
	}

	public void setPlateName(final String plateName) {
		this.plateName = plateName;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(final String sampleName) {
		this.sampleName = sampleName;
	}

	public String getSampleNum() {
		return sampleNum;
	}

	public void setSampleNum(final String sampleNum) {
		this.sampleNum = sampleNum;
	}

	public String getWellCol() {
		return wellCol;
	}

	public void setWellCol(final String wellCol) {
		this.wellCol = wellCol;
	}

	public GOBiiGermplasm getGermplasm() {
		return germplasm;
	}

	public void setGermplasm(final GOBiiGermplasm germplasm) {
		this.germplasm = germplasm;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, String> properties) {
		this.properties = properties;
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
