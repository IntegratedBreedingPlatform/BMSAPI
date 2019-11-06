package org.ibp.api.java.impl.middleware.design.runner;

import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.BVDesignTrialInstance;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MockDesignRunnerImplTest {

	private final MockDesignRunnerImpl mockDesignRunner = new MockDesignRunnerImpl();

	private final ExperimentDesignGenerator experimentDesignGenerator = new ExperimentDesignGenerator();

	@Test
	public void testMockDesignRunnerRCBD() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(2);
		experimentalDesignInput.setStartingPlotNo(200);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(experimentalDesignInput, "REP_NO", "PLOT_NO", TermId.ENTRY_NO.name(),
				Collections.singletonList("ENTRY_NO"),
				Collections.singletonList("20"));
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters().add(new ExperimentDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, "2"));

		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(mainDesign);
			Assert.assertTrue(output.isSuccess());
			// Per instance: 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(2, output.getTrialInstances().size());
			for (final BVDesignTrialInstance instance : output.getTrialInstances()) {
				Assert.assertEquals(40, instance.getRows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMockDesignRunnerRCBDWithTreatmentFactors() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(2);
		experimentalDesignInput.setStartingPlotNo(200);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(experimentalDesignInput, "REP_NO", "PLOT_NO", TermId.ENTRY_NO.name(),
				Arrays.asList("_8260", "_8261", "ENTRY_NO"), Arrays.asList("3", "2", "20"));
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters().add(new ExperimentDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, "2"));

		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(mainDesign);
			Assert.assertTrue(output.isSuccess());
			// Per instance: 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(2, output.getTrialInstances().size());
			for (final BVDesignTrialInstance instance : output.getTrialInstances()) {
				Assert.assertEquals(240, instance.getRows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMockDesignRunnerRIBD() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(2);
		experimentalDesignInput.setReplicationsCount(2);
		experimentalDesignInput.setStartingPlotNo(10);
		experimentalDesignInput.setUseLatenized(false);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableIncompleteBlockDesign(experimentalDesignInput, 20, "ENTRY_NO", "REP_NO", "BLOCK_NO", "PLOT_NO");
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters().add(new ExperimentDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, "3"));

		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(mainDesign);
			Assert.assertTrue(output.isSuccess());
			// Per instance: 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(3, output.getTrialInstances().size());
			for (final BVDesignTrialInstance instance : output.getTrialInstances()) {
				Assert.assertEquals(40, instance.getRows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMockDesignRunnerRRCD() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(2);
		experimentalDesignInput.setRowsPerReplications(2);
		experimentalDesignInput.setColsPerReplications(10);
		experimentalDesignInput.setStartingPlotNo(10);
		experimentalDesignInput.setUseLatenized(false);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableRowColDesign(experimentalDesignInput, 20, "ENTRY_NO", "REP_NO", "ROW", "COL", "PLOT_NO");
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters().add(new ExperimentDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, "5"));

		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(mainDesign);
			Assert.assertTrue(output.isSuccess());
			// Per instance: 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(5, output.getTrialInstances().size());
			for (final BVDesignTrialInstance instance : output.getTrialInstances()) {
				Assert.assertEquals(40, instance.getRows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGetTreatmentFactorValuesCombinations() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(2);
		experimentalDesignInput.setStartingPlotNo(200);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(experimentalDesignInput, "REP_NO", "PLOT_NO", TermId.ENTRY_NO.name(),
				Arrays.asList("_8260", "_8261", "ENTRY_NO"), Arrays.asList("3", "2", "20"));
		final List<List<String>> treatmentFactorValuesList =
			this.mockDesignRunner.getTreatmentFactorValuesCombinations(mainDesign.getDesign());
		Assert.assertEquals(6, treatmentFactorValuesList.size());
	}

}