package org.ibp.api.java.design.type;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;

import java.util.List;

public interface ExperimentDesignTypeService {

	Integer MAX_ENTRY_NO = 99999;
	Integer MAX_PLOT_NO = 99999999;

	/**
	 * @param studyId
	 * @param experimentDesignInput
	 * @param programUUID
	 */
	List<ObservationUnitRow> generateDesign(int studyId, ExperimentDesignInput experimentDesignInput, String programUUID,
		List<ImportedGermplasm> germplasmList);

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 *
	 * @return
	 */
	Boolean requiresBreedingViewLicence();

	Integer getDesignTypeId();

}
