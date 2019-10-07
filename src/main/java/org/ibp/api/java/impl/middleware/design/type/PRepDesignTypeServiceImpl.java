package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PRepDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
			TermId.PERCENTAGE_OF_REPLICATION.getId());

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	private BindingResult errors;

	@Override
	public void generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput, final String programUUID) {

		// TODO: Get Germplasm list from DB
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();
		this.experimentDesignTypeValidator.validatePrepDesign(experimentDesignInput, germplasmList);

		// TODO:
		// 1. IBP-3123 Create BVDesign XML input file (e.g.)
		/**
		 * 	final MainDesign mainDesign = this.experimentDesignGenerator
		 * 				.createRandomizedCompleteBlockDesign(block, stdvarRep.getName(), stdvarPlot.getName(), plotNo, entryNo, stdvarTreatment.getName(), treatmentFactors,
		 * 					levels, "");
		 */
		// 2. IBP-3123 Run BV Design and get the design output
		// 3. IBP-3124 Parse the design output and determine the variables / values that will be saved for each plot experiment.
		// 4. IBP-3124 Directly save the plot experiments based on the design output. Create a service/method at Middleware level.
		// 	  Germplasm factors (GID, ENTRY_NO, etc), Design factors (PLOT_NO, REP_NO, etc) should be saved at their respective tables in the DB.
		//    Treatment factors and checks should also be applied if applicable.
		// 5. Save experimental design variables (check if this is study level or environment level).
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.P_REP.getId();
	}
}
