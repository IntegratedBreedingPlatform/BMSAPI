
package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

/**
 * Derived from {@link MetadataSummary} to provide extra information like fields which are editable, record is deletable and variable usage.
 * {@link Usage} only have observations and studies carried out for variable
 */
public class MetadataDetails extends MetadataSummary {

	private final List<String> editableFields = new ArrayList<>();
	private boolean deletable;
	private boolean editable;

	private Usage usage = new Usage();

	public Usage getUsage() {
		return this.usage;
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}

	public List<String> getEditableFields() {
		return this.editableFields;
	}

	public void addEditableField(String editableField) {
		this.editableFields.add(editableField);
	}

	public boolean isDeletable() {
		return this.deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public void setObservations(Integer observations) {
		this.usage.setObservations(observations);
	}

	public void setStudies(Integer studies) {
		this.usage.setStudies(studies);
	}

	public boolean isEditable() {
		return editable;
	}

	public MetadataDetails setEditable(final boolean editable) {
		this.editable = editable;
		return this;
	}

	public Integer getDatasets() {
		return this.usage.getDatasets();
	}

	public void setDatasets(final Integer datasets) {
		this.usage.setDatasets(datasets);
	}

	public Integer getGermplasm() {
		return this.usage.getGermplasm();
	}

	public void setGermplasm(final Integer germplasm) {
		this.usage.setGermplasm(germplasm);
	}

	public Integer getBreedingMethods() {
		return this.usage.getBreedingMethods();
	}

	public void setBreedingMethods(final Integer breedingMethods) {
		this.usage.setBreedingMethods(breedingMethods);
	}
}
