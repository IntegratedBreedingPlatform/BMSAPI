package org.ibp.api.java.impl.middleware.dataset;

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
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Override
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
