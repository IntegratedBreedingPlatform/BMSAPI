package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class DatasetCSVGenerator implements DatasetFileGenerator {

	protected File generateCSVFileWithHeaders(
		final List<MeasurementVariable> columns, final String fileNameFullPath, final CSVWriter csvWriter) {
		final File newFile = new File(fileNameFullPath);
		csvWriter.writeNext(this.getHeaderNames(columns).toArray(new String[] {}));
		return newFile;
	}

	protected void writeInstanceObservationUnitRowsToCSVFile(
		final List<MeasurementVariable> columns, final List<ObservationUnitRow> observationUnitRows,
		final CSVWriter csvWriter) {
		// feed in your array (or convert your data to an array)
		final List<String[]> rowValues = new ArrayList<>();
		for (final ObservationUnitRow row : observationUnitRows) {
			rowValues.add(this.getColumnValues(row, columns));
		}
		csvWriter.writeAll(rowValues);
	}

	@Override
	public File generateFile(final Integer studyId, final DatasetDTO dataSetDto, final List<MeasurementVariable> columns,
		final List<ObservationUnitRow> observationUnitRows,
		final String fileNameFullPath) throws IOException {
		//Do nothing. Return a new file. This method is not used.
		return new File(fileNameFullPath);
	}

	protected String[] getColumnValues(final ObservationUnitRow row, final List<MeasurementVariable> subObservationSetColumns) {
		final List<String> values = new LinkedList<>();
		for (final MeasurementVariable column : subObservationSetColumns) {
			values.add(row.getVariables().get(column.getName()).getValue());
		}
		return values.toArray(new String[] {});
	}

	protected List<String> getHeaderNames(final List<MeasurementVariable> subObservationSetColumns) {
		final List<String> headerNames = new LinkedList<>();
		for (final MeasurementVariable measurementVariable : subObservationSetColumns) {
			headerNames.add(measurementVariable.getAlias());
		}
		return headerNames;
	}

}
