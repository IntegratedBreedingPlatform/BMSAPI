
package org.ibp.api.java.impl.middleware.study;

import static org.generationcp.middleware.domain.oms.TermId.DESIG;
import static org.generationcp.middleware.domain.oms.TermId.ENTRY_DESIGNATION_STORAGE;
import static org.generationcp.middleware.domain.oms.TermId.ENTRY_GID_STORAGE;
import static org.generationcp.middleware.domain.oms.TermId.ENTRY_NO;
import static org.generationcp.middleware.domain.oms.TermId.ENTRY_NUMBER_STORAGE;
import static org.generationcp.middleware.domain.oms.TermId.EXPERIMENT_DESIGN_FACTOR;
import static org.generationcp.middleware.domain.oms.TermId.GERMPLASM_ENTRY_STORAGE;
import static org.generationcp.middleware.domain.oms.TermId.NUMBER_OF_REPLICATES;
import static org.generationcp.middleware.domain.oms.TermId.PLOT_NO;
import static org.generationcp.middleware.domain.oms.TermId.REP_NO;
import static org.generationcp.middleware.domain.oms.TermId.TRIAL_DESIGN_INFO_STORAGE;
import static org.generationcp.middleware.domain.oms.TermId.TRIAL_ENVIRONMENT_INFO_STORAGE;
import static org.generationcp.middleware.domain.oms.TermId.TRIAL_INSTANCE_FACTOR;
import static org.generationcp.middleware.domain.oms.TermId.TRIAL_INSTANCE_STORAGE;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

/**
 * Enum that encapsulates generation of default study conditions, since virtually all of this conditions are built the same way
 *
 * @author j-alberto
 *
 */
public enum StudyBaseFactors {

	ENTRY_NUMBER("ENTRY_NO", "Germplasm entry - enumerated (number)", ENTRY_NO, ENTRY_NUMBER_STORAGE), //
	DESIGNATION("DESIGNATION", "Germplasm designation - assigned (DBCV)", DESIG, ENTRY_DESIGNATION_STORAGE), //
	CROSS("CROSS", "The pedigree string of the germplasm", TermId.CROSS, GERMPLASM_ENTRY_STORAGE), //
	GID("GID", "Germplasm identifier - assigned (DBID)", TermId.GID, ENTRY_GID_STORAGE), //
	TRIAL_INSTANCE("TRIAL_INSTANCE", "Trial Instance", TRIAL_INSTANCE_FACTOR, TRIAL_INSTANCE_STORAGE), //
	PLOT_NUMBER("PLOT_NO", "Field plot - enumerated (number)", PLOT_NO, TRIAL_DESIGN_INFO_STORAGE), //
	REPLICATION_NO("REP_NO", "Replication - assigned (number)", REP_NO, TRIAL_DESIGN_INFO_STORAGE), //
	NREP("NREP", "Number of replications in an experiment", NUMBER_OF_REPLICATES, TRIAL_ENVIRONMENT_INFO_STORAGE), //
	EXPT_DESIGN("EXPT_DESIGN", "Experimental design - assigned (type)", EXPERIMENT_DESIGN_FACTOR, TRIAL_ENVIRONMENT_INFO_STORAGE);

	private static final String DBCV = "DBCV";
	private static final String DBID = "DBID";
	private static final String CHAR = "C";
	private static final String NUMERIC = "N";
	private static final String ASSIGNED = "ASSIGNED";
	private static final String ENUMERATED = "ENUMERATED";
	private static final String NUMBER = "NUMBER";
	private static final String CATEGORICAL = "Categorical";

	private String name;
	private String description;
	private TermId term;

	private StudyBaseFactors(final String name, final String description, final TermId term, final TermId storageTerm) {
		this.name = name;
		this.description = description;
		this.term = term;
	}

	public String getName() {
		return this.name;
	}

	public String description() {
		return this.description;
	}

	public MeasurementVariable asFactor() {

		MeasurementVariable factor;

		switch (this) {
			case ENTRY_NUMBER:
				factor =
						this.createFactor("Germplasm entry", StudyBaseFactors.ENUMERATED, StudyBaseFactors.NUMBER,
								StudyBaseFactors.NUMERIC, PhenotypicType.GERMPLASM);
				break;
			case DESIGNATION:
				factor =
						this.createFactor("Germplasm Designation", StudyBaseFactors.ASSIGNED, StudyBaseFactors.DBCV, StudyBaseFactors.CHAR,
						PhenotypicType.GERMPLASM);
				break;
			case CROSS:
				factor =
						this.createFactor("Cross history", StudyBaseFactors.ASSIGNED, "PEDIGREE STRING", StudyBaseFactors.CHAR,
						PhenotypicType.GERMPLASM);
				break;
			case GID:
				factor =
						this.createFactor("Germplasm id", StudyBaseFactors.ASSIGNED, StudyBaseFactors.DBID, StudyBaseFactors.NUMERIC,
								PhenotypicType.GERMPLASM);
				break;
			case PLOT_NUMBER:
				factor =
						this.createFactor("Field plot", StudyBaseFactors.ENUMERATED, StudyBaseFactors.NUMBER, StudyBaseFactors.NUMERIC,
								PhenotypicType.TRIAL_DESIGN);
				break;
			case TRIAL_INSTANCE:
				factor =
						this.createFactor("Trial Instance", StudyBaseFactors.ASSIGNED, StudyBaseFactors.NUMBER, StudyBaseFactors.NUMERIC,
								PhenotypicType.TRIAL_ENVIRONMENT);
				break;

			case REPLICATION_NO:
				factor =
						this.createFactor("Replication factor", StudyBaseFactors.ENUMERATED, StudyBaseFactors.NUMERIC,
								StudyBaseFactors.NUMERIC, PhenotypicType.TRIAL_DESIGN);
				break;

			case NREP:
				factor =
						this.createFactor("ED - nrep", StudyBaseFactors.ASSIGNED, StudyBaseFactors.NUMBER, StudyBaseFactors.NUMERIC,
								PhenotypicType.TRIAL_ENVIRONMENT);
				break;
			case EXPT_DESIGN:
				factor =
						this.createFactor("Experimental design", StudyBaseFactors.ASSIGNED, "Type of EXPT_DESIGN",
						StudyBaseFactors.CATEGORICAL, PhenotypicType.TRIAL_ENVIRONMENT);
				break;
			default:
				factor = null;
		}

		return factor;
	}

	private MeasurementVariable createFactor(final String property, final String method, final String scale, final String dataType,
			final PhenotypicType role) {

		final MeasurementVariable variable = new MeasurementVariable();

		variable.setFactor(true);
		variable.setValue("");
		variable.setTermId(this.term.getId());
		variable.setName(this.name);
		variable.setDescription(this.description);
		variable.setProperty(property);
		variable.setMethod(method);
		variable.setScale(scale);
		variable.setDataType(dataType);
		variable.setLabel(role.getLabelList().get(0));
		variable.setRole(role);

		return variable;
	}

}
