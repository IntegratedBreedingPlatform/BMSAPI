package org.ibp.api.brapi.v1.search;

import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class GermplasmSearchRequestDto extends SearchRequestDto {

	private List<String> accessionNumbers;
	private List<String> commonCropNames;
	private List<String> germplasmDbIds;
	private List<String> germplasmGenus;
	private List<String> germplasmNames;
	private List<String> germplasmPUIs;
	private List<String> germplasmSpecies;

	public GermplasmSearchRequestDto() {
	}

	public List<String> getAccessionNumbers() {
		return this.accessionNumbers;
	}

	public void setAccessionNumbers(final List<String> accessionNumbers) {
		this.accessionNumbers = accessionNumbers;
	}

	public List<String> getCommonCropNames() {
		return this.commonCropNames;
	}

	public void setCommonCropNames(final List<String> commonCropNames) {
		this.commonCropNames = commonCropNames;
	}

	public List<String> getGermplasmDbIds() {
		return this.germplasmDbIds;
	}

	public void setGermplasmDbIds(final List<String> germplasmDbIds) {
		this.germplasmDbIds = germplasmDbIds;
	}

	public List<String> getGermplasmGenus() {
		return this.germplasmGenus;
	}

	public void setGermplasmGenus(final List<String> germplasmGenus) {
		this.germplasmGenus = germplasmGenus;
	}

	public List<String> getGermplasmNames() {
		return this.germplasmNames;
	}

	public void setGermplasmNames(final List<String> germplasmNames) {
		this.germplasmNames = germplasmNames;
	}

	public List<String> getGermplasmPUIs() {
		return this.germplasmPUIs;
	}

	public void setGermplasmPUIs(final List<String> germplasmPUIs) {
		this.germplasmPUIs = germplasmPUIs;
	}

	public List<String> getGermplasmSpecies() {
		return this.germplasmSpecies;
	}

	public void setGermplasmSpecies(final List<String> germplasmSpecies) {
		this.germplasmSpecies = germplasmSpecies;
	}
}
