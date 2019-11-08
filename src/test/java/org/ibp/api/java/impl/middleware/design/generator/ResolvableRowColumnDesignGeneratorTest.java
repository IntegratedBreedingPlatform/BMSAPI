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

public class ResolvableRowColumnDesignGeneratorTest {

	private static final String ENTRY_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String ROW = RandomStringUtils.randomAlphabetic(10);
	private static final String COL = RandomStringUtils.randomAlphabetic(10);
	private static final String REP_NO = RandomStringUtils.randomAlphabetic(10);

	private final ResolvableRowColumnDesignGenerator experimentDesignGenerator = new ResolvableRowColumnDesignGenerator();

	@Test
	public void testCreateResolvableRowColumnDesign() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer rows = 31;
		final Integer cols = 31;
		final Integer initialPlotNumber = 99;
		final Integer nrLatin = 1;
		final Integer ncLatin = 2;
		final String replatinGroups = "sample1,sample2";

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(numberOfReplicates);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		experimentalDesignInput.setRowsPerReplications(rows);
		experimentalDesignInput.setColsPerReplications(cols);
		experimentalDesignInput.setNrlatin(nrLatin);
		experimentalDesignInput.setNclatin(ncLatin);
		experimentalDesignInput.setReplatinGroups(replatinGroups);
		experimentalDesignInput.setUseLatenized(false);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRowColVariablesMap(ROW, COL, PLOT_NO, ENTRY_NO, REP_NO), numberOfTreatments, null,
				null);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignType.ROW_COL.getBvDesignName(), experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(BreedingViewDesignParameter.SEED.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfTreatments),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NTREATMENTS.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfReplicates),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NREPLICATES.getParameterName()));
		Assert.assertEquals(String.valueOf(rows),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NROWS.getParameterName()));
		Assert.assertEquals(String.valueOf(cols),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NCOLUMNS.getParameterName()));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName()));
		Assert.assertEquals(ROW, experimentDesign.getParameterValue(BreedingViewVariableParameter.ROW.getParameterName()));
		Assert.assertEquals(COL, experimentDesign.getParameterValue(BreedingViewVariableParameter.COLUMN.getParameterName()));
		Assert.assertEquals(REP_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.REP.getParameterName()));
		Assert.assertEquals(ENTRY_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.ENTRY.getParameterName()));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.PLOT.getParameterName()));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName()));

		Assert.assertEquals("0", experimentDesign.getParameterValue(BreedingViewDesignParameter.NRLATIN.getParameterName()));
		Assert.assertEquals("0", experimentDesign.getParameterValue(BreedingViewDesignParameter.NCLATIN.getParameterName()));

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.TIMELIMIT.getParameterName()));
	}

	@Test
	public void testCreateResolvableRowColumnDesignLatinized() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer rows = 31;
		final Integer cols = 31;
		final Integer initialPlotNumber = 99;
		final Integer nrLatin = 1;
		final Integer ncLatin = 2;
		final String replatinGroups = "sample1,sample2";

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(numberOfReplicates);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		experimentalDesignInput.setRowsPerReplications(rows);
		experimentalDesignInput.setColsPerReplications(cols);
		experimentalDesignInput.setNrlatin(nrLatin);
		experimentalDesignInput.setNclatin(ncLatin);
		experimentalDesignInput.setReplatinGroups(replatinGroups);
		experimentalDesignInput.setUseLatenized(true);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRowColVariablesMap(ROW, COL, PLOT_NO, ENTRY_NO, REP_NO), numberOfTreatments, null,
				null);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignType.ROW_COL.getBvDesignName(), experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(BreedingViewDesignParameter.SEED.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfTreatments),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NTREATMENTS.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfReplicates),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NREPLICATES.getParameterName()));
		Assert.assertEquals(String.valueOf(rows),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NROWS.getParameterName()));
		Assert.assertEquals(String.valueOf(cols),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NCOLUMNS.getParameterName()));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName()));
		Assert.assertEquals(ROW, experimentDesign.getParameterValue(BreedingViewVariableParameter.ROW.getParameterName()));
		Assert.assertEquals(COL, experimentDesign.getParameterValue(BreedingViewVariableParameter.COLUMN.getParameterName()));
		Assert.assertEquals(REP_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.REP.getParameterName()));
		Assert.assertEquals(ENTRY_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.ENTRY.getParameterName()));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.PLOT.getParameterName()));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName()));

		// Latinized
		Assert.assertEquals(String.valueOf(nrLatin), experimentDesign.getParameterValue(BreedingViewDesignParameter.NRLATIN.getParameterName()));
		Assert.assertEquals(String.valueOf(ncLatin), experimentDesign.getParameterValue(BreedingViewDesignParameter.NCLATIN.getParameterName()));
		Assert.assertEquals(2, experimentDesign.getParameterList(BreedingViewDesignParameter.REPLATINGROUPS.getParameterName()).size());

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.TIMELIMIT.getParameterName()));
	}

}
