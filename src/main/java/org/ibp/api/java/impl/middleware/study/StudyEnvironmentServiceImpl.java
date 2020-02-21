package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.dms.EnvironmentData;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.study.StudyEnvironmentService;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyEnvironmentServiceImpl implements StudyEnvironmentService {

	@Resource
	private org.generationcp.middleware.service.api.study.StudyEnvironmentService middlewareStudyEnvironmentService;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private InstanceValidator instanceValidator;

	@Resource
	private DatasetValidator datasetValidator;

	@Autowired
	private ObservationValidator observationValidator;

	@Override
	public List<StudyInstance> createStudyEnvironments(final String cropName, final int studyId, final Integer numberOfEnvironmentsToGenerate) {
		if (numberOfEnvironmentsToGenerate < 1 || numberOfEnvironmentsToGenerate > 999) {
			throw new ApiRuntimeException("Invalid number of environments to generate. Please specify number between 1 to 999.");
		}
		this.studyValidator.validate(studyId, true);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);
		final Integer datasetId = this.getEnvironmentDatasetId(studyId);
		// Add Study Instances in Environment (Summary Data) Dataset
		final List<org.generationcp.middleware.service.impl.study.StudyInstance> instances =
			this.middlewareStudyEnvironmentService
				.createStudyEnvironments(cropType, studyId, datasetId, numberOfEnvironmentsToGenerate);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<StudyInstance> studyInstances = new ArrayList<>();
		for (final org.generationcp.middleware.service.impl.study.StudyInstance instance : instances) {
			studyInstances.add(mapper.map(instance, StudyInstance.class));
		}
		return studyInstances;


	}

	@Override
	public List<StudyInstance> getStudyEnvironments(final int studyId) {
		this.studyValidator.validate(studyId, false);
		final List<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstances =
			this.middlewareStudyEnvironmentService.getStudyEnvironments(studyId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstances.stream().map(o -> mapper.map(o, StudyInstance.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteStudyEnvironments(final Integer studyId, final List<Integer> environmentIds) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(environmentIds), true);
		this.middlewareStudyEnvironmentService.deleteStudyEnvironments(studyId, environmentIds);
	}

	@Override
	public Optional<StudyInstance> getStudyEnvironment(final int studyId, final Integer environmentId) {
		this.studyValidator.validate(studyId, false);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(environmentId));
		final Optional<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstance =
			this.middlewareStudyEnvironmentService.getStudyEnvironment(studyId, environmentId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstance.isPresent()? Optional.of(mapper.map(studyInstance.get(), StudyInstance.class)) : Optional.empty();
	}

	@Override
	public EnvironmentData addEnvironmentData(final Integer studyId, final Integer environmentId, final EnvironmentData environmentData) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(environmentId));

		final Integer datasetId = this.getEnvironmentDatasetId(studyId);
		final Integer variableId = environmentData.getVariableId();
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
			variableId));
		this.observationValidator.validateObservationValue(variableId, environmentData.getValue());

		environmentData.setEnvironmentId(environmentId);
		final boolean isEnvironmentCondition =
			this.datasetService.getDatasetVariablesByType(studyId, datasetId, VariableType.STUDY_CONDITION).stream()
				.anyMatch(v -> v.getId().equals(variableId));
		this.middlewareStudyEnvironmentService.addEnvironmentData(environmentData, isEnvironmentCondition);
		return environmentData;
	}

	@Override
	public EnvironmentData updateEnvironmentData(final Integer studyId, final Integer environmentId, final Integer environmentDataId,
		final EnvironmentData environmentData) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(environmentId));

		final Integer datasetId = this.getEnvironmentDatasetId(studyId);
		final Integer variableId = environmentData.getVariableId();
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
			variableId));
		this.observationValidator.validateObservationValue(variableId, environmentData.getValue());

		final boolean isEnvironmentCondition =
			this.datasetService.getDatasetVariablesByType(studyId, datasetId, VariableType.STUDY_CONDITION).stream()
				.anyMatch(v -> v.getId().equals(variableId));
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Optional<EnvironmentData> existingEnvironmentData =
			this.middlewareStudyEnvironmentService.getEnvironmentData(environmentId, environmentDataId, isEnvironmentCondition);
		if (!existingEnvironmentData.isPresent()) {
			errors.reject("invalid.environment.data.id");
		} else if (!existingEnvironmentData.get().getVariableId().equals(variableId)) {
			errors.reject("invalid.variable.for.environment.data");
		}

		if (!errors.getAllErrors().isEmpty()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		environmentData.setEnvironmentDataId(environmentDataId);
		environmentData.setEnvironmentId(environmentId);
		this.middlewareStudyEnvironmentService.updateEnvironmentData(environmentData, isEnvironmentCondition);
		return environmentData;
	}

	private Integer getEnvironmentDatasetId(final Integer studyId) {
		final List<DatasetDTO> datasets =
			this.datasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!CollectionUtils.isEmpty(datasets)) {
			return datasets.get(0).getDatasetId();
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}
	}

}
