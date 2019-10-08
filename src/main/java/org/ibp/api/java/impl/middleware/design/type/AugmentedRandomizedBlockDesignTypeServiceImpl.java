package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AugmentedRandomizedBlockDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.BLOCK_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId());

	private static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NBLKS.getId());

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	public ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Resource
	public OntologyDataManager ontologyDataManager;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID, final List<ImportedGermplasm> germplasmList) {

		this.experimentDesignTypeValidator.validateAugmentedDesign(experimentDesignInput, germplasmList);

		final Set<Integer> entryIdsOfChecks = this.getEntryIdsOfChecks(germplasmList);
		final Set<Integer> entryIdsOfTestEntries = this.getEntryIdsOfTestEntries(germplasmList);

		final Map<Integer, Integer> designExpectedEntriesMap = this.createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(germplasmList,
			entryIdsOfChecks, entryIdsOfTestEntries);

		final Integer numberOfBlocks = StringUtil.parseInt(experimentDesignInput.getNumberOfBlocks(), null);
		final Integer numberOfControls = entryIdsOfChecks.size();
		final Integer numberOfTreatments = germplasmList.size() - numberOfControls;
		final Integer startingPlotNumber = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
		final Integer startingEntryNumber = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);

		final int noOfExistingEnvironments = Integer.valueOf(experimentDesignInput.getNoOfEnvironments());
		final int noOfEnvironmentsToBeAdded = Integer.valueOf(experimentDesignInput.getNoOfEnvironmentsToAdd());

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String blockNumberName = standardVariablesMap.get(TermId.BLOCK_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createAugmentedRandomizedBlockDesign(numberOfBlocks, numberOfTreatments, numberOfControls, startingPlotNumber,
				startingEntryNumber, entryNumberName, blockNumberName, plotNumberName);

		/**
		 * TODO: return ObservationUnitRows from  this.experimentDesignGenerator.generateExperimentDesignMeasurements
		 final List<MeasurementRow> measurementRowList = experimentDesignGenerator
		 .generateExperimentDesignMeasurements(noOfExistingEnvironments, noOfEnvironmentsToBeAdded, trialVariables, factors, nonTrialFactors,
		 variates, treatmentVariables, requiredDesignVariables, germplasmList, mainDesign, stdvarEntryNo.getName(), null,
		 designExpectedEntriesMap);
		 **/
		return new ArrayList<>();
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId();
	}

	@Override
	public Map<Integer, MeasurementVariable> getMeasurementVariablesMap(final int studyId, final String programUUID) {
		return this.experimentDesignGenerator.getMeasurementVariablesMap(studyId, programUUID, DESIGN_FACTOR_VARIABLES, new ArrayList<>());
	}

	Map<Integer, StandardVariable> convertStandardVariableListToMap(final List<StandardVariable> standardVariables) {

		final Map<Integer, StandardVariable> map = new HashMap<>();

		for (final StandardVariable stdvar : standardVariables) {
			map.put(stdvar.getId(), stdvar);
		}

		return map;

	}

	Map<Integer, Integer> createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(final List<ImportedGermplasm> importedGermplasmList,
		final Set<Integer> entryIdsOfChecks, final Set<Integer> entryIdsOfTestEntries) {

		/**
		 * The design engine assumes that the checks are at the end of the germplasm list that is passed to it. This might or might not be
		 * the case in the list that the user has specified for the trial. To make this simpler for the user, when processing the design
		 * file that comes back from BVDesign, the BMS will re-map the output into entry order.
		 *
		 * In a design with 52 entries (48 test entries and 4 check entries), BVDesign assumes the checks are entry numbers 49,50, 51, and
		 * 52. Since this may not be the case for the user's trial list, the BMS will sequentially map 49-52 to the four check entries in
		 * the list.
		 */

		final Map<Integer, Integer> designExpectedEntriesMap = new HashMap<>();

		// Map the last entries to the check entries in the list.
		int index = importedGermplasmList.size() - entryIdsOfChecks.size();
		for (final Integer checkEntryId : entryIdsOfChecks) {
			designExpectedEntriesMap.put(importedGermplasmList.get(index).getEntryId(), checkEntryId);
			index++;
		}

		// Map the top entries to the test entries in the list.
		index = 0;
		for (final Integer checkEntryId : entryIdsOfTestEntries) {
			designExpectedEntriesMap.put(importedGermplasmList.get(index).getEntryId(), checkEntryId);
			index++;
		}

		return designExpectedEntriesMap;
	}

	Set<Integer> getEntryIdsOfChecks(final List<ImportedGermplasm> importedGermplasmList) {

		final HashSet<Integer> entryIdsOfChecks = new HashSet<>();

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				entryIdsOfChecks.add(importedGermplasm.getEntryId());
			}
		}

		return entryIdsOfChecks;
	}

	Set<Integer> getEntryIdsOfTestEntries(final List<ImportedGermplasm> importedGermplasmList) {

		final HashSet<Integer> entryIdsOfTestEntries = new HashSet<>();

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (!importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				entryIdsOfTestEntries.add(importedGermplasm.getEntryId());
			}
		}

		return entryIdsOfTestEntries;
	}
}
