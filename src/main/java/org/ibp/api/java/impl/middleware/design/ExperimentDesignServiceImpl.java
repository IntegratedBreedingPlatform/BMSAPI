package org.ibp.api.java.impl.middleware.design;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.ExperimentDesignService;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.design.type.ExperimentDesignTypeServiceFactory;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public void generateAndSaveDesign(final String cropName, final int studyId, final ExperimentDesignInput experimentDesignInput)
		throws BVDesignException {

		this.studyValidator.validate(studyId, false);
		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

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
		this.experimentDesignMiddlewareService
			.saveExperimentDesign(cropType, studyId, measurementVariables, this.mapObservationUnitRow(observationUnitRows));
	}

	List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> mapObservationUnitRow(
		final List<ObservationUnitRow> observationUnitRows) {
		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> convertedRows = new ArrayList<>();
		for (final ObservationUnitRow row : observationUnitRows) {
			final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> variables = new HashMap<>();
			final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> environmentVariables = new HashMap<>();
			for (final String data : row.getVariables().keySet()) {
				variables.put(data, observationUnitRowMapper
					.map(row.getVariables().get(data), org.generationcp.middleware.service.api.dataset.ObservationUnitData.class));
			}
			for (final String data : row.getEnvironmentVariables().keySet()) {
				environmentVariables.put(data, observationUnitRowMapper.map(row.getEnvironmentVariables().get(data),
					org.generationcp.middleware.service.api.dataset.ObservationUnitData.class));
			}
			final org.generationcp.middleware.service.api.dataset.ObservationUnitRow convertedRow =
				observationUnitRowMapper.map(row, org.generationcp.middleware.service.api.dataset.ObservationUnitRow.class);
			convertedRow.setVariables(variables);
			convertedRow.setEnvironmentVariables(environmentVariables);
			convertedRows.add(convertedRow);
		}
		return convertedRows;
	}
}
