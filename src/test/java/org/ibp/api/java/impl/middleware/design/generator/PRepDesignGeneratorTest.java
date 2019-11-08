package org.ibp.api.java.impl.middleware.design.generator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PRepDesignGeneratorTest {

	private static final String ENTRY_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String BLOCK_NO = RandomStringUtils.randomAlphabetic(10);

	private final PRepDesignGenerator experimentDesignGenerator = new PRepDesignGenerator();

	@Test
	public void testCreatePRepDesign() {

		final Integer numberOfBlocks = 2;
		final Integer numberOfTreatments = 22;
		final Integer startingPlotNumber = 1;
		final List<ListItem> nRepeatsListItem = Collections.singletonList(new ListItem("1"));

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(numberOfBlocks);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);

		final Map<BreedingViewVariableParameter, String> bvVariablesMap = new HashMap<>();
		bvVariablesMap.put(BreedingViewVariableParameter.BLOCK, BLOCK_NO);
		bvVariablesMap.put(BreedingViewVariableParameter.PLOT, PLOT_NO);
		bvVariablesMap.put(BreedingViewVariableParameter.ENTRY, ENTRY_NO);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput, bvVariablesMap,
				numberOfTreatments, null, Collections.singletonMap(BreedingViewDesignParameter.NREPEATS, nRepeatsListItem));

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.TIMELIMIT.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfTreatments),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NTREATMENTS.getParameterName()));
		Assert.assertEquals(String.valueOf(numberOfBlocks),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLOCKS.getParameterName()));
		Assert.assertNull(experimentDesign.getParameterValue(BreedingViewDesignParameter.NREPEATS.getParameterName()));
		Assert.assertSame(nRepeatsListItem, experimentDesign.getParameterList(BreedingViewDesignParameter.NREPEATS.getParameterName()));
		Assert.assertEquals(ENTRY_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.ENTRY.getParameterName()));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.BLOCK.getParameterName()));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(BreedingViewVariableParameter.PLOT.getParameterName()));
		Assert.assertEquals(
			String.valueOf(1),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName()));
		Assert.assertEquals(
			String.valueOf(startingPlotNumber),
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName()));
		Assert.assertEquals(ExperimentDesignType.P_REP.getBvDesignName(), experimentDesign.getName());

	}
}
