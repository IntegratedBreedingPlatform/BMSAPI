package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.dms.DatasetBasicDTO;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyInstanceServiceImpl implements StudyInstanceService {

	@Resource
	private org.generationcp.middleware.service.api.study.StudyInstanceService studyInstanceMiddlewareService;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private InstanceValidator instanceValidator;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Override
	public StudyInstance createStudyInstance(final String cropName, final int studyId) {

		this.studyValidator.validate(studyId, true);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		final List<DatasetBasicDTO> datasets = this.middlewareDatasetService.getDatasetBasicDTOs(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!datasets.isEmpty()) {
			// Add Study Instance in Environment (Summary Data) Dataset
			final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance =
				this.studyInstanceMiddlewareService.createStudyInstance(cropType, studyId, datasets.get(0).getDatasetId());
			final ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
			return mapper.map(studyInstance, StudyInstance.class);
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}

	}

	@Override
	public List<StudyInstance> getStudyInstances(final int studyId) {
		this.studyValidator.validate(studyId, false);
		final List<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstances =
			this.studyInstanceMiddlewareService.getStudyInstances(studyId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstances.stream().map(o -> mapper.map(o, StudyInstance.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteStudyInstance(final Integer studyId, final Integer instanceId) {
		this.studyValidator.validate(studyId, true);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(instanceId), true);
		this.studyInstanceMiddlewareService.deleteStudyInstance(studyId, instanceId);
	}

	@Override
	public Optional<StudyInstance> getStudyInstance(final int studyId, final Integer instanceId) {
		this.studyValidator.validate(studyId, false);
		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(instanceId));
		final com.google.common.base.Optional<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstance =
			this.studyInstanceMiddlewareService.getStudyInstance(studyId, instanceId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return studyInstance.isPresent()? Optional.of(mapper.map(studyInstance.get(), StudyInstance.class)) : Optional.empty();
	}

}
