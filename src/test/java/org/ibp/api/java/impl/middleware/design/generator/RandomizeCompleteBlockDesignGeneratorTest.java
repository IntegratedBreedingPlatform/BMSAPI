package org.ibp.api.java.impl.middleware.design.generator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomizeCompleteBlockDesignGeneratorTest {

	private static final Integer NBLOCK = 2;
	private static final String BLOCK_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String REP_NO = RandomStringUtils.randomAlphabetic(10);

	private final RandomizeCompleteBlockDesignGenerator experimentDesignGenerator = new RandomizeCompleteBlockDesignGenerator();

	@Test
	public void testCreateRandomizedCompleteBlockDesign() {

		final List<String> treatmentFactors = new ArrayList<>(Arrays.asList("FACTOR_1", "FACTOR_2"));
		final List<String> levels = new ArrayList<>(Arrays.asList("Level1", "Level2"));
		final Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap =
			ExperimentalDesignGeneratorTestDataUtil.getTreatmentFactorsParametersMap(treatmentFactors, levels);

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		final Integer initialPlotNumber = 99;
		experimentalDesignInput.setNumberOfBlocks(NBLOCK);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap(BLOCK_NO, PLOT_NO), null, null,
				listItemsMap);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();
		Assert.assertEquals(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getBvDesignName(), experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(BreedingViewDesignParameter.SEED.getParameterName()));
		Assert.assertEquals(String.valueOf(NBLOCK),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLOCKS.getParameterName()));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.BLOCK.getParameterName()));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.PLOT.getParameterName()));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName()));
		Assert.assertEquals(
			treatmentFactors.size(),
			experimentDesign.getParameterList(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName()).size());
		Assert.assertEquals(treatmentFactors.size(),
			experimentDesign.getParameterList(BreedingViewDesignParameter.TREATMENTFACTORS.getParameterName()).size());
		Assert.assertEquals(levels.size(), experimentDesign.getParameterList(BreedingViewDesignParameter.LEVELS.getParameterName()).size());
		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.TIMELIMIT.getParameterName()));
	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberIsNull() {

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = null;

		this.experimentDesignGenerator.addInitialTreatmentNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should not be added to the param list.", 0, paramList.size());

	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberHasValue() {

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = 2;

		this.experimentDesignGenerator.addInitialTreatmentNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should  be added to the param list.", 1, paramList.size());
		Assert.assertEquals(String.valueOf(initialEntryNumber), paramList.get(0).getValue());

	}

	@Test
	public void testGetPlotNumberStringValue() {

		Assert.assertEquals("If the initialPlotNumber is null, it should return the default plot number which is '1'.", "1",
			this.experimentDesignGenerator.getPlotNumberStringValueOrDefault(null));
		Assert.assertEquals("99", this.experimentDesignGenerator.getPlotNumberStringValueOrDefault(99));
	}

}
