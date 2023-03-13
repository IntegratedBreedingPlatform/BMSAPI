package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.GenotypeDTO;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.rest.dataset.ObservationUnitRow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DatasetFileGenerator {

	File generateSingleInstanceFile(Integer studyId, DatasetDTO dataSetDto, List<MeasurementVariable> columns,
		List<ObservationUnitRow> observationUnitRows,
		final Map<Integer, List<GenotypeDTO>> genotypeDTORowMap,
		String fileNamePath, StudyInstance studyInstance) throws IOException;

	File generateMultiInstanceFile(Map<Integer, List<ObservationUnitRow>> observationUnitRowMap,
		final Map<Integer, List<GenotypeDTO>> genotypeDTORowMap,
		List<MeasurementVariable> columns,
		String fileNameFullPath) throws IOException;

	File generateTraitAndSelectionVariablesFile(List<String[]> rowValues, String filenamePath) throws IOException;
}
