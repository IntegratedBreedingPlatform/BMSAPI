package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
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

@Service
@Transactional
public class DatasetKSUCSVExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {

	@Resource
	private DatasetKSUCSVGenerator datasetKSUCSVGenerator;

	@Override
	public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final boolean singleFile) {

		this.validate(studyId, datasetId, instanceIds);

		try {
			//TODO: use the singleFile boolean after implementing singleFile download for KSU CSV option
			return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetKSUCSVGenerator, false, CSV);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	@Override
	public List<MeasurementVariable> getColumns(final int studyId, final int datasetId) {
		final DatasetDTO dataSetDTO = this.datasetService.getDataset(datasetId);
		final int plotDatasetId = dataSetDTO.getParentDatasetId();

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

		final List<MeasurementVariable> allVariables = new ArrayList<>();
		allVariables.addAll(plotDataSetColumns);
		allVariables.addAll(subObservationSetColumns);
		return this.moveSelectedVariableInTheFirstColumn(allVariables, TermId.OBS_UNIT_ID.getId());
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(final Study study, final DatasetDTO dataset, final int collectionOrderId, final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		return this.studyDatasetService.getInstanceObservationUnitRowsMap(study.getId(), dataset.getDatasetId(), new ArrayList<>(selectedDatasetInstancesMap.keySet()));
	}
}
