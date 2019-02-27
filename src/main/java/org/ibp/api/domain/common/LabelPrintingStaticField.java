package org.ibp.api.domain.common;

import java.util.List;
import org.ibp.api.rest.common.FileType;

import java.util.ArrayList;

/**
 * This enum is needed for hardcoded labels in printing label module (those that are not variable in the ontology)
 * Please refer to appconstants.properties in Fieldbook for new static fields, so we keep simple old preset migration
 */
public enum LabelPrintingStaticField {

	YEAR (4),
	STUDY_NAME(6),
	PARENTAGE(13),
	SUB_OBSERVATION_DATASET_OBS_UNIT_ID(25);

	private Integer fieldId;

	LabelPrintingStaticField(final Integer fieldId) {
		this.fieldId = fieldId;
	}

	public Integer getFieldId() {
		return fieldId;
	}

	public void setFieldId(final Integer fieldId) {
		this.fieldId = fieldId;
	}

	public static List<Integer> getAvailableStaticFields() {
		final List<Integer> availableStaticFieldIds = new ArrayList<>();
		for (LabelPrintingStaticField e : LabelPrintingStaticField.values()) {
			availableStaticFieldIds.add(e.getFieldId());
		}
		return availableStaticFieldIds;
	}

}
