package org.ibp.api.domain.common;

import java.util.List;
import org.ibp.api.rest.common.FileType;

import java.util.ArrayList;

/**
 * This enum is needed for hardcoded labels in printing label module (those that are not variable in the ontology)
 * Please refer to appconstants.properties for new static fields, so we keep simple old preset migration
 */
public enum LabelPrintingStaticField {

	YEAR (4),
	STUDY_NAME(6),
	PARENTAGE(13),
	SUB_OBSERVATION_DATASET_OBS_UNIT_ID(25),
	LOT_ID(22),
	LOT_UID(1),
	STOCK_ID(2),
	AVAILABLE_BALANCE(26),
	UNITS(5),
	STORAGE_LOCATION_ABBR(27),
	STORAGE_LOCATION(34),
	LOT_NOTES(11),
	TRN_ID(28),
	STATUS(29),
	TYPE(30),
	CREATED(31),
	TRN_NOTES(32),
	USERNAME(33);

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
