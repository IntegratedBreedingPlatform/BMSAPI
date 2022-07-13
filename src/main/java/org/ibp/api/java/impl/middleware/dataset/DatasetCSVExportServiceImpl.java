package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatasetCSVExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {

	@Resource
	private DatasetCSVGenerator datasetCSVGenerator;

	@Override
	public File export(
		final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final boolean singleFile) {

		this.validate(studyId, datasetId, instanceIds);
		try {
			return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetCSVGenerator, singleFile, CSV);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
			errors.reject("cannot.exportAsCSV.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	@Override
	public List<MeasurementVariable> getColumns(final int studyId, final int datasetId) {

		final List<MeasurementVariable> allVariables = new ArrayList<>(this.studyDatasetService.getAllDatasetVariables(studyId, datasetId));
		return this.moveSelectedVariableInTheFirstColumn(allVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(
		final Study study, final DatasetDTO dataset, final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap =
			this.studyDatasetService.getInstanceObservationUnitRowsMap(study.getId(), dataset.getDatasetId(),
				new ArrayList<>(selectedDatasetInstancesMap.keySet()));
		this.addLocationValues(observationUnitRowMap, selectedDatasetInstancesMap);
		return observationUnitRowMap;
	}

	void addLocationValues(final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap,
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		for (final Map.Entry<Integer, List<ObservationUnitRow>> entry : observationUnitRowMap.entrySet()) {
			final Integer instanceId = entry.getKey();
			final StudyInstance instance = selectedDatasetInstancesMap.get(instanceId);
			final ObservationUnitData locationIdData = new ObservationUnitData();
			locationIdData.setValue(instance.getLocationId().toString());
			observationUnitRowMap.get(instanceId)
				.forEach(row -> row.getVariables().put(DatasetServiceImpl.LOCATION_ID_VARIABLE_NAME, locationIdData));
			final ObservationUnitData locationAbbrData = new ObservationUnitData();
			locationAbbrData.setValue(instance.getLocationAbbreviation());
			observationUnitRowMap.get(instanceId)
				.forEach(row -> row.getVariables().put(DatasetServiceImpl.LOCATION_ABBR_VARIABLE_NAME, locationAbbrData));
		}
	}

}
