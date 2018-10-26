package org.ibp.api.java.impl.middleware.dataset;

import java.util.List;

import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

	public int countTotalObservationUnitsForDataset(final int datasetId, final int instanceId) {
		return this.datasetService.countTotalObservationUnitsForDataset(datasetId, instanceId);
	}

	@Override
	public List<ObservationUnitRow> getObservationUnitRows(final int studyId, final int datasetId, final int instanceId,
		final int pageNumber, final int pageSize, final String sortBy, final String sortOrder) {
		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows =
			this.datasetService.getObservationUnitRows(studyId, datasetId, instanceId, pageNumber, pageSize, sortBy, sortOrder);
		final ModelMapper mapper = new ModelMapper();
		final List<ObservationUnitRow> list = new ArrayList<>();
		for (final org.generationcp.middleware.service.api.dataset.ObservationUnitRow dto : observationUnitRows) {
			final ObservationUnitRow observationUnitRow = mapper.map(dto, ObservationUnitRow.class);
			list.add(observationUnitRow);
		}
		return list;
	}

	/*@Override
	public Integer generateSubObservationDataset(final DatasetGeneratorInput datasetGeneratorInput) {
		return datasetService.generateSubObservationDataset(null, null, null, null, null, null);
	}*/

}
