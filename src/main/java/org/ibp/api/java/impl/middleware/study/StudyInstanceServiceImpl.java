package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.study.StudyEnvironmentService;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyInstanceServiceImpl implements StudyInstanceService {

	@Resource
	private StudyEnvironmentService middlewareStudyEnvironmentService;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private InstanceValidator instanceValidator;

	@Override
	public List<StudyInstance> createStudyInstances(final String cropName, final int studyId, final Integer numberOfInstancesToGenerate) {
		if (numberOfInstancesToGenerate < 1) {
			throw new ApiRuntimeException("Invalid number of instances to generate.");
		}
		this.studyValidator.validate(studyId, true);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		final List<DatasetDTO> datasets = this.datasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		final List<StudyInstance> studyInstances = new ArrayList<>();
		if (!datasets.isEmpty()) {
			// Add Study Instance in Environment (Summary Data) Dataset
			final List<org.generationcp.middleware.service.impl.study.StudyInstance> instances =
				this.middlewareStudyEnvironmentService
					.createStudyEnvironments(cropType, studyId, datasets.get(0).getDatasetId(), numberOfInstancesToGenerate);
			final ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
			for (final org.generationcp.middleware.service.impl.study.StudyInstance instance : instances) {
				studyInstances.add(mapper.map(instance, StudyInstance.class));
			}
			return studyInstances;
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}

	}

	@Override
	public List<StudyInstance> getStudyInstances(final int studyId) {
		this.studyValidator.validate(studyId, false);
		final List<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstances =
			this.middlewareStudyEnvironmentService.getStudyEnvironments(studyId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstances.stream().map(o -> mapper.map(o, StudyInstance.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteStudyInstances(final Integer studyId, final List<Integer> instanceIds) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(instanceIds), true);
		this.middlewareStudyEnvironmentService.deleteStudyEnvironments(studyId, instanceIds);
	}

	@Override
	public Optional<StudyInstance> getStudyInstance(final int studyId, final Integer instanceId) {
		this.studyValidator.validate(studyId, false);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(instanceId));
		final Optional<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstance =
			this.middlewareStudyEnvironmentService.getStudyEnvironments(studyId, instanceId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstance.isPresent()? Optional.of(mapper.map(studyInstance.get(), StudyInstance.class)) : Optional.empty();
	}

}
