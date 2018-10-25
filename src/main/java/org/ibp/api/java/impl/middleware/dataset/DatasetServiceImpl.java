package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Override
	public List<DatasetDTO> getDatasetByStudyId(final Integer studyId, final Set<Integer> filterByTypeIds) {
		List<org.generationcp.middleware.domain.dms.DatasetDTO> datasetDTOS = this.datasetService.getDatasetByStudyId(studyId, filterByTypeIds);

		final ModelMapper mapper = new ModelMapper();
		final List<DatasetDTO> DatasetDTOs = mapper.map(datasetDTOS, List.class);
		return DatasetDTOs;
	}
}
