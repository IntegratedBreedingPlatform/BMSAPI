
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

/**
 * Enum that encapsulates generation of default study conditions, since virtually all of this conditions are built the same way
 *
 * @author j-alberto
 *
 */
public enum StudyConditions {

	BREEDING_METHOD("Breeding Method"), //
	STUDY_INSTITUTE("Institute"), //
	STUDY_NAME("Study"), //
	STUDY_TITLE("Study Title"), //
	START_DATE("Start Date"), //
	END_DATE("End Date"), //
	OBJECTIVE("Study Objective");

	public static final String ASSIGNED = "ASSIGNED";
	public static final String APPLIED = "APPLIED";
	private static final String CONDUCTED = "CONDUCTED";
	private static final String DESCRIBED = "Described";
	private static final String TEXT_STUDY = "STUDY";
	private static final String CHAR = "C";
	private static final String DBCV = "DBCV";
	private static final String SCALE_TEXT = "Text";
	private static final String DATE = "Date (yyyymmdd)";

	private String value;

	private StudyConditions(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	public MeasurementVariable asMeasurementVariable(final String initialValue) {

		MeasurementVariable measureVariable = new MeasurementVariable();

		switch (this) {
			case BREEDING_METHOD:
				measureVariable =
						this.createMeasurementVariable(TermId.BREEDING_METHOD_CODE.getId(), "STUDY_BM_CODE",
								"Breeding method applied to all plots in a study (CODE)", this.toString(), APPLIED, "BMETH_CODE", CHAR,
								null,  true);
				break;
			case STUDY_INSTITUTE:
				measureVariable =
						this.createMeasurementVariable(TermId.STUDY_INSTITUTE.getId(), "STUDY_INSTITUTE",
								"Study institute - conducted (DBCV)", this.toString(), CONDUCTED, DBCV, CHAR, initialValue, true);
				break;
			case STUDY_NAME:
				measureVariable =
						this.createMeasurementVariable(TermId.STUDY_NAME.getId(), "STUDY_NAME", "Study - assigned (DBCV)", this.toString(),
								ASSIGNED, DBCV, CHAR, initialValue, true);
				break;
			default:
				//Adding default block empty as SonarQube report that default block is required in switch: Add a default case to this switch.
		}

		return measureVariable;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String description,
			final String property, final String method, final String scale, final String dataType, final String value, final boolean isFactor) {

		final MeasurementVariable variable = new MeasurementVariable();

		variable.setTermId(termId);
		variable.setName(name);
		variable.setDescription(description);
		variable.setProperty(property);
		variable.setMethod(method);
		variable.setScale(scale);
		variable.setDataType(dataType);
		variable.setValue(value);
		variable.setLabel(PhenotypicType.STUDY.getLabelList().get(0));
		variable.setRole(PhenotypicType.STUDY);
		variable.setFactor(isFactor);

		return variable;
	}

}
