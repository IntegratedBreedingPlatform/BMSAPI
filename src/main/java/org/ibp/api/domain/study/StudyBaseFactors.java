package org.ibp.api.domain.study;

import static org.generationcp.middleware.domain.oms.TermId.*;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

/**
 * Enum that encapsulates generation of default study conditions, since virtually all of this conditions are built the same way 
 * @author j-alberto
 *
 */
public enum StudyBaseFactors {
	ENTRY_NUMBER("ENTRY_NO","Germplasm entry - enumerated (number)", ENTRY_NO, ENTRY_NUMBER_STORAGE),
	DESIGNATION("DESIGNATION", "Germplasm designation - assigned (DBCV)", DESIG, ENTRY_DESIGNATION_STORAGE),
	CROSS("CROSS", "The pedigree string of the germplasm", TermId.CROSS, GERMPLASM_ENTRY_STORAGE),
	GID("GID", "Germplasm identifier - assigned (DBID)", TermId.GID, ENTRY_GID_STORAGE),
	TRIAL_INSTANCE("TRIAL_INSTANCE", "Trial Instance", TRIAL_INSTANCE_FACTOR, TRIAL_INSTANCE_STORAGE),
	PLOT_NUMBER("PLOT_NO", "Field plot - enumerated (number)", PLOT_NO, TRIAL_DESIGN_INFO_STORAGE);

	private static final String DBCV = "DBCV";
	private static final String DBID = "DBID";
	private static final String CHAR = "C";
	private static final String NUMERIC = "N";
	private static final String ASSIGNED = "ASSIGNED";
	private static final String ENUMERATED = "ENUMERATED";
	private static final String NUMBER = "NUMBER";

	private String value;
	private String description;
	private TermId term;
	private TermId storageTerm;

	private StudyBaseFactors(String value, String description,TermId term, TermId storageTerm){
		this.value = value;
		this.description = description;
		this.term = term;
		this.storageTerm = storageTerm;
	}

	public String value(){
		return value;
	}
	
	public String description(){
		return description;
	}
	
	public MeasurementVariable asFactor() {

		MeasurementVariable factor;
		
		switch(this){
			case ENTRY_NUMBER:
				factor = createFactor("Germplasm entry", ENUMERATED, NUMBER, NUMERIC, "ENTRY");
			break;
			case DESIGNATION:
				factor = createFactor("Germplasm Designation", ASSIGNED, DBCV, CHAR, "DESIG");
			break;
			case CROSS:
				factor = createFactor("Cross history", ASSIGNED, "PEDIGREE STRING", CHAR, this.value);
			break;
			case GID:
				factor = createFactor("Germplasm id", ASSIGNED, DBID, NUMERIC, this.value);
			break;
			case PLOT_NUMBER:
				factor = createFactor("Field plot", ENUMERATED, NUMBER, NUMERIC, "PLOT");
			break;
			case TRIAL_INSTANCE:
				factor = createFactor("Trial Instance", ASSIGNED, NUMBER, NUMERIC, this.value);
			break;
			default:
				factor = null;
		}

		return factor;
	}
	
	private MeasurementVariable createFactor(String property, String method,
			String scale, String dataType, String label) {

		MeasurementVariable variable = new MeasurementVariable();

		variable.setFactor(true);
		variable.setValue("");
		variable.setTermId(term.getId());
		variable.setName(value);
		variable.setDescription(description);
		variable.setProperty(property);
		variable.setMethod(method);
		variable.setScale(scale);
		variable.setDataType(dataType);
		variable.setLabel(label);
		variable.setStoredIn(storageTerm.getId());

		return variable;
	}


}
