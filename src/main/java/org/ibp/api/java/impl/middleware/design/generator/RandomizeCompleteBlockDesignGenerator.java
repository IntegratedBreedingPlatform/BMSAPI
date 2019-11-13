package org.ibp.api.java.impl.middleware.design.generator;

import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RandomizeCompleteBlockDesignGenerator extends AbstractExperimentalDesignGenerator implements ExperimentalDesignGenerator {

	@Override
	public MainDesign generate(final ExperimentalDesignInput experimentalDesignInput,
		final Map<BreedingViewVariableParameter, String> variableNameMap,
		final Integer entriesSize, final Integer controlsSize, final Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.SEED.getParameterName(), "", null));
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NBLOCKS.getParameterName(), String.valueOf(experimentalDesignInput.getNumberOfBlocks()), null));
		for (final Map.Entry<BreedingViewVariableParameter, String> variableNamePair : variableNameMap.entrySet()) {
			paramList.add(new ExperimentDesignParameter(variableNamePair.getKey().getParameterName(), variableNamePair.getValue(), null));
		}

		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName(),
			this.getPlotNumberStringValueOrDefault(experimentalDesignInput.getStartingPlotNo()), null));

		for (final Map.Entry<BreedingViewDesignParameter, List<ListItem>> listItemParameter : listItemsMap.entrySet()) {
			paramList.add(new ExperimentDesignParameter(listItemParameter.getKey().getParameterName(), null, listItemParameter.getValue()));
		}

		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.TIMELIMIT.getParameterName(), timeLimit, null));
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.OUTPUTFILE.getParameterName(), "", null));

		final ExperimentDesign design = new ExperimentDesign(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getBvDesignName(), paramList);
		return new MainDesign(design);
	}
}
