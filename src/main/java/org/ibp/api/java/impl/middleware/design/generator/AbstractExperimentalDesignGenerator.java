package org.ibp.api.java.impl.middleware.design.generator;

import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;

import java.util.List;

public abstract class AbstractExperimentalDesignGenerator {

	public static final int STARTING_ENTRY_NUMBER = 1;

	String getPlotNumberStringValueOrDefault(final Integer initialPlotNumber) {
		return (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);
	}

	void addInitialTreatmentNumberIfAvailable(final Integer initialEntryNumber, final List<ExperimentDesignParameter> paramList) {

		if (initialEntryNumber != null) {
			paramList.add(new ExperimentDesignParameter(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName(),
				String.valueOf(initialEntryNumber), null));
		}

	}
}
