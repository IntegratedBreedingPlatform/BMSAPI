package org.ibp.api.rest.labelprinting.filegenerator;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import liquibase.util.StringUtils;
import org.generationcp.commons.util.FileUtils;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelPrintingFieldUtils;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by clarysabel on 2/7/19.
 */
@Component
public class CSVLabelsFileGenerator implements LabelsFileGenerator {

	private static String BARCODE = "Barcode";

	@Override
	public File generate(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final String sanitizedFileName = FileUtils.sanitizeFileName(String.format("%s." + "csv", labelsGeneratorInput.getFileName()));

		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

		try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8),
				',')) {

			final Map<String, Field> keyFieldMap = Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(),
				field -> LabelPrintingFieldUtils.buildCombinedKey(field.getFieldType(), field.getId()));

			final File newFile = new File(fileNameFullPath);
			// feed in your array (or convert your data to an array)
			final List<String[]> rowValues = new ArrayList<>();

			if (labelsGeneratorInput.isIncludeHeadings()) {
				rowValues.add(this.getHeaderNames(labelsGeneratorInput, keyFieldMap).toArray(new String[] {}));
			}

			labelsData.getData().forEach(
					labels -> rowValues.add(this.getColumnValues(labels, labelsGeneratorInput, labelsData.getDefaultBarcodeKey())));
			csvWriter.writeAll(rowValues);
			return newFile;
		}
	}

	private String[] getColumnValues(final Map<String, String> labels, final LabelsGeneratorInput labelsGeneratorInput, final String defaultBarcodeKey) {
		final List<String> values = new LinkedList<>();
		for (final List<String> fieldsList : labelsGeneratorInput.getFields()) {
			fieldsList.forEach(field -> values.add(labels.get(field)));
			if (labelsGeneratorInput.isBarcodeRequired()) {
				if (labelsGeneratorInput.isAutomaticBarcode()) {
					values.add(labels.get(defaultBarcodeKey));
				} else {
					StringBuffer barcode = new StringBuffer();
					for (String barcodeField : labelsGeneratorInput.getBarcodeFields()) {
						if (StringUtils.isEmpty(barcode.toString())) {
							barcode.append(labels.get(barcodeField));
							continue;
						}
						barcode.append(" | ").append(labels.get(barcodeField));
					}
					values.add(barcode.toString());
				}
			}
		}
		return values.toArray(new String[] {});
	}

	protected List<String> getHeaderNames(final LabelsGeneratorInput labelsGeneratorInput, final Map<String, Field> combinedKeyFieldMap) {
		final List<String> headerNames = new LinkedList<>();
		for (final List<String> headers : labelsGeneratorInput.getFields()) {
			headers.forEach(header -> headerNames.add(combinedKeyFieldMap.get(header).getName()));
		}
		if (labelsGeneratorInput.isBarcodeRequired()) {
			headerNames.add(BARCODE);
		}
		return headerNames;
	}

}
