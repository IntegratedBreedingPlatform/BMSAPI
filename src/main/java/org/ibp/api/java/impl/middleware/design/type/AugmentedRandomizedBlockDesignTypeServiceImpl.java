package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.generator.AugmentedRandomizedBlockDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignProcessor;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AugmentedRandomizedBlockDesignTypeServiceImpl implements ExperimentalDesignTypeService {

	protected static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.BLOCK_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.OBS_UNIT_ID.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NBLKS.getId());

	@Resource
	public AugmentedRandomizedBlockDesignGenerator experimentDesignGenerator;

	@Resource
	public MeasurementVariableGenerator measurementVariableGenerator;

	@Resource
	private ExperimentalDesignProcessor experimentalDesignProcessor;

	@Resource
	public OntologyDataManager ontologyDataManager;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID, final List<StudyEntryDto> studyEntryDtoList) {

		// Generate experiment design parameters input to design runner
		final Set<Integer> entryIdsOfChecks = this.getEntryIdsOfChecks(studyEntryDtoList);
		final Integer numberOfControls = entryIdsOfChecks.size();
		final Integer numberOfTreatments = studyEntryDtoList.size() - numberOfControls;

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput, this.getBreedingViewVariablesMap(standardVariablesMap), numberOfTreatments, numberOfControls, null);

		// Generate observation unit rows
		final Set<Integer> entryIdsOfTestEntries = this.getEntryIdsOfTestEntries(studyEntryDtoList);
		final Map<Integer, Integer> designExpectedEntriesMap =
			this.createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(studyEntryDtoList,
				entryIdsOfChecks, entryIdsOfTestEntries);
		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final List<MeasurementVariable> measurementVariables = this.getMeasurementVariables(studyId, experimentalDesignInput, programUUID);
		return this.experimentalDesignProcessor
			.generateObservationUnitRows(experimentalDesignInput.getTrialInstancesForDesignGeneration(), measurementVariables,
					studyEntryDtoList, mainDesign, entryNumberName, null,
				designExpectedEntriesMap);
	}

	private Map<BreedingViewVariableParameter, String> getBreedingViewVariablesMap(final Map<Integer, StandardVariable> standardVariablesMap) {
		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String blockNumberName = standardVariablesMap.get(TermId.BLOCK_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();

		final Map<BreedingViewVariableParameter, String> map = new HashMap<>();
		map.put(BreedingViewVariableParameter.ENTRY, entryNumberName);
		map.put(BreedingViewVariableParameter.BLOCK, blockNumberName);
		map.put(BreedingViewVariableParameter.PLOT, plotNumberName);
		return map;
	}

	@Override
	public Boolean requiresLicenseCheck() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId();
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID) {
		return this.measurementVariableGenerator.generateFromExperimentalDesignInput(studyId, programUUID, DESIGN_FACTOR_VARIABLES,
				EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput);
	}

	Map<Integer, StandardVariable> convertStandardVariableListToMap(final List<StandardVariable> standardVariables) {

		final Map<Integer, StandardVariable> map = new HashMap<>();

		for (final StandardVariable stdvar : standardVariables) {
			map.put(stdvar.getId(), stdvar);
		}

		return map;

	}

	Map<Integer, Integer> createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(final List<StudyEntryDto> studyEntryDtoList,
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
		int index = studyEntryDtoList.size() - entryIdsOfChecks.size();
		for (final Integer checkEntryId : entryIdsOfChecks) {
			designExpectedEntriesMap.put(studyEntryDtoList.get(index).getEntryNumber(), checkEntryId);
			index++;
		}

		// Map the top entries to the test entries in the list.
		index = 0;
		for (final Integer checkEntryId : entryIdsOfTestEntries) {
			designExpectedEntriesMap.put(studyEntryDtoList.get(index).getEntryNumber(), checkEntryId);
			index++;
		}

		return designExpectedEntriesMap;
	}

	Set<Integer> getEntryIdsOfChecks(final List<StudyEntryDto> studyEntryDtoList) {

		final HashSet<Integer> entryIdsOfChecks = new HashSet<>();

		for (final StudyEntryDto studyEntryDto : studyEntryDtoList) {
			final Optional<String> entryType = studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId());
			if (entryType.isPresent() && SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() != Integer.valueOf(entryType.get())) {
				entryIdsOfChecks.add(studyEntryDto.getEntryNumber());
			}
		}

		return entryIdsOfChecks;
	}

	Set<Integer> getEntryIdsOfTestEntries(final List<StudyEntryDto> studyEntryDtoList) {

		final HashSet<Integer> entryIdsOfTestEntries = new HashSet<>();


		for (final StudyEntryDto studyEntryDto : studyEntryDtoList) {
			final Optional<String> entryType = studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId());
			if (entryType.isPresent() && SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() == Integer.valueOf(entryType.get())) {
				entryIdsOfTestEntries.add(studyEntryDto.getEntryNumber());
			}
		}

		return entryIdsOfTestEntries;
	}
}
