
package org.ibp.api.rest.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.io.Serializable;
import java.util.List;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldmapRequestDto implements Serializable {

	private Integer datasetId;
	private boolean allExistingFieldmapSelected;
	private List<Integer> instanceIds;

	public Integer getDatasetId() {
		return this.datasetId;
	}

	public void setDatasetId(final Integer datasetId) {
		this.datasetId = datasetId;
	}

	public boolean isAllExistingFieldmapSelected() {
		return this.allExistingFieldmapSelected;
	}

	public void setAllExistingFieldmapSelected(final boolean allExistingFieldmapSelected) {
		this.allExistingFieldmapSelected = allExistingFieldmapSelected;
	}

	public List<Integer> getInstanceIds() {
		return this.instanceIds;
	}

	public void setInstanceIds(final List<Integer> instanceIds) {
		this.instanceIds = instanceIds;
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
