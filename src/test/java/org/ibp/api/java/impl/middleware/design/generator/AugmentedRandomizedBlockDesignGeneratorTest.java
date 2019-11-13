package org.ibp.api.java.impl.middleware.design.generator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

public class AugmentedRandomizedBlockDesignGeneratorTest {

	private static final String BLOCK_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String ENTRY_NO = RandomStringUtils.randomAlphabetic(10);

	private final AugmentedRandomizedBlockDesignGenerator experimentDesignGenerator = new AugmentedRandomizedBlockDesignGenerator();

	@Test
	public void testCreateAugmentedRandomizedBlockDesign() {

		final Integer numberOfBlocks = 2;
		final Integer numberOfTreatments = 22;
		final Integer numberOfControls = 11;
		final Integer startingPlotNumber = 1;

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(numberOfBlocks);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(
				experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getPRepVariablesMap(BLOCK_NO, ENTRY_NO, PLOT_NO),
				numberOfTreatments, numberOfControls, null);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getBvDesignName(), experimentDesign.getName());
		Assert.assertEquals(String.valueOf(numberOfTreatments),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NTREATMENTS.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfControls),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NCONTROLS.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfBlocks),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLOCKS.getParameterName()));
		Assert.assertEquals(ENTRY_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.ENTRY.getParameterName()));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.BLOCK.getParameterName()));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.PLOT.getParameterName()));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName()));
		Assert.assertEquals(
			String.valueOf(startingPlotNumber),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName()));
	}

}
