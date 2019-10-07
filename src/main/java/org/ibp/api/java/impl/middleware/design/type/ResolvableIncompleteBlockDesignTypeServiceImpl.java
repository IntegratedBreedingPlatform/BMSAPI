package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class ResolvableIncompleteBlockDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES_LATINIZED =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
			TermId.NO_OF_CBLKS_LATINIZE.getId(), TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());
	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId());

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID, final List<ImportedGermplasm> germplasmList) {

		this.experimentDesignTypeValidator.validateResolvableIncompleteBlockDesign(experimentDesignInput, germplasmList);

		final int nTreatments = germplasmList.size();
		final String blockSize = experimentDesignInput.getBlockSize();
		final String replicates = experimentDesignInput.getReplicationsCount();
		final int environments = Integer.valueOf(experimentDesignInput.getNoOfEnvironments());
		final int environmentsToAdd = Integer.valueOf(experimentDesignInput.getNoOfEnvironmentsToAdd());

		final StandardVariable entryNumberVariable = this.ontologyDataManager.getStandardVariable(TermId.ENTRY_NO.getId(), programUUID);
		final StandardVariable replicateNumberVariable = this.ontologyDataManager.getStandardVariable(TermId.REP_NO.getId(), programUUID);
		final StandardVariable blockNumberVariable = this.ontologyDataManager.getStandardVariable(TermId.BLOCK_NO.getId(), programUUID);
		final StandardVariable plotNumberVariable = this.ontologyDataManager.getStandardVariable(TermId.PLOT_NO.getId(), programUUID);

		if (experimentDesignInput.getUseLatenized() != null && experimentDesignInput.getUseLatenized().booleanValue()) {
			if (experimentDesignInput.getReplicationsArrangement() != null) {
				if (experimentDesignInput.getReplicationsArrangement().intValue() == 1) {
					// column
					experimentDesignInput.setReplatinGroups(experimentDesignInput.getReplicationsCount());
				} else if (experimentDesignInput.getReplicationsArrangement().intValue() == 2) {
					// rows
					final StringBuilder rowReplatingGroupStringBuilder = new StringBuilder();
					for (int i = 0; i < Integer.parseInt(experimentDesignInput.getReplicationsCount()); i++) {
						if (StringUtils.isEmpty(rowReplatingGroupStringBuilder.toString())) {
							rowReplatingGroupStringBuilder.append(",");
						}
						rowReplatingGroupStringBuilder.append("1");
					}
					experimentDesignInput.setReplatinGroups(rowReplatingGroupStringBuilder.toString());
				}
			}
		}

		final Integer plotNo = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
		Integer entryNo = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);

		if (!Objects.equals(entryNumberVariable.getId(), TermId.ENTRY_NO.getId())) {
			entryNo = null;
		}

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableIncompleteBlockDesign(blockSize, Integer.toString(nTreatments), replicates, entryNumberVariable.getName(),
				replicateNumberVariable.getName(), blockNumberVariable.getName(), plotNumberVariable.getName(), plotNo, entryNo,
				experimentDesignInput.getNblatin(),
				experimentDesignInput.getReplatinGroups(), "", experimentDesignInput.getUseLatenized());

		/**
		 * TODO: return ObservationUnitRows from  this.experimentDesignGenerator.generateExperimentDesignMeasurements
		 measurementRowList = this.experimentDesignGenerator
		 .generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
		 variates, treatmentVariables, reqVarList, germplasmList, mainDesign, entryNumberVariable.getName(), null,
		 new HashMap<Integer, Integer>());
		 **/

		return new ArrayList<>();
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId();
	}
}
