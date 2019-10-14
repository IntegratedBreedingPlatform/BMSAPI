package org.ibp.api.java.design.type;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.exception.BVDesignException;
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
		List<StudyGermplasmDto> studyGermplasmDtoList);

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 *
	 * @return
	 */
	Boolean requiresLicenseCheck();

	Integer getDesignTypeId();

	List<MeasurementVariable> getMeasurementVariables(int studyId, ExperimentDesignInput experimentDesignInput, String programUUID);

}
