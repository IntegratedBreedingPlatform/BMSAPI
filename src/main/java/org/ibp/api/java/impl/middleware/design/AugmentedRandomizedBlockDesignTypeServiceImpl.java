package org.ibp.api.java.impl.middleware.design;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.exception.DesignValidationException;
import org.ibp.api.java.design.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignValidationOutput;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignValidator;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AugmentedRandomizedBlockDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NBLKS.getId());

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	public ExperimentDesignValidator experimentDesignValidator;

	@Resource
	public OntologyDataManager ontologyDataManager;

	@Override
	public void generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput) {

		// TODO: Get Germplasm list from DB
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();

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

		// TODO: Find a way to get the ProgramUUID without using ContextUtil
		final StandardVariable stdvarEntryNo = this.ontologyDataManager.getStandardVariable(TermId.ENTRY_NO.getId(), "");
		final StandardVariable stdvarBlock = this.ontologyDataManager.getStandardVariable(TermId.BLOCK_NO.getId(), "");
		final StandardVariable stdvarPlot = this.ontologyDataManager.getStandardVariable(TermId.PLOT_NO.getId(), "");

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
	public ExperimentDesignValidationOutput validate(final ExperimentDesignInput experimentDesignInput,
		final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		ExperimentDesignValidationOutput output = new ExperimentDesignValidationOutput(true, "");
		try {

			this.experimentDesignValidator.validateAugmentedDesign(experimentDesignInput, germplasmList);

		} catch (final DesignValidationException e) {
			output = new ExperimentDesignValidationOutput(false, e.getMessage());
		} catch (final Exception e) {
			output = new ExperimentDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return output;
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
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
