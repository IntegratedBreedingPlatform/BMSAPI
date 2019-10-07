package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class RandomizeCompleteBlockDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final Logger LOG = LoggerFactory.getLogger(RandomizeCompleteBlockDesignTypeServiceImpl.class);

	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId());

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput, final String programUUID, final List<ImportedGermplasm> germplasmList) {

		try {

			this.experimentDesignTypeValidator.validateRandomizedCompleteBlockDesign(experimentDesignInput, germplasmList);

			final StandardVariable replicatesFactor = this.ontologyDataManager.getStandardVariable(TermId.REP_NO.getId(), programUUID);
			final StandardVariable plotNumberFactor = this.ontologyDataManager.getStandardVariable(TermId.PLOT_NO.getId(), programUUID);
			final String block = experimentDesignInput.getReplicationsCount();
			final int environments = Integer.valueOf(experimentDesignInput.getNoOfEnvironments());
			final int environmentsToAdd = Integer.valueOf(experimentDesignInput.getNoOfEnvironmentsToAdd());
			final Integer plotNo = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
			final Integer entryNo = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);

			// TODO: Process treatment factors if available.
			final MainDesign mainDesign = this.experimentDesignGenerator
				.createRandomizedCompleteBlockDesign(block, replicatesFactor.getName(), plotNumberFactor.getName(), plotNo, entryNo,
					null, null,
					null, "");

			// TODO:
			// 1. IBP-3123 Create BVDesign XML input file (e.g.)
			/**
			 * 	final MainDesign mainDesign = this.experimentDesignGenerator
			 * 				.createRandomizedCompleteBlockDesign(block, stdvarRep.getName(), stdvarPlot.getName(), plotNo, entryNo, stdvarTreatment.getName(), treatmentFactors,
			 * 					levels, "");
			 */
			// 2. IBP-3123 Run BV Design and get the design output
			// 3. IBP-3124 Parse the design output and determine the variables / values that will be saved for each plot experiment.
			// 4. IBP-3122 Return list of ObservationUnit rows

		} catch (final Exception e) {
			RandomizeCompleteBlockDesignTypeServiceImpl.LOG.error(e.getMessage(), e);
		}

		return null;
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId();
	}

}
