package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

/**
 * Enum that encapsulates generation of default study conditions, since virtually all of this conditions are built the same way 
 * @author j-alberto
 *
 */
public enum StudyConditions {
	BREEDING_METHOD("Breeding Method"),
	INSTITUTE("Institute"),
	STUDY_NAME("Study"),
	STUDY_TITLE("Study Title"),
	START_DATE("Start Date"),
	END_DATE("End Date"),
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

	private StudyConditions(String value){
		this.value = value;
	}

	public String toString(){
		return value;
	}
	
	
	public MeasurementVariable asMeasurementVariable(String initialValue) {

		MeasurementVariable measureVariable = new MeasurementVariable();
		
		switch(this){
			case BREEDING_METHOD:
				measureVariable = createMeasurementVariable(TermId.BREEDING_METHOD_CODE.getId(), "STUDY_BM_CODE",
						"Breeding method applied to all plots in a study (CODE)", this.toString(), APPLIED,
						"BMETH_CODE", CHAR, null, TEXT_STUDY, TermId.STUDY_INFORMATION.getId(), true);
			break;
			case INSTITUTE:
				measureVariable = createMeasurementVariable(8080, "STUDY_INSTITUTE", "Study institute - conducted (DBCV)",
						this.toString(), CONDUCTED, DBCV, CHAR, "CIMMYT",
						TEXT_STUDY, TermId.STUDY_INFORMATION.getId(), true);
			break;
			case STUDY_NAME:
				measureVariable = createMeasurementVariable(TermId.STUDY_NAME.getId(), "STUDY_NAME", "Study - assigned (DBCV)",
						this.toString(), ASSIGNED, DBCV, CHAR, initialValue,
						TEXT_STUDY, TermId.STUDY_NAME_STORAGE.getId(), true);
			break;
			case STUDY_TITLE:
				measureVariable = createMeasurementVariable(TermId.STUDY_TITLE.getId(), "STUDY_TITLE", "Study title - assigned (text)",
						this.toString(), ASSIGNED, SCALE_TEXT, CHAR,
						initialValue, TEXT_STUDY, TermId.STUDY_TITLE_STORAGE.getId(), true);
			break;
			case START_DATE:
				measureVariable = createMeasurementVariable(TermId.START_DATE.getId(), "START_DATE", "Start date - assigned (date)",
						this.toString(), ASSIGNED, DATE, CHAR, initialValue,
						TEXT_STUDY, TermId.STUDY_INFORMATION.getId(), true);
			break;
			case END_DATE:
				measureVariable = createMeasurementVariable(TermId.END_DATE.getId(), "END_DATE", "End date - assigned (date)",
						this.toString(), ASSIGNED, DATE, CHAR,
						initialValue, TEXT_STUDY, TermId.STUDY_INFORMATION.getId(), true);
			break;
			case OBJECTIVE:
				measureVariable = createMeasurementVariable(TermId.STUDY_OBJECTIVE.getId(), "STUDY_OBJECTIVE", "Objective - described (text)",
						this.toString(), DESCRIBED, SCALE_TEXT, CHAR, initialValue,
						TEXT_STUDY, TermId.STUDY_INFORMATION.getId(), true);
			break;
		}


		return measureVariable;
	}
	
	private MeasurementVariable createMeasurementVariable(int termId, String name, String description, String property, String method,
			String scale, String dataType, String value, String label, int storedIn, boolean isFactor) {

		MeasurementVariable variable = new MeasurementVariable();

		variable.setTermId(termId);
		variable.setName(name);
		variable.setDescription(description);
		variable.setProperty(property);
		variable.setMethod(method);
		variable.setScale(scale);
		variable.setDataType(dataType);
		variable.setValue(value);
		variable.setLabel(label);
		variable.setStoredIn(storedIn);
		variable.setFactor(isFactor);

		return variable;
	}


}
