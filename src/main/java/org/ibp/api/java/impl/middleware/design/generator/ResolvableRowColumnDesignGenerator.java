package org.ibp.api.java.impl.middleware.design.generator;

import org.apache.commons.lang3.BooleanUtils;
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
import java.util.StringTokenizer;

@Component
public class ResolvableRowColumnDesignGenerator extends AbstractExperimentalDesignGenerator implements ExperimentalDesignGenerator {

	@Override
	public MainDesign generate(final ExperimentalDesignInput experimentalDesignInput,
		final Map<BreedingViewVariableParameter, String> variableNameMap,
		final Integer entriesSize, final Integer controlsSize, final Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.SEED.getParameterName(), "", null));
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NTREATMENTS.getParameterName(), String.valueOf(entriesSize), null));
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NREPLICATES.getParameterName(), String.valueOf(experimentalDesignInput.getReplicationsCount()), null));
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NROWS.getParameterName(), String.valueOf(experimentalDesignInput.getRowsPerReplications()), null));
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NCOLUMNS.getParameterName(), String.valueOf(experimentalDesignInput.getColsPerReplications()), null));
		for (final Map.Entry<BreedingViewVariableParameter, String> variableNamePair : variableNameMap.entrySet()) {
			paramList.add(new ExperimentDesignParameter(variableNamePair.getKey().getParameterName(), variableNamePair.getValue(), null));
		}

		this.addInitialTreatmentNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName(),
			this.getPlotNumberStringValueOrDefault(experimentalDesignInput.getStartingPlotNo()), null));

		this.addLatinizeParametersForResolvableRowAndColumnDesign(experimentalDesignInput.getUseLatenized(), paramList,
			experimentalDesignInput.getReplatinGroups(), experimentalDesignInput.getNrlatin(), experimentalDesignInput.getNclatin());

		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.TIMELIMIT.getParameterName(), timeLimit, null));
		paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.OUTPUTFILE.getParameterName(), "", null));

		final ExperimentDesign design = new ExperimentDesign(ExperimentDesignType.ROW_COL.getBvDesignName(), paramList);

		return new MainDesign(design);
	}

	void addLatinizeParametersForResolvableRowAndColumnDesign(
		final Boolean useLatinize, final List<ExperimentDesignParameter> paramList,
		final String replatingGroups, final Integer nrLatin, final Integer ncLatin) {

		if (BooleanUtils.isTrue(useLatinize)) {
			paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NRLATIN.getParameterName(), String.valueOf(nrLatin), null));
			paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NCLATIN.getParameterName(), String.valueOf(ncLatin), null));
			// we tokenize the replating groups
			final StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			final List<ListItem> replatingList = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.REPLATINGROUPS.getParameterName(), null, replatingList));
		} else {
			paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NRLATIN.getParameterName(), "0", null));
			paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.NCLATIN.getParameterName(), "0", null));
		}

	}
}
