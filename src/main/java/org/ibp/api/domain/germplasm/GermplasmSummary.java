
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

public class GermplasmSummary {

	private String germplasmId;
	private String pedigreeString;
	private final List<String> names = new ArrayList<String>();
	private String breedingMethod;
	private String location;

	private String parent1Id;
	private String parent1Url;

	private String parent2Id;
	private String parent2Url;

	public String getGermplasmId() {
		return germplasmId;
	}

	public void setGermplasmId(String germplasmId) {
		this.germplasmId = germplasmId;
	}

	public String getPedigreeString() {
		return pedigreeString;
	}

	public void setPedigreeString(String pedigreeString) {
		this.pedigreeString = pedigreeString;
	}

	public String getBreedingMethod() {
		return breedingMethod;
	}

	public void setBreedingMethod(String breedingMethod) {
		this.breedingMethod = breedingMethod;
	}

	public List<String> getNames() {
		return names;
	}

	public void addNames(List<String> names) {
		if (names != null) {
			this.names.addAll(names);
		}
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getParent1Id() {
		return parent1Id;
	}

	public void setParent1Id(String parent1Id) {
		this.parent1Id = parent1Id;
	}

	public String getParent2Id() {
		return parent2Id;
	}

	public void setParent2Id(String parent2Id) {
		this.parent2Id = parent2Id;
	}

	public String getParent1Url() {
		return parent1Url;
	}

	public void setParent1Url(String parent1Url) {
		this.parent1Url = parent1Url;
	}

	public String getParent2Url() {
		return parent2Url;
	}

	public void setParent2Url(String parent2Url) {
		this.parent2Url = parent2Url;
	}

}
