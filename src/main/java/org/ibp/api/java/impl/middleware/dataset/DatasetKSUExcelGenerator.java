package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class DatasetKSUExcelGenerator implements DatasetFileGenerator {

	@Override
	public File generateSingleInstanceFile(
		final Integer studyId,
		final DatasetDTO dataSetDto, final List<MeasurementVariable> columns,
		final List<ObservationUnitRow> reorderedObservationUnitRows,
		final String fileNamePath) throws IOException {
		return new File(fileNamePath);
	}

	@Override
	public File generateMultiInstanceFile(final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns,
		final String fileNameFullPath) throws IOException {
		//Do nothing. Implement for the singleFile download KSU Excel option
		return new File(fileNameFullPath);
	}

}
