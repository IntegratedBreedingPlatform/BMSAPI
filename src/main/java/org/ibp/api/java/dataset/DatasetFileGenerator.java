package org.ibp.api.java.dataset;

import com.google.common.collect.ImmutableMap;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.rest.dataset.ObservationUnitRow;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface DatasetFileGenerator {
	List<String> TRAIT_FILE_HEADERS = Arrays.asList("trait", "format", "defaultValue", "minimum",
		"maximum", "details", "categories", "isVisible", "realPosition");

	List<Integer> DATA_TYPE_LIST = Arrays.asList(
		TermId.NUMERIC_VARIABLE.getId(),
		TermId.CATEGORICAL_VARIABLE.getId(), TermId.DATE_VARIABLE.getId(), TermId.CHARACTER_VARIABLE.getId());

	ImmutableMap<Integer, String> DATA_TYPE_FORMATS = ImmutableMap.<Integer, String> builder()
		.put(TermId.CATEGORICAL_VARIABLE.getId(), "categorical").put(TermId.NUMERIC_VARIABLE.getId(), "numeric")
		.put(TermId.DATE_VARIABLE.getId(), "date").put(TermId.CHARACTER_VARIABLE.getId(), "text")
		.put(0, "unrecognized").build();

	File generateSingleInstanceFile(Integer studyId, DatasetDTO dataSetDto, List<MeasurementVariable> columns,
		List<ObservationUnitRow> observationUnitRows,
		String fileNamePath) throws IOException;

	File generateMultiInstanceFile(Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, List<MeasurementVariable> columns,
		String fileNameFullPath) throws IOException;

	File generateTraitAndSelectionVariablesFile(List<String[]> rowValues, String filenamePath) throws IOException;
}
