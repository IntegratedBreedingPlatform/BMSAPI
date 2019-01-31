package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		return this.reorderColumns(this.studyDatasetService.getAllDatasetVariables(studyId, datasetId));
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(final Study study, final DatasetDTO dataSet, final int collectionOrderId, final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = this.studyDatasetService.getInstanceObservationUnitRowsMap(study.getId(), dataSet.getDatasetId(), new ArrayList<>(selectedDatasetInstancesMap.keySet()));
		return observationUnitRowMap;
	}

	@Override
	public File writeSingleFile(
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns,
		final String fileNameFullPath) throws IOException {
		final CSVWriter csvWriter =
			new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',');
		final File file = this.datasetCSVGenerator.generateCSVFileWithHeaders(columns, fileNameFullPath, csvWriter);
		for(final Integer instanceDBID: observationUnitRowMap.keySet()) {
			final List<ObservationUnitRow> observationUnitRows = observationUnitRowMap.get(instanceDBID);
			this.datasetCSVGenerator.writeInstanceObservationUnitRowsToCSVFile(columns, observationUnitRows, csvWriter);
		}
		csvWriter.close();
		return file;
	}

	protected List<MeasurementVariable> reorderColumns(List<MeasurementVariable> columns) {
		int trialInstanceIndex = 0;
		for(MeasurementVariable column: columns) {
			if(TermId.TRIAL_INSTANCE_FACTOR.getId() == column.getTermId()) {
				final MeasurementVariable trialInstanceMeasurementVariable = columns.remove(trialInstanceIndex);
				columns.add(0, trialInstanceMeasurementVariable);
				break;
			}
			trialInstanceIndex++;
		}
		return columns;

	}
}
