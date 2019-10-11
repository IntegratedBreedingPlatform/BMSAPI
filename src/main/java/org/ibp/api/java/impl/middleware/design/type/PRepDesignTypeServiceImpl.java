package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PRepDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.BLOCK_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId());

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
		final String programUUID, final List<StudyGermplasmDto> studyGermplasmDtoList) throws BVDesignException {

		this.experimentDesignTypeValidator.validatePrepDesign(experimentDesignInput, studyGermplasmDtoList);

		final int nTreatments = studyGermplasmDtoList.size();
		final int blockSize = Integer.parseInt(experimentDesignInput.getBlockSize());
		final int replicationPercentage = experimentDesignInput.getReplicationPercentage();
		final int replicationNumber = Integer.parseInt(experimentDesignInput.getReplicationsCount());
		final int numberOfTrials = Integer.parseInt(experimentDesignInput.getNoOfEnvironments());

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String blockNumberName = standardVariablesMap.get(TermId.BLOCK_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();

		final Integer plotNo = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), 1);

		final List<ListItem> replicationListItems =
			this.experimentDesignGenerator
				.createReplicationListItemForPRepDesign(studyGermplasmDtoList, replicationPercentage, replicationNumber);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createPRepDesign(blockSize, nTreatments, replicationListItems, entryNumberName,
				blockNumberName, plotNumberName, plotNo);

		final List<MeasurementVariable> measurementVariables = this.getMeasurementVariables(studyId, experimentDesignInput, programUUID);
		return this.experimentDesignGenerator
			.generateExperimentDesignMeasurements(numberOfTrials, measurementVariables, studyGermplasmDtoList, mainDesign,
				entryNumberName, null,
				new HashMap<Integer, Integer>());
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.P_REP.getId();
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID) {
		return this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, programUUID, DESIGN_FACTOR_VARIABLES, EXPERIMENT_DESIGN_VARIABLES,
				experimentDesignInput);
	}
}
