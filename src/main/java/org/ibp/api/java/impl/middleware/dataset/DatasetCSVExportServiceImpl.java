package org.ibp.api.java.impl.middleware.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import com.google.common.collect.Lists;

@Service
@Transactional
public class DatasetCSVExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {

	@Resource
	private DatasetCSVGenerator datasetCSVGenerator;

	@Override
	public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final boolean singleFile) {

		this.validate(studyId, datasetId, instanceIds);
		try {
			return this.generate(studyId, datasetId, instanceIds, collectionOrderId, datasetCSVGenerator, singleFile, CSV);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsCSV.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	@Override
	public List<MeasurementVariable> getColumns(final int studyId, final int datasetId) {
		
		final DatasetDTO dataSetDTO = this.datasetService.getDataset(datasetId);
		final int environmentDatasetId =
				this.studyDataManager.getDataSetsByType(studyId, DataSetType.SUMMARY_DATA).get(0).getId();
		final int plotDatasetId = dataSetDTO.getParentDatasetId();
		
		final List<MeasurementVariable> studyVariables = this.datasetService
				.getMeasurementVariables(studyId, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));
		final List<MeasurementVariable> environmentDetailsVariables = this.datasetService
				.getMeasurementVariables(environmentDatasetId, Lists.newArrayList(
						VariableType.ENVIRONMENT_DETAIL.getId(),
						VariableType.STUDY_CONDITION.getId()));
		// Experimental Design variables have value at dataset level. Perform sorting to ensure that they come first
		Collections.sort(environmentDetailsVariables, new Comparator<MeasurementVariable>() {
			@Override
			public int compare(MeasurementVariable var1, MeasurementVariable var2) {
				final String value1 = var1.getValue();
				final String value2 = var2.getValue();
		        if (value1 != null && value2 != null)
		            return value1.compareTo(value2);
		        return (value1 == null) ? 1 : -1;
		    }
		});
		final List<MeasurementVariable> environmentConditions = this.datasetService
				.getMeasurementVariables(environmentDatasetId, Lists.newArrayList(VariableType.TRAIT.getId()));
		
		final List<MeasurementVariable> plotDataSetColumns =
				this.datasetService
						.getMeasurementVariables(plotDatasetId,
								Lists.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
										VariableType.TREATMENT_FACTOR.getId(), VariableType.OBSERVATION_UNIT.getId()));
		final List<MeasurementVariable> subObservationSetColumns =
				this.datasetService
				.getMeasurementVariables(datasetId, Lists.newArrayList(
						VariableType.GERMPLASM_DESCRIPTOR.getId(),
						VariableType.OBSERVATION_UNIT.getId()));
		final List<MeasurementVariable> treatmentFactors =
				this.datasetService
				.getMeasurementVariables(plotDatasetId, Lists.newArrayList(TermId.MULTIFACTORIAL_INFO.getId()));
		 plotDataSetColumns.removeAll(treatmentFactors);

		final List<MeasurementVariable> traits =
			this.datasetService.getMeasurementVariables(datasetId, Lists.newArrayList(VariableType.TRAIT.getId()));
		final List<MeasurementVariable> selectionVariables =
			this.datasetService.getMeasurementVariables(datasetId, Lists.newArrayList(VariableType.SELECTION_METHOD.getId()));
		final List<MeasurementVariable> allVariables = new ArrayList<>();
		allVariables.addAll(studyVariables);
		allVariables.addAll(environmentDetailsVariables);
		allVariables.addAll(environmentConditions);
		allVariables.addAll(treatmentFactors);
		allVariables.addAll(plotDataSetColumns);
		allVariables.addAll(subObservationSetColumns);
		allVariables.addAll(traits);
		allVariables.addAll(selectionVariables);
		return this.moveSelectedVariableInTheFirstColumn(allVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
	}
}
