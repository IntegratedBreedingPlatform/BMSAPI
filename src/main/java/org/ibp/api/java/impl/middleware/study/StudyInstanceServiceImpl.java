package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.dms.DatasetBasicDTO;
import org.generationcp.middleware.domain.dms.InstanceData;
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
import org.ibp.api.java.study.StudyInstanceService;
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
public class StudyInstanceServiceImpl implements StudyInstanceService {

	@Resource
	private org.generationcp.middleware.service.api.study.StudyInstanceService middlewareStudyInstanceService;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private InstanceValidator instanceValidator;

	@Resource
	private DatasetValidator datasetValidator;

	@Autowired
	private ObservationValidator observationValidator;

	@Override
	public List<StudyInstance> createStudyInstances(final String cropName, final int studyId, final Integer numberOfInstancesToGenerate) {
		if (numberOfInstancesToGenerate < 1 || numberOfInstancesToGenerate > 999) {
			throw new ApiRuntimeException("Invalid number of instances to generate. Please specify number between 1 to 999.");
		}
		this.studyValidator.validate(studyId, true);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);
		final Integer datasetId = this.getEnvironmentDatasetId(studyId);
		// Add Study Instances in Environment (Summary Data) Dataset
		final List<org.generationcp.middleware.service.impl.study.StudyInstance> instances =
			this.middlewareStudyInstanceService
				.createStudyInstances(cropType, studyId, datasetId, numberOfInstancesToGenerate);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<StudyInstance> studyInstances = new ArrayList<>();
		for (final org.generationcp.middleware.service.impl.study.StudyInstance instance : instances) {
			studyInstances.add(mapper.map(instance, StudyInstance.class));
		}
		return studyInstances;

	}

	@Override
	public List<StudyInstance> getStudyInstances(final int studyId) {
		this.studyValidator.validate(studyId, false);
		final List<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstances =
			this.middlewareStudyInstanceService.getStudyInstances(studyId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstances.stream().map(o -> mapper.map(o, StudyInstance.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteStudyInstances(final Integer studyId, final List<Integer> instanceIds) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(instanceIds), true);
		this.middlewareStudyInstanceService.deleteStudyInstances(studyId, instanceIds);
	}

	@Override
	public Optional<StudyInstance> getStudyInstance(final int studyId, final Integer instanceId) {
		this.studyValidator.validate(studyId, false);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(instanceId));
		final Optional<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstance =
			this.middlewareStudyInstanceService.getStudyInstance(studyId, instanceId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstance.isPresent() ? Optional.of(mapper.map(studyInstance.get(), StudyInstance.class)) : Optional.empty();
	}

	@Override
	public InstanceData addInstanceData(final Integer studyId, final Integer instanceId, final InstanceData instanceData) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(instanceId));

		final Integer datasetId = this.getEnvironmentDatasetId(studyId);
		final Integer variableId = instanceData.getVariableId();
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
			variableId));
		this.observationValidator.validateObservationValue(variableId, instanceData.getValue());

		instanceData.setInstanceId(instanceId);
		final boolean isEnvironmentCondition =
			this.datasetService.getDatasetVariablesByType(studyId, datasetId, VariableType.STUDY_CONDITION).stream()
				.anyMatch(v -> v.getId().equals(variableId));
		this.middlewareStudyInstanceService.addInstanceData(instanceData, isEnvironmentCondition);
		return instanceData;
	}

	@Override
	public InstanceData updateInstanceData(final Integer studyId, final Integer instanceId, final Integer instanceDataId,
		final InstanceData instanceData) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(instanceId));

		final Integer datasetId = this.getEnvironmentDatasetId(studyId);
		final Integer variableId = instanceData.getVariableId();
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
			variableId));
		this.observationValidator.validateObservationValue(variableId, instanceData.getValue());

		final boolean isEnvironmentCondition =
			this.datasetService.getDatasetVariablesByType(studyId, datasetId, VariableType.STUDY_CONDITION).stream()
				.anyMatch(v -> v.getId().equals(variableId));
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Optional<InstanceData> existingEnvironmentData =
			this.middlewareStudyInstanceService.getInstanceData(instanceId, instanceDataId, isEnvironmentCondition);
		if (!existingEnvironmentData.isPresent()) {
			errors.reject("invalid.environment.data.id");
		} else if (!existingEnvironmentData.get().getVariableId().equals(variableId)) {
			errors.reject("invalid.variable.for.environment.data");
		}

		if (!errors.getAllErrors().isEmpty()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		instanceData.setInstanceDataId(instanceDataId);
		instanceData.setInstanceId(instanceId);
		this.middlewareStudyInstanceService.updateInstanceData(instanceData, isEnvironmentCondition);
		return instanceData;
	}

	private Integer getEnvironmentDatasetId(final Integer studyId) {
		final List<DatasetBasicDTO> datasets =
			this.middlewareDatasetService.getDatasetBasicDTOs(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!CollectionUtils.isEmpty(datasets)) {
			return datasets.get(0).getDatasetId();
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}
	}

}
