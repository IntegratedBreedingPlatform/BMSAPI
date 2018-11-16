package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DatasetExportServiceImpl implements DatasetExportService {

	private static final String TEMP_FILE_DIR = new File(System.getProperty("java.io.tmpdir")).getPath();

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private DatasetService studyDatasetService;

	private final ZipUtil zipUtil = new ZipUtil();

	@Override
	public File exportAsCSV(final int studyId, final int datasetId, final Set<Integer> instanceIds) {

		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		try {
			return this.generateCSVFiles(studyId, datasetId, instanceIds);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsCSV.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	protected File generateCSVFiles(final int studyId, final int datasetId, final Set<Integer> instanceIds) throws IOException {
		final List<File> csvFiles = new ArrayList<>();
		final List<String> headerNames = getHeaderNames(this.studyDatasetService.getSubObservationSetColumns(studyId, datasetId));

		for (final int instanceId : instanceIds) {
			final List<ObservationUnitRow> observationUnitRows =
				this.studyDatasetService.getObservationUnitRows(studyId, datasetId, instanceId, Integer.MAX_VALUE, Integer.MAX_VALUE, null,
					"");
			final String csvFileName = TEMP_FILE_DIR + "Export-" + UUID.randomUUID() + ".csv";
			csvFiles.add(generateCSVFile(headerNames, observationUnitRows, csvFileName));
		}

		if (csvFiles.size() == 1) {
			return csvFiles.get(0);
		} else {
			return this.zipUtil.zipFiles(csvFiles);
		}
	}

	protected File generateCSVFile(
		final List<String> headerNames, final List<ObservationUnitRow> observationUnitRows,
		final String fileNameFullPath) throws IOException {
		final File newFile = new File(fileNameFullPath);

		final CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',');

		// feed in your array (or convert your data to an array)
		final List<String[]> rowValues = new ArrayList<>();

		rowValues.add(headerNames.toArray(new String[] {}));

		for (final ObservationUnitRow row : observationUnitRows) {
			rowValues.add(this.getColumnValues(row, headerNames));
		}

		writer.writeAll(rowValues);
		writer.close();
		return newFile;
	}

	protected String[] getColumnValues(final ObservationUnitRow row, final List<String> headerNames) {
		final List<String> values = new LinkedList<>();
		for (final String headerName : headerNames) {
			values.add(row.getVariables().get(headerName).getValue());
		}
		return values.toArray(new String[] {});
	}

	protected List<String> getHeaderNames(final List<MeasurementVariable> subObservationSetColumns) {
		final List<String> headerNames = new LinkedList<>();
		for (final MeasurementVariable measurementVariable : subObservationSetColumns) {
			headerNames.add(measurementVariable.getName());
		}
		return headerNames;
	}
}
