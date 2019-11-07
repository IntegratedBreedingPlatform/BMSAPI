package org.ibp.api.java.impl.middleware.design.generator;

import org.ibp.api.domain.design.ListItem;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExperimentalDesignGeneratorTestDataUtil {

	public static Map<BreedingViewDesignParameter, List<ListItem>> getTreatmentFactorsParametersMap(final List<String> treatmentFactors,
		final List<String> levels) {
		final Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap = new LinkedHashMap<>();
		final List<ListItem> initialTreatmentNumList = new ArrayList<>();
		for (final String treatmentFactor : treatmentFactors) {
			initialTreatmentNumList.add(new ListItem("1"));
		}
		listItemsMap.put(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER, initialTreatmentNumList);
		listItemsMap.put(BreedingViewDesignParameter.TREATMENTFACTORS, ExperimentalDesignUtil.convertToListItemList(treatmentFactors));
		listItemsMap.put(BreedingViewDesignParameter.LEVELS, ExperimentalDesignUtil.convertToListItemList(levels));
		return listItemsMap;
	}

	public static Map<BreedingViewVariableParameter, String> getRCBDVariablesMap(final String block, final String plot) {
		final Map<BreedingViewVariableParameter, String> bvVariablesMap = new LinkedHashMap<>();
		bvVariablesMap.put(BreedingViewVariableParameter.BLOCK, block);
		bvVariablesMap.put(BreedingViewVariableParameter.PLOT, plot);
		return bvVariablesMap;
	}

	public static Map<BreedingViewVariableParameter, String> getRIBDVariablesMap(final String block, final String plot, final String entry,
		final String rep) {
		final Map<BreedingViewVariableParameter, String> bvVariablesMap = new LinkedHashMap<>();
		bvVariablesMap.put(BreedingViewVariableParameter.BLOCK, block);
		bvVariablesMap.put(BreedingViewVariableParameter.PLOT, plot);
		bvVariablesMap.put(BreedingViewVariableParameter.ENTRY, entry);
		bvVariablesMap.put(BreedingViewVariableParameter.REP, rep);
		return bvVariablesMap;
	}

	public static Map<BreedingViewVariableParameter, String> getRowColVariablesMap(final String row, final String column, final String plot,
		final String entry,
		final String rep) {
		final Map<BreedingViewVariableParameter, String> bvVariablesMap = new LinkedHashMap<>();
		bvVariablesMap.put(BreedingViewVariableParameter.ROW, row);
		bvVariablesMap.put(BreedingViewVariableParameter.COLUMN, column);
		bvVariablesMap.put(BreedingViewVariableParameter.PLOT, plot);
		bvVariablesMap.put(BreedingViewVariableParameter.ENTRY, entry);
		bvVariablesMap.put(BreedingViewVariableParameter.REP, rep);

		return bvVariablesMap;
	}

	public static Map<BreedingViewVariableParameter, String> getPRepVariablesMap(final String block,
		final String entry, final String plot) {
		final Map<BreedingViewVariableParameter, String> bvVariablesMap = new LinkedHashMap<>();
		bvVariablesMap.put(BreedingViewVariableParameter.BLOCK, block);
		bvVariablesMap.put(BreedingViewVariableParameter.PLOT, plot);
		bvVariablesMap.put(BreedingViewVariableParameter.ENTRY, entry);

		return bvVariablesMap;
	}

}
