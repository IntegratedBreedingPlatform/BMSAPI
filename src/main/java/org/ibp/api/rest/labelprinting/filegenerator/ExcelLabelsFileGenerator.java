package org.ibp.api.rest.labelprinting.filegenerator;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.commons.util.FileUtils;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelPrintingFieldUtils;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic Excel label generator
 */
@Component
public class ExcelLabelsFileGenerator implements LabelsFileGenerator {

	@Override
	public File generate(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final String fileName = FileUtils.sanitizeFileName(labelsGeneratorInput.getFileName());
		final String fullFileName = fileName + "." + FileType.XLS.getExtension();
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + fullFileName;

		final Map<String, Field> fieldsByKey = Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(), field -> LabelPrintingFieldUtils.buildCombinedKey(field.getFieldType(), field.getId()));

		final HSSFWorkbook xlsBook = new HSSFWorkbook();
		final HSSFSheet sheet = xlsBook.createSheet(fileName);

		// Header

		final List<Field> headers = labelsGeneratorInput.getFields().stream()
			.flatMap(Collection::stream)
			.map(field -> fieldsByKey.get(field))
			.collect(Collectors.toList());

		int rowIndex = 0;

		if (labelsGeneratorInput.isIncludeHeadings()) {
			final HSSFRow headerRow = sheet.createRow(rowIndex++);
			int headerIndex = 0;
			for (final Field header : headers) {
				final HSSFCell cell = headerRow.createCell(headerIndex);
				cell.setCellValue(header.getName());
				headerIndex++;
			}
		}

		// values

		for (final Map<String, String> dataRow : labelsData.getData()) {
			final HSSFRow row = sheet.createRow(rowIndex);

			int colIndex = 0;
			for (final Field header : headers) {
				final HSSFCell cell = row.createCell(colIndex);
				cell.setCellValue(dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(header.getFieldType(), header.getId())));
				colIndex++;
			}
			rowIndex++;
		}

		for (int c = 0; c < sheet.getRow(0).getPhysicalNumberOfCells(); c++) {
			sheet.autoSizeColumn(c);
		}

		final File tempFile = new File(fileNameFullPath);

		try (final FileOutputStream fos = new FileOutputStream(tempFile)) {
			xlsBook.write(fos);
			return tempFile;
		}
	}
}
