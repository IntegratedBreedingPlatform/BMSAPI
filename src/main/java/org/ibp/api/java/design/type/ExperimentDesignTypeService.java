package org.ibp.api.java.design.type;

import org.ibp.api.rest.design.ExperimentDesignInput;

public interface ExperimentDesignTypeService {

	Integer MAX_ENTRY_NO = 99999;
	Integer MAX_PLOT_NO = 99999999;

	/**
	 * @param studyId
	 * @param experimentDesignInput
	 * @param programUUID
	 */
	void generateDesign(int studyId, ExperimentDesignInput experimentDesignInput, String programUUID);

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 *
	 * @return
	 */
	Boolean requiresBreedingViewLicence();

	Integer getDesignTypeId();

}
