package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class DatasetCSVGenerator {

	protected File generateCSVFile(
		final List<String> headerNames, final List<ObservationUnitRow> observationUnitRows,
		final String fileNameFullPath, final CSVWriter csvWriter) throws IOException {
		final File newFile = new File(fileNameFullPath);
		// feed in your array (or convert your data to an array)
		final List<String[]> rowValues = new ArrayList<>();

		rowValues.add(headerNames.toArray(new String[] {}));

		for (final ObservationUnitRow row : observationUnitRows) {
			rowValues.add(this.getColumnValues(row, headerNames));
		}

		csvWriter.writeAll(rowValues);
		csvWriter.close();
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
