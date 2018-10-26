package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.ModelMapper;
import java.util.List;

import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Override
	public long countPhenotypes(final Integer studyId, final Integer datasetId, final List<Integer> traitIds) {
		this.studyValidator.validate(studyId, false);
		//FIXME - add validation if dataset is valid dataset of study (waiting on Middleware service to be available)
		return this.middlewareDatasetService.countPhenotypes(datasetId, traitIds);
	}

	@Override
	public List<DatasetDTO> getDatasetByStudyId(final Integer studyId, final Set<Integer> filterByTypeIds) {
		List<org.generationcp.middleware.domain.dms.DatasetDTO> datasetDTOS = this.datasetService.getDatasetByStudyId(studyId, filterByTypeIds);

		final ModelMapper mapper = new ModelMapper();
		final List<DatasetDTO> DatasetDTOs = mapper.map(datasetDTOS, List.class);
		return DatasetDTOs;
	}
}
