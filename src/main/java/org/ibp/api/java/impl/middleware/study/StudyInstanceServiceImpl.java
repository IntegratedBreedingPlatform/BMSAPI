package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
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
	private DatasetService datasetService;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private InstanceValidator instanceValidator;

	@Resource
	private PlantingServiceImpl plantingService;

	@Override
	public StudyInstance createStudyInstance(final String cropName, final int studyId) {

		this.studyValidator.validate(studyId, true);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		final List<DatasetDTO> datasets = this.datasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
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
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Integer pendingTransactions =
			this.plantingService.getPlantingTransactionsByInstanceId(instanceId, TransactionStatus.PENDING).size();
		final Integer confirmedTransactions =
			this.plantingService.getPlantingTransactionsByInstanceId(instanceId, TransactionStatus.CONFIRMED).size();
		if (pendingTransactions > 0 || confirmedTransactions > 0) {
			errors.reject("dataset.instance.has.pending.or.confirmed.transactions");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
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
