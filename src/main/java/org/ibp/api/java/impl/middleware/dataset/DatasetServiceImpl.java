package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private StudyDataManager studyDataManager;

	@Override
	public long countPhenotypes(final Integer studyId, final Integer datasetId, final List<Integer> traitIds) {
		this.studyValidator.validate(studyId, false);
		//FIXME - add validation if dataset is valid dataset of study (waiting on Middleware service to be available)
		return this.middlewareDatasetService.countPhenotypes(datasetId, traitIds);
	}

	@Override
	public List<DatasetDTO> getDatasetByStudyId(final Integer studyId, final Set<Integer> filterByTypeIds) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Study study = studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		if (filterByTypeIds != null) {
			for (Integer dataSetTypeId : filterByTypeIds) {
				final DataSetType dataSetType = DataSetType.findById(dataSetTypeId);
				if (dataSetType == null) {
					errors.reject("dataset.type.id.not.exist", new Object[] {dataSetTypeId}, "");
					throw new ResourceNotFoundException(errors.getAllErrors().get(0));
				}
			}
		}

		final List<org.generationcp.middleware.domain.dms.DatasetDTO> datasetDTOS =
			this.middlewareDatasetService.getDatasetByStudyId(studyId, filterByTypeIds);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<DatasetDTO> DatasetDTOs = new ArrayList();
		for (org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO : datasetDTOS) {
			final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
			DatasetDTOs.add(datasetDto);
		}
		return DatasetDTOs;
	}
}
