
package org.ibp.api.domain.program;

import java.util.HashSet;
import java.util.Set;

/**
 * Summary information about breeding program.
 *
 */
public class ProgramSummary {

	private String id;
	private String uniqueID;
	private String name;
	private String createdBy;
	private Set<String> members = new HashSet<>();
	private String crop;
	private String startDate;

	public ProgramSummary() {
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUniqueID() {
		return this.uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Set<String> getMembers() {
		return this.members;
	}

	public void setMembers(Set<String> members) {
		this.members = members;
	}

	public String getCrop() {
		return this.crop;
	}

	public void setCrop(String crop) {
		this.crop = crop;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

}
