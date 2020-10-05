package org.ibp.api.java.design.type;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;

import java.util.List;

public interface ExperimentalDesignTypeService {

	/**
	 * @param studyId
	 * @param experimentalDesignInput
	 * @param programUUID
	 * @param studyEntryDtoList
	 */
	List<ObservationUnitRow> generateDesign(int studyId, ExperimentalDesignInput experimentalDesignInput, String programUUID,
		List<StudyEntryDto> studyEntryDtoList);

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 *
	 * @return
	 */
	Boolean requiresLicenseCheck();

	Integer getDesignTypeId();

	List<MeasurementVariable> getMeasurementVariables(int studyId, ExperimentalDesignInput experimentalDesignInput, String programUUID);

}
