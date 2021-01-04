package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.dms.InstanceDescriptorData;
import org.generationcp.middleware.domain.dms.InstanceObservationData;
import org.generationcp.middleware.domain.dms.InstanceVariableData;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;
import org.ibp.api.java.study.StudyService;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	public static final String INVALID_ENVIRONMENT_DATA_ID = "invalid.environment.data.id";
	public static final String INVALID_VARIABLE_FOR_ENVIRONMENT_DATA = "invalid.variable.for.environment.data";
	@Resource
	private org.generationcp.middleware.service.api.study.StudyInstanceService middlewareStudyInstanceService;

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

	@Autowired
	private StudyService studyService;

	@Override
	public List<StudyInstance> createStudyInstances(final String cropName, final int studyId, final Integer numberOfInstancesToGenerate) {
		if (numberOfInstancesToGenerate < 1 || numberOfInstancesToGenerate > 999) {
			throw new ApiRuntimeException("Invalid number of instances to generate. Please specify number between 1 to 999.");
		}
		this.studyValidator.validate(studyId, true);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);
		final Integer datasetId = this.studyService.getEnvironmentDatasetId(studyId);
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
	public InstanceObservationData addInstanceObservation(final Integer studyId, final Integer instanceId,
		final InstanceObservationData instanceObservationData) {
		this.validateInstanceData(studyId, instanceId, instanceObservationData, VariableType.ENVIRONMENT_CONDITION);

		instanceObservationData.setInstanceId(instanceId);
		this.middlewareStudyInstanceService.addInstanceObservation(instanceObservationData);
		return instanceObservationData;
	}

	@Override
	public InstanceObservationData updateInstanceObservation(final Integer studyId, final Integer instanceId,
		final Integer observationDataId,
		final InstanceObservationData instanceObservationData) {
		this.validateInstanceData(studyId, instanceId, instanceObservationData, VariableType.ENVIRONMENT_CONDITION);
		final Integer variableId = instanceObservationData.getVariableId();

		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final Optional<InstanceObservationData> existingObservationData =
			this.middlewareStudyInstanceService.getInstanceObservation(instanceId, observationDataId, variableId);
		if (!existingObservationData.isPresent()) {
			errors.reject(INVALID_ENVIRONMENT_DATA_ID);
		} else if (!existingObservationData.get().getVariableId().equals(variableId)) {
			errors.reject(INVALID_VARIABLE_FOR_ENVIRONMENT_DATA);
		}

		if (!errors.getAllErrors().isEmpty()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		instanceObservationData.setInstanceObservationId(observationDataId);
		instanceObservationData.setInstanceId(instanceId);
		this.middlewareStudyInstanceService.updateInstanceObservation(instanceObservationData);
		return instanceObservationData;
	}

	@Override
	public InstanceDescriptorData addInstanceDescriptorData(final Integer studyId, final Integer instanceId,
		final InstanceDescriptorData instanceDescriptorData) {

		this.validateInstanceData(studyId, instanceId, instanceDescriptorData, VariableType.ENVIRONMENT_DETAIL);
		instanceDescriptorData.setInstanceId(instanceId);
		this.middlewareStudyInstanceService.addInstanceDescriptorData(instanceDescriptorData);
		return instanceDescriptorData;
	}

	@Override
	public InstanceDescriptorData updateInstanceDescriptorData(final Integer studyId, final Integer instanceId,
		final Integer descriptorDataId,
		final InstanceDescriptorData instanceDescriptorData) {
		this.validateInstanceData(studyId, instanceId, instanceDescriptorData, VariableType.ENVIRONMENT_DETAIL);
		final Integer variableId = instanceDescriptorData.getVariableId();

		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final Optional<InstanceDescriptorData> existingDescriptorData =
			this.middlewareStudyInstanceService.getInstanceDescriptorData(instanceId, descriptorDataId, variableId);
		if (!existingDescriptorData.isPresent()) {
			errors.reject(INVALID_ENVIRONMENT_DATA_ID);
		} else if (!existingDescriptorData.get().getVariableId().equals(variableId)) {
			errors.reject(INVALID_VARIABLE_FOR_ENVIRONMENT_DATA);
		}

		if (!errors.getAllErrors().isEmpty()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		instanceDescriptorData.setInstanceDescriptorDataId(descriptorDataId);
		instanceDescriptorData.setInstanceId(instanceId);
		this.middlewareStudyInstanceService.updateInstanceDescriptorData(instanceDescriptorData);
		return instanceDescriptorData;
	}

	private void validateInstanceData(final Integer studyId, final Integer instanceId, final InstanceVariableData instanceVariableData, final VariableType variableType) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(instanceId));

		final Integer datasetId = this.studyService.getEnvironmentDatasetId(studyId);
		final Integer variableId = instanceVariableData.getVariableId();
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
				variableId));
		this.observationValidator.validateVariableValue(variableId, instanceVariableData.getValue());
		this.datasetValidator.validateVariableBelongsToVariableType(datasetId, variableId, variableType.getId());
	}


}
