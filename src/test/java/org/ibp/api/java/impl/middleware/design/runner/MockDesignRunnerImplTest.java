package org.ibp.api.java.impl.middleware.design.runner;

import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.BVDesignTrialInstance;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignGeneratorTestDataUtil;
import org.ibp.api.java.impl.middleware.design.generator.RandomizeCompleteBlockDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ResolvableIncompleteBlockDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ResolvableRowColumnDesignGenerator;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MockDesignRunnerImplTest {

	private final MockDesignRunnerImpl mockDesignRunner = new MockDesignRunnerImpl();

	@Test
	public void testMockDesignRunnerRCBD() {
		final Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap =
			ExperimentalDesignGeneratorTestDataUtil
				.getTreatmentFactorsParametersMap(Collections.singletonList("ENTRY_NO"), Collections.singletonList("20"));

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(2);
		experimentalDesignInput.setStartingPlotNo(200);
		final MainDesign mainDesign = new RandomizeCompleteBlockDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap("REP_NO", "PLOT_NO"),
				null, null, listItemsMap);
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters()
			.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NUMBER_TRIALS.getParameterName(), "2"));

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
		final Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap =
			ExperimentalDesignGeneratorTestDataUtil
				.getTreatmentFactorsParametersMap(Arrays.asList("_8260", "_8261", "ENTRY_NO"), Arrays.asList("3", "2", "20"));

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(2);
		experimentalDesignInput.setStartingPlotNo(200);
		final MainDesign mainDesign = new RandomizeCompleteBlockDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap("REP_NO", "PLOT_NO"), null, null,
				listItemsMap);
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters()
			.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NUMBER_TRIALS.getParameterName(), "2"));

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

		final MainDesign mainDesign = new ResolvableIncompleteBlockDesignGenerator()
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRIBDVariablesMap("BLOCK_NO", "PLOT_NO", "ENTRY_NO", "REP_NO"), 20, null, null);
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters()
			.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NUMBER_TRIALS.getParameterName(), "3"));

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
		final MainDesign mainDesign = new ResolvableRowColumnDesignGenerator()
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRowColVariablesMap("ROW", "COL", "PLOT_NO", "ENTRY_NO", "REP_NO"), 20, null,
				null);
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters()
			.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NUMBER_TRIALS.getParameterName(), "5"));

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
		final MainDesign mainDesign = new RandomizeCompleteBlockDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap("REP_NO", "PLOT_NO"),
				null, null, ExperimentalDesignGeneratorTestDataUtil.getTreatmentFactorsParametersMap(Arrays.asList("_8260", "_8261", "ENTRY_NO"), Arrays.asList("3", "2", "20")));
		final List<List<String>> treatmentFactorValuesList =
			this.mockDesignRunner.getTreatmentFactorValuesCombinations(mainDesign.getDesign());
		Assert.assertEquals(6, treatmentFactorValuesList.size());
	}

}