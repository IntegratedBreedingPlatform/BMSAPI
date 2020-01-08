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
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatasetCSVExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {
	static final String LOCATION_ID_VARIABLE_NAME = "LOCATION";

	@Resource
	private DatasetCSVGenerator datasetCSVGenerator;

	@Autowired
	private DatasetService studyDatasetService;

	@Override
	public File export(
		final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final boolean singleFile) {

		this.validate(studyId, datasetId, instanceIds);
		try {
			return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetCSVGenerator, singleFile, CSV);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsCSV.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	@Override
	public List<MeasurementVariable> getColumns(final int studyId, final int datasetId) {

		final List<MeasurementVariable> allVariables = new ArrayList<>();
		allVariables.addAll(this.studyDatasetService.getAllDatasetVariables(studyId, datasetId));
		return this.moveSelectedVariableInTheFirstColumn(allVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(
		final Study study, final DatasetDTO dataset, final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = this.studyDatasetService.getInstanceObservationUnitRowsMap(study.getId(), dataset.getDatasetId(),
			new ArrayList<>(selectedDatasetInstancesMap.keySet()));
		this.transformEntryTypeValues(observationUnitRowMap);
		this.addLocationIdValues(observationUnitRowMap);
		return observationUnitRowMap;
	}

	void transformEntryTypeValues(final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap) {
		final List<Enumeration> entryTypes = this.ontologyDataManager
			.getStandardVariable(TermId.ENTRY_TYPE.getId(), ContextHolder.getCurrentProgram()).getEnumerations();
		final Map<String, String> entryTypeDescriptionNameMap =
			entryTypes.stream().collect(Collectors.toMap(Enumeration::getDescription, Enumeration::getName));

		final List<ObservationUnitRow> allRows =
			observationUnitRowMap.values().stream().flatMap(list -> list.stream()).collect(Collectors.toList());
		allRows.forEach(row -> {
			final ObservationUnitData data = row.getVariables().get(TermId.ENTRY_TYPE.name());
			data.setValue(entryTypeDescriptionNameMap.get(data.getValue()));
		});
	}

	void addLocationIdValues(final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap) {
		// FIXME IBP-3048: With location id now a property of StudyInstance, the query below is likely not needed
		final Map<Integer, String> instanceIdLocationIdMap = this.studyDataManager.getInstanceIdLocationIdMap(new ArrayList<>(observationUnitRowMap.keySet()));
		for(final Integer instanceId: observationUnitRowMap.keySet()) {
			final ObservationUnitData locationIdData = new ObservationUnitData();
			locationIdData.setValue(instanceIdLocationIdMap.get(instanceId));
			observationUnitRowMap.get(instanceId).forEach(row -> {
				row.getVariables().put(LOCATION_ID_VARIABLE_NAME, locationIdData);
			});
		}

	}

	void addLocationIdVariable(final List<MeasurementVariable> environmentDetailAndConditionVariables) {
		final MeasurementVariable locationIdVariable = new MeasurementVariable();
		locationIdVariable.setAlias(TermId.LOCATION_ID.name());
		locationIdVariable.setName(LOCATION_ID_VARIABLE_NAME);
		environmentDetailAndConditionVariables.add(0, locationIdVariable);
	}
}
