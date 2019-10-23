package org.ibp.api.java.impl.middleware.design.runner;

import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.BVDesignTrialInstance;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
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

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(2, "REP_NO", "PLOT_NO", 200, TermId.ENTRY_NO.name(),
				Collections.singletonList("ENTRY_NO"),
				Collections.singletonList("20"),
				"mock-bv-out.csv");
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

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(2, "REP_NO", "PLOT_NO", 200, TermId.ENTRY_NO.name(),
				Arrays.asList("_8260", "_8261", "ENTRY_NO"), Arrays.asList("3", "2", "20"),
				"mock-bv-out.csv");
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

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableIncompleteBlockDesign(2, 20, 2, "ENTRY_NO", "REP_NO", "BLOCK_NO", "PLOT_NO", 10, null, null,
				"mock-bv-out.csv", false);
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

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableRowColDesign(20, 2, 2, 10, "ENTRY_NO", "REP_NO", "ROW", "COL", "PLOT_NO", 10, null, null, "",
				"mock-bv-out.csv", false);
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
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(2, "REP_NO", "PLOT_NO", 200, TermId.ENTRY_NO.name(),
				Arrays.asList("_8260", "_8261", "ENTRY_NO"), Arrays.asList("3", "2", "20"),
				"mock-bv-out.csv");
		final List<List<String>> treatmentFactorValuesList =
			this.mockDesignRunner.getTreatmentFactorValuesCombinations(mainDesign.getDesign());
		Assert.assertEquals(6, treatmentFactorValuesList.size());
	}

}