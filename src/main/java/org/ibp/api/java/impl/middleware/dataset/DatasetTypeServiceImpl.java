package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.java.dataset.DatasetTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
@Transactional
public class DatasetTypeServiceImpl implements DatasetTypeService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetTypeService middlewareDatasetTypeService;


	@Override
	public List<String> getObservationLevels(final Integer pageSize, final Integer pageNumber){
		return this.middlewareDatasetTypeService.getObservationLevels(pageSize, pageNumber);
	}

	@Override
	public Long countObservationLevels() {
		return this.middlewareDatasetTypeService.countObservationLevels();
	}

}
