package org.ibp.api.rest.labelprinting;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import liquibase.util.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.commons.util.FileUtils;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by clarysabel on 2/7/19.
 */
@Component
public class CSVLabelsFileGenerator implements LabelsFileGenerator {

	@Autowired
	private LabelPrintingStrategy subObservationDatasetLabelPrinting;

	@Override
	public File generate(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final String sanitizedFileName = FileUtils.sanitizeFileName(String
			.format("%s." + "csv", labelsGeneratorInput.getFileName()));
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;
		try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',')){

			final Set<Field> availableKeys = new HashSet<>();
			subObservationDatasetLabelPrinting.getAvailableLabelFields(labelsGeneratorInput).forEach(labelType -> availableKeys.addAll(labelType.getFields()));

			final Map<String, Field> termIdFieldMap = Maps.uniqueIndex(availableKeys, Field::getId);

			final File newFile = new File(fileNameFullPath);
			// feed in your array (or convert your data to an array)
			final List<String[]> rowValues = new ArrayList<>();

			rowValues.add(this.getHeaderNames(labelsGeneratorInput,termIdFieldMap).toArray(new String[] {}));

			for (final Map<String, String> lables : labelsData.getData()) {
				rowValues.add(this.getColumnValues(lables, labelsGeneratorInput, labelsData.getDefaultBarcodeKey()));
			}

			csvWriter.writeAll(rowValues);
			return newFile;
		}
	}

	private String[] getColumnValues(final Map<String, String> lables, final LabelsGeneratorInput labelsGeneratorInput,
		final String defaultBarcodeKey) {
		final List<String> values = new LinkedList<>();
		for (final List<String> fieldsList : labelsGeneratorInput.getFields()) {
			for (final String field : fieldsList) {
				values.add(lables.get(field));
			}
			if (labelsGeneratorInput.isBarcodeRequired()) {
				if (labelsGeneratorInput.isAutomaticBarcode()) {
					values.add(lables.get(defaultBarcodeKey));
				} else {
					String barcode = "";
					for (String barcodeField : labelsGeneratorInput.getBarcodeFields()) {
						if (StringUtils.isEmpty(barcode)) {
							barcode = lables.get(barcodeField);
						} else {
							barcode = barcode + " | " + lables.get(barcodeField);
						}

					}
					values.add(barcode);
				}
			}
		}

		return values.toArray(new String[] {});
	}

	protected List<String> getHeaderNames(final LabelsGeneratorInput labelsGeneratorInput, final Map<String, Field> termIdFieldMap) {
		final List<String> headerNames = new LinkedList<>();
		for (final List<String> headers : labelsGeneratorInput.getFields()) {
			for (final String header : headers) {
				if (NumberUtils.isNumber(header)) {
					headerNames.add(termIdFieldMap.get(header).getName());
				} else {
					headerNames.add(header);
				}
			}
		}

		if (labelsGeneratorInput.isBarcodeRequired()) {
			headerNames.add("Barcode");
		}

		return headerNames;
	}

}
