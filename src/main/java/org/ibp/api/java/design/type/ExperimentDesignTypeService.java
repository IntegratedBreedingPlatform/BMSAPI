package org.ibp.api.java.design.type;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignValidationOutput;
import org.ibp.api.rest.design.ExperimentDesignInput;

import java.util.List;

public interface ExperimentDesignTypeService {

	Integer MAX_ENTRY_NO = 99999;
	Integer MAX_PLOT_NO = 99999999;

	/**
	 *
	 * @param studyId
	 * @param experimentDesignInput
	 */
	void generateDesign(int studyId, ExperimentDesignInput experimentDesignInput);

	/**
	 * Validates the design parameters and germplasm list entries.
	 *
	 * @param experimentDesignInput the exp design parameter
	 * @return the exp design validation output
	 */
	ExperimentDesignValidationOutput validate(ExperimentDesignInput experimentDesignInput, List<ImportedGermplasm> germplasmList);

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 *
	 * @return
	 */
	Boolean requiresBreedingViewLicence();

}
