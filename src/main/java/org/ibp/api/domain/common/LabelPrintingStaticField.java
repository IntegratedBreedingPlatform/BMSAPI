package org.ibp.api.domain.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This enum is needed for hardcoded labels in printing label module (those that are not variable in the ontology)
 * Please refer to appconstants.properties for new static fields, so we keep simple old preset migration
 */
public enum LabelPrintingStaticField {
	YEAR(4),
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
	USERNAME(33),

	GUID(41),
	METHOD_CODE(50),
	METHOD_NUMBER(51),
	METHOD_GROUP(52),
	GROUP_SOURCE_GID(53),
	GROUP_SOURCE_PREFERRED_NAME(54),
	REFERENCE(48),
	LOTS(57),
	CROSS(58),
	INMEDIATE_SOURCE_GID(62),
	INMEDIATE_SOURCE_PREFERRED_NAME(63);

	private Integer fieldId;
	private static final Map<Integer, LabelPrintingStaticField> STATIC_FIELD_MAP_LOOKUP = new HashMap<>();
	static {
		for (final LabelPrintingStaticField sf : EnumSet.allOf(LabelPrintingStaticField.class)) {
			STATIC_FIELD_MAP_LOOKUP.put(sf.getFieldId(), sf);
		}
	}

	LabelPrintingStaticField(final Integer fieldId) {
		this.fieldId = fieldId;
	}

	public Integer getFieldId() {
		return this.fieldId;
	}

	public void setFieldId(final Integer fieldId) {
		this.fieldId = fieldId;
	}

	public static List<Integer> getAvailableStaticFields() {
		final List<Integer> availableStaticFieldIds = new ArrayList<>();
		for (final LabelPrintingStaticField labelPrintingStaticField : LabelPrintingStaticField.values()) {
			availableStaticFieldIds.add(labelPrintingStaticField.getFieldId());
		}
		return availableStaticFieldIds;
	}

	public static Optional<LabelPrintingStaticField> getByFieldId(final Integer id) {
		if (id == null) {
			return Optional.empty();
		}
		if (LabelPrintingStaticField.STATIC_FIELD_MAP_LOOKUP.containsKey(id)) {
			return Optional.of(STATIC_FIELD_MAP_LOOKUP.get(id));
		}
		return Optional.empty();
	}

}
