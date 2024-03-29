package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeData;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.Util;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DatasetCSVGenerator implements DatasetFileGenerator {

	private final static List<VariableType> ENVIRONMENT_VARIABLES_VARIABLE_TYPES =
		Arrays.asList(VariableType.ENVIRONMENT_DETAIL, VariableType.ENVIRONMENT_CONDITION);

	@Override
	public File generateSingleInstanceFile(final Integer studyId, final DatasetDTO dataSetDto, final List<MeasurementVariable> columns,
		final List<ObservationUnitRow> observationUnitRows,
		final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
		final String fileNameFullPath, final StudyInstance studyInstance) throws IOException {
		try (final CSVWriter csvWriter = new CSVWriter(
			new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',')) {

			final File newFile = new File(fileNameFullPath);
			// feed in your array (or convert your data to an array)
			final List<String[]> rowValues = new ArrayList<>();

			rowValues.add(this.getHeaderNames(columns).toArray(new String[] {}));
			if (!observationUnitRows.isEmpty()) {
				final Map<String, Map<String, String>> studyAndEnvironmentCategoricalValuesMap =
					this.getStudyAndEnvironmentCategoricalValuesMap(columns);
				for (final ObservationUnitRow row : observationUnitRows) {
					final List<SampleGenotypeDTO> sampleGenotypeDtoList = genotypeDTORowMap.getOrDefault(row.getObservationUnitId(), new ArrayList<>());
					rowValues.add(this.getColumnValues(row, sampleGenotypeDtoList, columns, studyAndEnvironmentCategoricalValuesMap));
				}
			}

			csvWriter.writeAll(rowValues);
			return newFile;
		}
	}

	@Override
	public File generateTraitAndSelectionVariablesFile(final List<String[]> rowValues, final String filenamePath) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File generateMultiInstanceFile(final Integer studyId, final DatasetDTO datasetDTO,
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap,
		final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
		final List<MeasurementVariable> columns,
		final String fileNameFullPath) throws IOException {
		final List<ObservationUnitRow> allObservationUnitRows = new ArrayList<>();
		for (final List<ObservationUnitRow> observationUnitRows : observationUnitRowMap.values()) {
			allObservationUnitRows.addAll(observationUnitRows);
		}
		return this.generateSingleInstanceFile(null, null, columns, allObservationUnitRows, genotypeDTORowMap, fileNameFullPath, null);
	}

	Map<String, Map<String, String>> getStudyAndEnvironmentCategoricalValuesMap(final List<MeasurementVariable> columns) {
		final Map<String, Map<String, String>> categoricalValuesMap = new HashMap<>();

		final List<MeasurementVariable> studyAndEnvironmentCategoricalVariables = columns.stream()
			.filter(column -> (((ENVIRONMENT_VARIABLES_VARIABLE_TYPES
				.contains(column.getVariableType()) || VariableType.STUDY_DETAIL.equals(column.getVariableType()))
				&& DataType.CATEGORICAL_VARIABLE.getName().equals(column.getDataType()))))
			.collect(Collectors.toList());
		for (final MeasurementVariable column : studyAndEnvironmentCategoricalVariables) {
			final Map<String, String> possibleValuesMap = new HashMap<>();
			for (final ValueReference possibleValue : column.getPossibleValues()) {
				possibleValuesMap.put(possibleValue.getId().toString(), possibleValue.getName());
			}
			categoricalValuesMap.put(column.getName(), possibleValuesMap);

		}
		return categoricalValuesMap;
	}

	String[] getColumnValues(final ObservationUnitRow row,
		final List<SampleGenotypeDTO> sampleGenotypeDtoList,
		final List<MeasurementVariable> subObservationSetColumns,
		final Map<String, Map<String, String>> studyAndEnvironmentCategoricalValuesMap) {
		final List<String> values = new LinkedList<>();
		for (final MeasurementVariable column : subObservationSetColumns) {
			if (!Util.isNullOrEmpty(Util.getObservationUnitData(row.getEnvironmentVariables(), column))
				&& ENVIRONMENT_VARIABLES_VARIABLE_TYPES
				.contains(column.getVariableType())) {
				this.getValue(studyAndEnvironmentCategoricalValuesMap, values, column,
					Util.getObservationUnitData(row.getEnvironmentVariables(), column).getValue());
			} else if (!Util.isNullOrEmpty(Util.getObservationUnitData(row.getVariables(), column)) && VariableType.STUDY_DETAIL.equals(
				column.getVariableType())) {
				this.getValue(studyAndEnvironmentCategoricalValuesMap, values, column,
					Util.getObservationUnitData(row.getVariables(), column).getValue());
			} else if (!Util.isNullOrEmpty(Util.getObservationUnitData(row.getVariables(), column))) {
				values.add(Util.getObservationUnitData(row.getVariables(), column).getValue());
			} else if (column.getVariableType().equals(VariableType.GENOTYPE_MARKER)) {
				if (CollectionUtils.isNotEmpty(sampleGenotypeDtoList)) {
					// If the observation unit has multiple samples associated to it,
					// Concatenate the sample genotype values of the samples (delimited by ";")
					final String genotypeValue =
						sampleGenotypeDtoList.stream()
							.map(genotypeDTO -> genotypeDTO.getGenotypeDataMap().getOrDefault(column.getName(), new SampleGenotypeData())
								.getValue())
							.filter(
								Objects::nonNull).collect(Collectors.joining(";"));
					values.add(genotypeValue);
				} else {
					values.add(StringUtils.EMPTY);
				}
			}

		}
		return values.toArray(new String[] {});
	}

	private void getValue(final Map<String, Map<String, String>> categoricalValuesMap, final List<String> values,
		final MeasurementVariable column, final String value) {
		final String key = categoricalValuesMap.containsKey(column.getName()) ? column.getName() : column.getAlias();
		if (categoricalValuesMap.containsKey(key) && categoricalValuesMap.get(key).get(value) != null) {
			values.add(categoricalValuesMap.get(key).get(value));
		} else {
			values.add(value);
		}
	}

	List<String> getHeaderNames(final List<MeasurementVariable> subObservationSetColumns) {
		final List<String> headerNames = new LinkedList<>();
		for (final MeasurementVariable measurementVariable : subObservationSetColumns) {
			headerNames.add(
				StringUtils.isNotEmpty(measurementVariable.getAlias()) ? measurementVariable.getAlias() : measurementVariable.getName());
		}
		return headerNames;
	}

}
