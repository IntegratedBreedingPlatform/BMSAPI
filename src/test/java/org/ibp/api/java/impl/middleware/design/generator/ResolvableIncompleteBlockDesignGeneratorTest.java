package org.ibp.api.java.impl.middleware.design.generator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

public class ResolvableIncompleteBlockDesignGeneratorTest {

	private static final String ENTRY_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String BLOCK_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String REP_NO = RandomStringUtils.randomAlphabetic(10);

	private final ResolvableIncompleteBlockDesignGenerator experimentDesignGenerator = new ResolvableIncompleteBlockDesignGenerator();

	@Test
	public void testCreateResolvableIncompleteBlockDesign() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer blockSize = 22;
		final Integer initialPlotNumber = 99;
		final Integer nBLatin = 1;
		final String replatinGroups = "sample1,sample2";

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(blockSize);
		experimentalDesignInput.setReplicationsCount(numberOfReplicates);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		experimentalDesignInput.setNblatin(nBLatin);
		experimentalDesignInput.setReplatinGroups(replatinGroups);
		experimentalDesignInput.setUseLatenized(false);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRIBDVariablesMap(BLOCK_NO, PLOT_NO, ENTRY_NO, REP_NO), numberOfTreatments, null,
				null);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getBvDesignName(), experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(BreedingViewDesignParameter.SEED.getParameterName()));
		Assert.assertEquals(String.valueOf(blockSize),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.BLOCKSIZE.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfTreatments),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NTREATMENTS.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfReplicates),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NREPLICATES.getParameterName()));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName()));
		Assert.assertEquals(REP_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.REP.getParameterName()));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.BLOCK.getParameterName()));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.PLOT.getParameterName()));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName()));

		Assert.assertEquals("0", experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLATIN.getParameterName()));

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.TIMELIMIT.getParameterName()));
	}

	@Test
	public void testCreateResolvableIncompleteBlockDesignLatinized() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer blockSize = 22;
		final Integer initialPlotNumber = 99;
		final Integer nBLatin = 1;
		final String replatinGroups = "sample1,sample2";

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(blockSize);
		experimentalDesignInput.setReplicationsCount(numberOfReplicates);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		experimentalDesignInput.setNblatin(nBLatin);
		experimentalDesignInput.setReplatinGroups(replatinGroups);
		experimentalDesignInput.setUseLatenized(true);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRIBDVariablesMap(BLOCK_NO, PLOT_NO, ENTRY_NO, REP_NO), numberOfTreatments, null,
				null);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getBvDesignName(), experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(BreedingViewDesignParameter.SEED.getParameterName()));
		Assert.assertEquals(String.valueOf(blockSize),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.BLOCKSIZE.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfTreatments),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NTREATMENTS.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfReplicates),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NREPLICATES.getParameterName()));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName()));
		Assert.assertEquals(REP_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.REP.getParameterName()));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.BLOCK.getParameterName()));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.PLOT.getParameterName()));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName()));

		// Latinized Parameters
		Assert.assertEquals(String.valueOf(nBLatin),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLATIN.getParameterName()));
		Assert.assertEquals(2, experimentDesign.getParameterList(BreedingViewDesignParameter.REPLATINGROUPS.getParameterName()).size());

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.TIMELIMIT.getParameterName()));
	}

}
