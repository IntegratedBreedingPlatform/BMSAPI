package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class DatasetExcelExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {

	@Resource
	private DatasetExcelGenerator datasetExcelGenerator;

	@Override
	public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final boolean singleFile) {

		this.validate(studyId, datasetId, instanceIds);

		try {
			//TODO: use the singleFile boolean after implementing singleFile download for XLS option
			return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetExcelGenerator, false, XLS);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	@Override
	public List<MeasurementVariable> getColumns(final int studyId, final int datasetId) {
		return this.studyDatasetService.getSubObservationSetVariables(studyId, datasetId);
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(final Study study, final DatasetDTO dataset, final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = new HashMap<>();
		for(final Integer instanceDBID: selectedDatasetInstancesMap.keySet()) {
			final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
			searchDTO.setInstanceIds(Arrays.asList(selectedDatasetInstancesMap.get(instanceDBID).getInstanceId()));
			final PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE);
			final List<ObservationUnitRow> observationUnitRows = this.studyDatasetService
				.getObservationUnitRows(study.getId(), dataset.getDatasetId(), searchDTO, pageRequest);
			observationUnitRowMap.put(instanceDBID, observationUnitRows);
		}
		return observationUnitRowMap;
	}
}
