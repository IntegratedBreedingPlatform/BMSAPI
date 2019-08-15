package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class DatasetCSVExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {
	static final String LOCATION_ID_VARIABLE_NAME = "LOCATION";

	@Resource
	private DatasetCSVGenerator datasetCSVGenerator;

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

		final DatasetDTO datasetDTO = this.datasetService.getDataset(datasetId);
		final List<Integer> subObsDatasetTypeIds = this.datasetTypeService.getSubObservationDatasetTypeIds();
		final int environmentDatasetId =
			this.studyDataManager.getDataSetsByType(studyId, DatasetTypeEnum.SUMMARY_DATA.getId()).get(0).getId();
		final int plotDatasetId;

		if (datasetDTO.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			plotDatasetId = datasetDTO.getDatasetId();
		} else {
			plotDatasetId = datasetDTO.getParentDatasetId();
		}

		final List<MeasurementVariable> studyVariables = this.datasetService
			.getObservationSetVariables(studyId, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));
		final List<MeasurementVariable> environmentDetailAndConditionVariables = this.datasetService
			.getObservationSetVariables(environmentDatasetId, Lists.newArrayList(
				VariableType.ENVIRONMENT_DETAIL.getId(),
				VariableType.STUDY_CONDITION.getId()));
		this.addLocationIdVariable(environmentDetailAndConditionVariables);
		// Experimental Design variables have value at dataset level. Perform sorting to ensure that they come first
		Collections.sort(environmentDetailAndConditionVariables, new Comparator<MeasurementVariable>() {

			@Override
			public int compare(final MeasurementVariable var1, final MeasurementVariable var2) {
				final String value1 = var1.getValue();
				final String value2 = var2.getValue();
				if (value1 != null && value2 != null)
					return value1.compareTo(value2);
				return (value1 == null) ? 1 : -1;
			}
		});

		final List<MeasurementVariable> plotDataSetColumns =
			this.datasetService
				.getObservationSetVariables(
					plotDatasetId,
					Lists.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.TREATMENT_FACTOR.getId(), VariableType.OBSERVATION_UNIT.getId()));
		final List<MeasurementVariable> treatmentFactors =
			this.datasetService
				.getObservationSetVariables(plotDatasetId, Lists.newArrayList(TermId.MULTIFACTORIAL_INFO.getId()));
		plotDataSetColumns.removeAll(treatmentFactors);

		final List<MeasurementVariable> traits =
			this.datasetService.getObservationSetVariables(datasetId, Lists.newArrayList(VariableType.TRAIT.getId()));
		final List<MeasurementVariable> selectionVariables =
			this.datasetService.getObservationSetVariables(datasetId, Lists.newArrayList(VariableType.SELECTION_METHOD.getId()));
		final List<MeasurementVariable> allVariables = new ArrayList<>();
		allVariables.addAll(studyVariables);
		allVariables.addAll(environmentDetailAndConditionVariables);
		allVariables.addAll(treatmentFactors);
		allVariables.addAll(plotDataSetColumns);

		//Add variables that are specific to the sub-observation dataset types
		if (Arrays.stream(subObsDatasetTypeIds.toArray()).anyMatch(datasetDTO.getDatasetTypeId()::equals)) {
			final List<MeasurementVariable> subObservationSetColumns =
				this.datasetService
					.getObservationSetVariables(datasetId, Lists.newArrayList(
						VariableType.GERMPLASM_DESCRIPTOR.getId(),
						VariableType.OBSERVATION_UNIT.getId()));
			allVariables.addAll(subObservationSetColumns);

		}
		allVariables.addAll(traits);
		allVariables.addAll(selectionVariables);
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
			.getStandardVariable(TermId.ENTRY_TYPE.getId(), this.contextUtil.getCurrentProgramUUID()).getEnumerations();
		final Map<String, String> entryTypeDescriptionNameMap = new HashMap<>();
		for(final Enumeration entryType: entryTypes) {
			entryTypeDescriptionNameMap.put(entryType.getDescription(), entryType.getName());
		}

		for(final Integer instanceId: observationUnitRowMap.keySet()) {
			final List<ObservationUnitRow> observationUnitRows = observationUnitRowMap.get(instanceId);
			for(final ObservationUnitRow row: observationUnitRows) {
				final ObservationUnitData data = row.getVariables().get(TermId.ENTRY_TYPE.name());
				data.setValue(entryTypeDescriptionNameMap.get(data.getValue()));
			}
		}
	}

	void addLocationIdValues(final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap) {
		final Map<Integer, String> instanceIdLocationIdMap = this.studyDataManager.getInstanceIdLocationIdMap(new ArrayList<>(observationUnitRowMap.keySet()));
		for(final Integer instanceId: observationUnitRowMap.keySet()) {
			final List<ObservationUnitRow> observationUnitRows = observationUnitRowMap.get(instanceId);
			final ObservationUnitData locationIdData = new ObservationUnitData();
			locationIdData.setValue(instanceIdLocationIdMap.get(instanceId));
			for(final ObservationUnitRow row: observationUnitRows) {
				row.getVariables().put(LOCATION_ID_VARIABLE_NAME, locationIdData);
			}
		}

	}

	void addLocationIdVariable(final List<MeasurementVariable> environmentDetailAndConditionVariables) {
		final MeasurementVariable locationIdVariable = new MeasurementVariable();
		locationIdVariable.setAlias(TermId.LOCATION_ID.name());
		locationIdVariable.setName(LOCATION_ID_VARIABLE_NAME);
		environmentDetailAndConditionVariables.add(0, locationIdVariable);
	}
}
