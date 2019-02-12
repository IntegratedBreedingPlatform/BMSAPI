package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class DatasetKSUExcelGenerator extends DatasetExcelGenerator implements DatasetFileGenerator {

	@Override
	public File generateSingleInstanceFile(
		final Integer studyId,
		final DatasetDTO dataSetDto, final List<MeasurementVariable> columns,
		final List<ObservationUnitRow> reorderedObservationUnitRows,
		final String fileNamePath) throws IOException {
		final HSSFWorkbook xlsBook = new HSSFWorkbook();
		this.writeObservationSheet(columns, reorderedObservationUnitRows, xlsBook, dataSetDto.getName());

		final File file = new File(fileNamePath);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			xlsBook.write(fos);
		}
		return file;
	}

	@Override
	public File generateTraitAndSelectionVariablesFile(final List<String[]> rowValues, final String filenamePath) throws IOException{
		try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(filenamePath), StandardCharsets.UTF_8), ',')){
			final File newFile = new File(filenamePath);
			csvWriter.writeAll(rowValues);
			return newFile;
		}
	}

}
