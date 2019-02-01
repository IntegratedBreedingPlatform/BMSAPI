package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.rest.dataset.ObservationUnitRow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DatasetFileGenerator {

	File generateSingleInstanceFile(final Integer studyId, final DatasetDTO dataSetDto, final List<MeasurementVariable> columns,
		final List<ObservationUnitRow> observationUnitRows,
		final String fileNamePath) throws IOException;

	File generateMultiInstanceFile(Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, List<MeasurementVariable> columns,
		String fileNameFullPath) throws IOException;
}
