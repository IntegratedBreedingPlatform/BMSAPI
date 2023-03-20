package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class DatasetKsuCSVGenerator extends DatasetCSVGenerator implements DatasetFileGenerator {

	@Override
	public File generateMultiInstanceFile(final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap,
		final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
		final List<MeasurementVariable> columns,
		final String fileNameFullPath) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File generateTraitAndSelectionVariablesFile(final List<String[]> rowValues, final String filenamePath) throws IOException{
		try (final CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(filenamePath), StandardCharsets.UTF_8), ',')){
			final File newFile = new File(filenamePath);
			csvWriter.writeAll(rowValues);
			return newFile;
		}
	}
}
