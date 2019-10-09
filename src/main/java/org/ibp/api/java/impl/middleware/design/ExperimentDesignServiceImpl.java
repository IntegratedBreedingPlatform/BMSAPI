package org.ibp.api.java.impl.middleware.design;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.ExperimentDesignService;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.design.type.ExperimentDesignTypeServiceFactory;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExperimentDesignServiceImpl implements ExperimentDesignService {

	@Resource
	private StudyValidator studyValidator;

	@Autowired
	private StudyService studyService;

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Resource
	private org.generationcp.middleware.service.api.study.generation.ExperimentDesignService experimentDesignMiddlewareService;

	@Resource
	private ExperimentDesignTypeServiceFactory experimentDesignTypeServiceFactory;

	@Override
	public void generateAndSaveDesign(final int studyId, final ExperimentDesignInput experimentDesignInput) throws BVDesignException {

		this.studyValidator.validate(studyId, false);

		final String programUUID = this.studyService.getProgramUUID(studyId);
		final List<StudyGermplasmDto> studyGermplasmDtoList = this.middlewareStudyService.getStudyGermplasmList(studyId);

		final ExperimentDesignTypeService experimentDesignTypeService =
			this.experimentDesignTypeServiceFactory.lookup(experimentDesignInput.getDesignType());

		final List<ObservationUnitRow> observationUnitRows =
			experimentDesignTypeService.generateDesign(studyId, experimentDesignInput, programUUID, studyGermplasmDtoList);
		final List<MeasurementVariable> measurementVariables =
			experimentDesignTypeService.getMeasurementVariablesMap(studyId, programUUID).values().stream().collect(
				Collectors.toList());

		this.experimentDesignMiddlewareService.deleteExperimentDesign(studyId);
		this.experimentDesignMiddlewareService.saveExperimentDesign(null, studyId, measurementVariables, this.convert(observationUnitRows));
	}

	List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> convert(final List<ObservationUnitRow> observationUnitRows) {
		// TODO: Convert dto to middleware pojo version ?
		return new ArrayList<>();
	}
}
