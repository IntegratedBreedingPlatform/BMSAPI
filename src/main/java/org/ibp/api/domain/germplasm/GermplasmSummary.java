
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

public class GermplasmSummary {

	private Integer gid;
	private String cross;
	private final List<String> names = new ArrayList<String>();
	private String breedingMethod;
	private String location;

	public Integer getGid() {
		return gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public String getCross() {
		return cross;
	}

	public void setCross(String cross) {
		this.cross = cross;
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

}
