package org.ibp.api.java.impl.middleware.design.generator;

import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.rest.design.ExperimentalDesignInput;

import java.util.List;
import java.util.Map;

public interface ExperimentalDesignGenerator {

	MainDesign generate(ExperimentalDesignInput experimentalDesignInput, Map<BreedingViewVariableParameter, String> variableNameMap,
		Integer entriesSize, Integer controlsSize, Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap);

}
