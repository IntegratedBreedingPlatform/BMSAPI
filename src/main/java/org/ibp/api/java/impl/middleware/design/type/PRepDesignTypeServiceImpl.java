package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.ListItem;
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
public class PRepDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
			TermId.PERCENTAGE_OF_REPLICATION.getId());

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID, final List<ImportedGermplasm> germplasmList) {

		this.experimentDesignTypeValidator.validatePrepDesign(experimentDesignInput, germplasmList);

		final int nTreatments = germplasmList.size();
		final int blockSize = Integer.parseInt(experimentDesignInput.getBlockSize());
		final int replicationPercentage = experimentDesignInput.getReplicationPercentage();
		final int replicationNumber = Integer.parseInt(experimentDesignInput.getReplicationsCount());
		final int environments = Integer.parseInt(experimentDesignInput.getNoOfEnvironments());
		final int environmentsToAdd = Integer.parseInt(experimentDesignInput.getNoOfEnvironmentsToAdd());

		final StandardVariable entryNumberVariable = this.ontologyDataManager.getStandardVariable(TermId.ENTRY_NO.getId(), programUUID);
		final StandardVariable blockNumberVariable = this.ontologyDataManager.getStandardVariable(TermId.BLOCK_NO.getId(), programUUID);
		final StandardVariable plotNumberVariable = this.ontologyDataManager.getStandardVariable(TermId.PLOT_NO.getId(), programUUID);

		final Integer plotNo = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
		Integer entryNo = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);

		if (!Objects.equals(entryNumberVariable.getId(), TermId.ENTRY_NO.getId())) {
			entryNo = null;
		}

		final List<ListItem> replicationListItems =
			this.experimentDesignGenerator
				.createReplicationListItemForPRepDesign(germplasmList, replicationPercentage, replicationNumber);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createPRepDesign(blockSize, nTreatments, replicationListItems, entryNumberVariable.getName(),
				blockNumberVariable.getName(), plotNumberVariable.getName(), plotNo, entryNo);

		/**
		 * TODO: return ObservationUnitRows from  this.experimentDesignGenerator.generateExperimentDesignMeasurements
		 measurementRowList = this.experimentDesignGenerator
		 .generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
		 variates, treatmentVariables, new ArrayList<StandardVariable>(requiredVariablesMap.values()), germplasmList, mainDesign,
		 entryNumberVariable.getName(), null,
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
		return ExperimentDesignType.P_REP.getId();
	}
}
