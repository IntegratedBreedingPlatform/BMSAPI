package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.genotype.SampleGenotypeService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeData;
import org.generationcp.middleware.domain.genotype.SampleGenotypeVariablesSearchFilter;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.Util;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DatasetExcelGenerator implements DatasetFileGenerator {

	private static final String NUMERIC_DATA_TYPE = "Numeric";
	private static final int PIXEL_SIZE = 250;
	private static final int VARIABLE_NAME_COLUMN_INDEX = 0;
	private static final int DESCRIPTION_COLUMN_INDEX = 1;
	private static final int ONTOLOGY_ID_COLUMN_INDEX = 2;
	private static final int PROPERTY_COLUMN_INDEX = 3;
	private static final int SCALE_COLUMN_INDEX = 4;
	private static final int METHOD_COLUMN_INDEX = 5;
	private static final int DATATYPE_COLUMN_INDEX = 6;
	private static final int VARIABLE_VALUE_COLUMN_INDEX = 7;
	private static final int DATASET_COLUMN_INDEX = 8;
	private static final String MAX_ONLY = " and below";
	private static final String MIN_ONLY = " and above";
	private static final String NO_RANGE = "All values allowed";
	private static final String POSSIBLE_VALUES_AS_STRING_DELIMITER = "/";
	private static final String STUDY = "STUDY";
	private static final String ENVIRONMENT = "ENVIRONMENT";
	private static final String PLOT = "PLOT";
	private static final String BREEDING_METHOD_PROPERTY_NAME = "";

	@Autowired
	ResourceBundleMessageSource messageSource;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetServiceMiddlewareService;

	@Resource
	private DatasetTypeService datasetTypeService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private SampleGenotypeService sampleGenotypeService;

	private boolean includeSampleGenotypeValues;

	@Override
	public File generateSingleInstanceFile(
		final Integer studyId,
		final DatasetDTO dataSetDto, final List<MeasurementVariable> columns,
		final List<ObservationUnitRow> reorderedObservationUnitRows,
		final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
		final String fileNamePath, final StudyInstance studyInstance) throws IOException {
		final HSSFWorkbook xlsBook = new HSSFWorkbook();

		final List<MeasurementVariable> orderedColumns = this.orderColumns(columns);
		this.writeDescriptionSheet(xlsBook, studyId, dataSetDto, studyInstance, false);
		final Locale locale = LocaleContextHolder.getLocale();
		this.writeObservationSheet(
			orderedColumns, reorderedObservationUnitRows, genotypeDTORowMap, xlsBook,
			this.messageSource.getMessage("export.study.sheet.observation", null, locale));

		final File file = new File(fileNamePath);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			xlsBook.write(fos);

		}
		return file;
	}

	@Override
	public File generateMultiInstanceFile(final Integer studyId, final DatasetDTO datasetDTO,
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap,
		final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
		final List<MeasurementVariable> columns,
		final String fileNameFullPath) throws IOException {
		final HSSFWorkbook xlsBook = new HSSFWorkbook();

		final List<MeasurementVariable> orderedColumns = this.orderColumns(columns);
		this.writeDescriptionSheet(xlsBook, studyId, datasetDTO, datasetDTO.getInstances().get(0), true);
		final Locale locale = LocaleContextHolder.getLocale();
		final List<ObservationUnitRow> allObservationUnitRows = observationUnitRowMap.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		this.writeObservationSheet(
				orderedColumns, allObservationUnitRows, genotypeDTORowMap, xlsBook,
				this.messageSource.getMessage("export.study.sheet.observation", null, locale));

		final File file = new File(fileNameFullPath);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			xlsBook.write(fos);
		}
		return file;
	}

	@Override
	public File generateTraitAndSelectionVariablesFile(final List<String[]> rowValues, final String filenamePath) throws IOException {
		throw new UnsupportedOperationException();
	}

	List<MeasurementVariable> orderColumns(final List<MeasurementVariable> columns) {
		final List<MeasurementVariable> orderedColumns = new ArrayList<>();
		final List<MeasurementVariable> trait = new ArrayList<>();
		final List<MeasurementVariable> selection = new ArrayList<>();

		for (final MeasurementVariable measurementVariable : columns) {
			if (TermId.OBS_UNIT_ID.getId() == measurementVariable.getTermId()) {
				orderedColumns.add(0, measurementVariable);
			} else if (measurementVariable.getVariableType() != null && VariableType.TRAIT.getId()
				.equals(measurementVariable.getVariableType().getId())) {
				trait.add(measurementVariable);
			} else if (measurementVariable.getVariableType() != null && VariableType.SELECTION_METHOD.getId()
				.equals(measurementVariable.getVariableType().getId())) {
				selection.add(measurementVariable);
			} else {
				orderedColumns.add(measurementVariable);
			}
		}

		orderedColumns.addAll(trait);
		orderedColumns.addAll(selection);
		return orderedColumns;
	}

	void writeObservationSheet(
		final List<MeasurementVariable> columns, final List<ObservationUnitRow> reorderedObservationUnitRows,
		final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
		final HSSFWorkbook xlsBook, final String sheetName) {
		final HSSFSheet xlsSheet = xlsBook.createSheet(sheetName);
		this.writeObservationHeader(xlsBook, xlsSheet, columns);
		int currentRowNum = 1;
		for (final ObservationUnitRow dataRow : reorderedObservationUnitRows) {
			final List<SampleGenotypeDTO> sampleGenotypeDtoList =
				genotypeDTORowMap.getOrDefault(dataRow.getObservationUnitId(), new ArrayList<>());
			this.writeObservationRow(currentRowNum++, xlsSheet, dataRow, sampleGenotypeDtoList, columns);
		}
	}

	private void writeObservationRow(
		final int currentRowNum, final HSSFSheet xlsSheet, final ObservationUnitRow dataRow,
		final List<SampleGenotypeDTO> sampleGenotypeDtoList,
		final List<MeasurementVariable> columns) {

		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		int currentColNum = 0;


		for (final MeasurementVariable column : columns) {
			ObservationUnitData observationUnitData = Util.getObservationUnitData(dataRow.getVariables(), column);
			if (Util.isNullOrEmpty(observationUnitData)
				&& (VariableType.ENVIRONMENT_DETAIL.getId().equals(column.getVariableType().getId())
					|| VariableType.ENVIRONMENT_CONDITION.getId().equals(column.getVariableType().getId()))) {
				observationUnitData = Util.getObservationUnitData(dataRow.getEnvironmentVariables(), column);
			}
			if (!Util.isNullOrEmpty(observationUnitData)) {
				final String dataCell = observationUnitData.getValue();
				final HSSFCell cell = row.createCell(currentColNum++);
				if (dataCell != null) {
					if (column.getPossibleValues() != null && !column.getPossibleValues()
						.isEmpty() && column.getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
						&& column.getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId() && !column.getProperty()
						.equals(DatasetExcelGenerator.BREEDING_METHOD_PROPERTY_NAME)) {
						cell.setCellValue(DatasetExcelGenerator.getCategoricalCellValue(dataCell, column.getPossibleValues()));
					} else if (DatasetExcelGenerator.NUMERIC_DATA_TYPE.equalsIgnoreCase(column.getDataType())) {
						if (!dataCell.isEmpty() && NumberUtils.isNumber(dataCell)) {
							cell.setCellType(CellType.BLANK);
							cell.setCellType(CellType.NUMERIC);
							cell.setCellValue(Double.valueOf(dataCell));
						}
					} else {
						cell.setCellType(CellType.STRING);
						cell.setCellValue(dataCell);
					}
				}
			}

			if (column.getVariableType().equals(VariableType.GENOTYPE_MARKER)) {
				final HSSFCell cell = row.createCell(currentColNum++);
				cell.setCellType(CellType.STRING);
				if (CollectionUtils.isNotEmpty(sampleGenotypeDtoList)) {
					// If the observation unit has multiple samples associated to it,
					// Concatenate the sample genotype values of the samples (delimited by ";")
					final String genotypeValue =
						sampleGenotypeDtoList.stream()
							.map(genotypeDTO -> genotypeDTO.getGenotypeDataMap().getOrDefault(column.getName(), new SampleGenotypeData())
								.getValue())
							.filter(
								Objects::nonNull).collect(Collectors.joining(";"));

					cell.setCellValue(genotypeValue);
				} else {
					cell.setCellValue(StringUtils.EMPTY);
				}
			}
		}
	}

	private void writeObservationHeader(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<MeasurementVariable> variables) {
		if (variables != null && !variables.isEmpty()) {
			int currentColNum = 0;
			final HSSFRow row = xlsSheet.createRow(0);
			for (final MeasurementVariable variable : variables) {
				final HSSFCell cell = row.createCell(currentColNum++);
				cell.setCellStyle(this.getObservationHeaderStyle(variable.isFactor(), xlsBook));
				cell.setCellValue(StringUtils.isNotEmpty(variable.getAlias()) ? variable.getAlias() : variable.getName());
			}
		}
	}

	private CellStyle getObservationHeaderStyle(final boolean isFactor, final HSSFWorkbook xlsBook) {
		final CellStyle style;
		if (isFactor) {
			style = this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 51, 153, 102));
		} else {
			style = this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 51, 51, 153));
		}
		return style;
	}

	private CellStyle getHeaderStyle(final HSSFWorkbook xlsBook, final short colorIndex) {
		final HSSFFont whiteFont = xlsBook.createFont();
		whiteFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());

		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(whiteFont);

		return cellStyle;
	}

	private void writeDescriptionSheet(
		final HSSFWorkbook xlsBook, final Integer studyId, final DatasetDTO dataSetDto, final StudyInstance studyInstance,
		final boolean excludeVariableValues) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet = xlsBook.createSheet(this.messageSource.getMessage("export.study.sheet.description", null, locale));
		int currentRowNum = 0;

		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(dataSetDto.getDatasetTypeId());

		final StudyDetails studyDetails = this.studyDataManager.getStudyDetails(studyId);
		final List<MeasurementVariable> studyDetailsVariables =
			this.datasetService.getMeasurementVariables(studyId, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));

		final int environmentDatasetId =
			this.studyDataManager.getDataSetsByType(studyId, DatasetTypeEnum.SUMMARY_DATA.getId()).get(0).getId();
		final int plotDatasetId;
		if (dataSetDto.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			plotDatasetId = dataSetDto.getDatasetId();
		} else {
			plotDatasetId = dataSetDto.getParentDatasetId();
		}

		final List<MeasurementVariable> environmentVariables =
			this.datasetService
				.getMeasurementVariables(
					environmentDatasetId, Lists
						.newArrayList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
							VariableType.ENVIRONMENT_CONDITION.getId()));

		final List<MeasurementVariable> plotVariables =
			this.datasetService.getMeasurementVariables(plotDatasetId, Lists
				.newArrayList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.TREATMENT_FACTOR.getId(),
					VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.ENTRY_DETAIL.getId(), VariableType.GERMPLASM_ATTRIBUTE.getId(),
					VariableType.GERMPLASM_PASSPORT.getId()));

		final List<MeasurementVariable> datasetVariables = this.datasetService
			.getMeasurementVariables(dataSetDto.getDatasetId(), Lists
				.newArrayList(VariableType.OBSERVATION_UNIT.getId(), VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId(), VariableType.ANALYSIS_SUMMARY.getId()));

		final SampleGenotypeVariablesSearchFilter filter = new SampleGenotypeVariablesSearchFilter();
		filter.setStudyId(studyId);
		filter.setDatasetIds(Arrays.asList(dataSetDto.getDatasetId()));
		final List<MeasurementVariable> genotypeMarkerVariables =
			new ArrayList<>(this.sampleGenotypeService.getSampleGenotypeVariables(filter).values());

		final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs = this.datasetServiceMiddlewareService.getDatasetNameTypes(plotDatasetId);

		currentRowNum = this.writeStudyDetails(currentRowNum, xlsBook, xlsSheet, studyDetails);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.study.details",
			this.getColorIndex(xlsBook, 153, 51, 0));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			studyDetailsVariables, STUDY, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.experimental.design",
			this.getColorIndex(xlsBook, 124, 124, 124));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(environmentVariables, VariableType.EXPERIMENTAL_DESIGN),
			ENVIRONMENT, excludeVariableValues);

		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(plotVariables, VariableType.EXPERIMENTAL_DESIGN), PLOT, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.environment.details",
			this.getColorIndex(xlsBook, 124, 124, 124));

		final List<MeasurementVariable> environmentDetails =
			this.getEnvironmentalDetails(environmentDatasetId, environmentVariables, studyInstance);

		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			environmentDetails, ENVIRONMENT, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.environmental.conditions",
			this.getColorIndex(xlsBook, 124, 124, 124));

		final List<MeasurementVariable> environmentConditions =
			this.getEnvironmentalConditions(environmentDatasetId, environmentVariables, studyInstance);

		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(environmentConditions, VariableType.ENVIRONMENT_CONDITION), ENVIRONMENT, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.germplasm.descriptors",
			this.getColorIndex(xlsBook, 51, 153, 102));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(plotVariables, VariableType.GERMPLASM_DESCRIPTOR), PLOT, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		this.createNameTypeHeader(xlsBook, xlsSheet, currentRowNum++, "export.study.description.column.name.type",
			this.getColorIndex(xlsBook, 51, 153, 102));
		currentRowNum = this.writeNameTypeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			germplasmNameTypeDTOs);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.germplasm.passports",
			this.getColorIndex(xlsBook, 51, 153, 102));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(plotVariables, VariableType.GERMPLASM_PASSPORT), PLOT, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.germplasm.attributes",
			this.getColorIndex(xlsBook, 51, 153, 102));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(plotVariables, VariableType.GERMPLASM_ATTRIBUTE), PLOT, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.entry.details.column.germplasm.descriptors",
			this.getColorIndex(xlsBook, 51, 143, 102));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(plotVariables, VariableType.ENTRY_DETAIL), PLOT, excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.observation.unit",
			this.getColorIndex(xlsBook, 51, 153, 102));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(datasetVariables, VariableType.OBSERVATION_UNIT),
			datasetType.getName(), excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.traits",
			this.getColorIndex(xlsBook, 51, 51, 153));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(datasetVariables, VariableType.TRAIT),
			datasetType.getName(), excludeVariableValues);
		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.selections",
			this.getColorIndex(xlsBook, 51, 51, 153));
		currentRowNum = this.writeSection(
			currentRowNum,
			xlsBook,
			xlsSheet,
			filterByVariableType(datasetVariables, VariableType.SELECTION_METHOD),
			datasetType.getName(), excludeVariableValues);

		xlsSheet.createRow(currentRowNum++);

		currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.analysis.summary",
				this.getColorIndex(xlsBook, 51, 51, 153));
		currentRowNum = this.writeSection(
				currentRowNum,
				xlsBook,
				xlsSheet,
				filterByVariableType(datasetVariables, VariableType.ANALYSIS_SUMMARY),
				datasetType.getName(), excludeVariableValues);

		xlsSheet.createRow(currentRowNum++);

		if (this.includeSampleGenotypeValues) {
			currentRowNum = this.createHeader(currentRowNum, xlsBook, xlsSheet, "export.study.description.column.genotype.markers",
				this.getColorIndex(xlsBook, 51, 51, 153));
			this.writeSection(
				currentRowNum,
				xlsBook,
				xlsSheet,
				genotypeMarkerVariables,
				datasetType.getName(), excludeVariableValues);
		}

		xlsSheet.setColumnWidth(0, 20 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(1, 24 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(2, 30 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(3, 18 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(4, 18 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(5, 15 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(6, 20 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(7, 20 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(8, 20 * PIXEL_SIZE);
	}

	List<MeasurementVariable> getEnvironmentalDetails(
		final int environmentDatasetId, final List<MeasurementVariable> environmentVariables, final StudyInstance instance) {
		final List<MeasurementVariable> environmentDetails =
			filterByVariableType(environmentVariables, VariableType.ENVIRONMENT_DETAIL);
		final Map<Integer, String> geoLocationMap =
			this.studyDataManager.getGeolocationByInstanceId(environmentDatasetId, instance.getInstanceId());

		final ListIterator<MeasurementVariable> iterator = environmentDetails.listIterator();
		while (iterator.hasNext()) {
			final MeasurementVariable measurementVariable = iterator.next();
			switch (measurementVariable.getTermId()) {
				case 8170:
					measurementVariable.setValue(String.valueOf(instance.getInstanceNumber()));
					break;
				case 8190:
					// Automatically add  LOCATION_NAME variable to environment details. So that both LOCATION_ID abd LOCATION_NAME variables are present
					// in Description sheet of the exported Dataset Excel file. This is to make sure that the location name is processed properly
					// when the file is imported in Dataset Importer.
					iterator.add(this.createLocationNameVariable(measurementVariable.getAlias(), instance.getLocationName()));

					// Rename the LOCATION_ID variable appropriately
					measurementVariable.setAlias(TermId.LOCATION_ID.name());
					measurementVariable.setValue(String.valueOf(instance.getLocationId()));
					break;
				default:
					final String keyValue = geoLocationMap.get(measurementVariable.getTermId());
					if (StringUtils.isBlank(measurementVariable.getValue()) && StringUtils.isNotBlank(keyValue)) {
						measurementVariable.setValue(keyValue);
					}
			}

		}

		return environmentDetails;
	}

	List<MeasurementVariable> getEnvironmentalConditions(
		final int environmentDatasetId, final List<MeasurementVariable> environmentVariables, final StudyInstance instance) {
		final List<MeasurementVariable> environmentConditions =
			filterByVariableType(environmentVariables, VariableType.ENVIRONMENT_CONDITION);
		final Map<Integer, String> environmentConditionMap =
			this.studyDataManager.getPhenotypeByVariableId(environmentDatasetId, instance.getInstanceId());
		for (final MeasurementVariable variable : environmentConditions) {
			final String keyValue = environmentConditionMap.get(variable.getTermId());
			if (variable.getValue() == null && StringUtils.isNotBlank(keyValue)) {
				variable.setValue(keyValue);
			}
		}
		return environmentConditions;
	}

	private int writeStudyDetails(
		final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final StudyDetails studyDetails) {
		int rowNumIndex = currentRowNum;
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.study",
			studyDetails.getStudyName() != null ? HtmlUtils.htmlUnescape(studyDetails.getStudyName()) : "");
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.title",
			studyDetails.getDescription() != null ? HtmlUtils.htmlUnescape(studyDetails.getDescription()) : "");
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.objective",
			studyDetails.getObjective() != null ? HtmlUtils.htmlUnescape(studyDetails.getObjective()) : "");

		String startDate = studyDetails.getStartDate();
		String endDate = studyDetails.getEndDate();

		if (startDate != null) {
			startDate = startDate.replace("-", "");
		}

		if (endDate != null) {
			endDate = endDate.replace("-", "");
		}

		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.startdate", startDate);
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.enddate", endDate);
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.studytype",
			studyDetails.getStudyType().getLabel());

		return rowNumIndex;
	}

	private void createNameTypeHeader(
			final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum, final String typeLabel, final short color) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(VARIABLE_NAME_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage(typeLabel, null, locale));

		cell = row.createCell(DESCRIPTION_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.name", null, locale));

		cell = row.createCell(ONTOLOGY_ID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.description", null, locale));
	}

	private void writeSectionHeader(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum, final String typeLabel, final short color) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(VARIABLE_NAME_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage(typeLabel, null, locale));

		cell = row.createCell(DESCRIPTION_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.description", null, locale));

		cell = row.createCell(ONTOLOGY_ID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.ontology.id", null, locale));

		cell = row.createCell(PROPERTY_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.property", null, locale));

		cell = row.createCell(SCALE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.scale", null, locale));

		cell = row.createCell(METHOD_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.method", null, locale));

		cell = row.createCell(DATATYPE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.datatype", null, locale));

		cell = row.createCell(VARIABLE_VALUE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.value", null, locale));

		cell = row.createCell(DATASET_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, color));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.dataset", null, locale));
	}

	private int writeSection(
		final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<MeasurementVariable> variables, final String datasetColumn, final boolean excludeVariableValue) {

		final CellStyle backgroundStyle = xlsBook.createCellStyle();
		final HSSFFont blackFont = xlsBook.createFont();
		backgroundStyle.setFillBackgroundColor(this.getColorIndex(xlsBook, 231, 230, 230));
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		backgroundStyle.setFont(blackFont);
		int rowNumIndex = currentRowNum;
		if (variables != null && !variables.isEmpty()) {
			for (final MeasurementVariable variable : variables) {
				final String cropOntologyId = variable.getCropOntology();

				this.writeSectionRow(
					rowNumIndex++, xlsSheet, variable, datasetColumn,
					cropOntologyId, backgroundStyle, excludeVariableValue);
			}
		}
		return rowNumIndex;
	}

	private int writeNameTypeSection(
		final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs) {

		final CellStyle backgroundStyle = xlsBook.createCellStyle();
		final HSSFFont blackFont = xlsBook.createFont();
		backgroundStyle.setFillBackgroundColor(this.getColorIndex(xlsBook, 231, 230, 230));
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		backgroundStyle.setFont(blackFont);
		int rowNumIndex = currentRowNum;
		if (germplasmNameTypeDTOs != null && !germplasmNameTypeDTOs.isEmpty()) {
			for (final GermplasmNameTypeDTO germplasmNameTypeDTO : germplasmNameTypeDTOs) {
				final HSSFRow row = xlsSheet.createRow(rowNumIndex++);

				HSSFCell cell = row.createCell(VARIABLE_NAME_COLUMN_INDEX, CellType.STRING);
				cell.setCellValue(germplasmNameTypeDTO.getCode());
				cell.setCellStyle(backgroundStyle);

				cell = row.createCell(DESCRIPTION_COLUMN_INDEX, CellType.STRING);
				cell.setCellValue(germplasmNameTypeDTO.getName());
				cell.setCellStyle(backgroundStyle);

				cell = row.createCell(ONTOLOGY_ID_COLUMN_INDEX, CellType.STRING);
				cell.setCellValue(germplasmNameTypeDTO.getDescription());
				cell.setCellStyle(backgroundStyle);
			}
		}
		return rowNumIndex;
	}

	private int createHeader(
		final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final String sectionLabel, final short headerColor) {

		int rowNumIndex = currentRowNum;
		this.writeSectionHeader(xlsBook, xlsSheet, rowNumIndex++, sectionLabel, headerColor);
		return rowNumIndex;
	}

	private void writeSectionRow(
		final int currentRowNum, final HSSFSheet xlsSheet, final MeasurementVariable measurementVariable,
		final String datasetColumn, final String ontologyId, final CellStyle backgroundStyle, final boolean excludeVariableValue) {
		{
			final HSSFRow row = xlsSheet.createRow(currentRowNum);

			HSSFCell cell = row.createCell(VARIABLE_NAME_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(measurementVariable.getAlias());
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(DESCRIPTION_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(measurementVariable.getDescription());
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(ONTOLOGY_ID_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(ontologyId);
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(PROPERTY_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(measurementVariable.getProperty());
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(SCALE_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(measurementVariable.getScale());
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(METHOD_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(measurementVariable.getMethod());
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(DATATYPE_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(measurementVariable.getDataTypeCode());
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(VARIABLE_VALUE_COLUMN_INDEX, CellType.STRING);
			if (!excludeVariableValue) {
				this.setContentOfVariableValueColumn(cell, measurementVariable);
			} else {
				cell.setCellValue("");
			}
			cell.setCellStyle(backgroundStyle);

			cell = row.createCell(DATASET_COLUMN_INDEX, CellType.STRING);
			cell.setCellValue(datasetColumn);
			cell.setCellStyle(backgroundStyle);
		}
	}

	private void setContentOfVariableValueColumn(final HSSFCell cell, final MeasurementVariable measurementVariable) {

		if (StringUtils.isBlank(measurementVariable.getValue()) && (measurementVariable.getVariableType() == VariableType.TRAIT
			|| (measurementVariable.getRole() != null && measurementVariable.getRole().equals(PhenotypicType.VARIATE)))) {
			/**If the variable is a 'Trait' then the VALUE column in Description sheet will be:
			 for numerical variables: we will see the Min and Max values (if any) separated by a dash "-", e.g.: 30 - 100 (we should allow decimal values too, e.g.: 0.50 - 23.09)
			 for categorical variables: we will
			 see the Categories values separated by a slash "/", e.g.: 1/2/3/4/5
			 for date variables: will remain empty
			 for character/text variables: will remain empty
			 **/
			cell.setCellValue(this.getPossibleValueDetailAsStringBasedOnDataType(measurementVariable));
		} else {
			this.setVariableValueBasedOnDataType(cell, measurementVariable);
		}
	}

	private void setVariableValueBasedOnDataType(final HSSFCell cell, final MeasurementVariable measurementVariable) {

		if (DataType.NUMERIC_VARIABLE.getId().equals(measurementVariable.getDataTypeId()) && StringUtils
			.isNotBlank(measurementVariable.getValue()) && NumberUtils.isNumber(measurementVariable.getValue())) {
			cell.setCellValue(Double.valueOf(measurementVariable.getValue()));
			cell.setCellType(CellType.NUMERIC);
		} else if (DataType.CATEGORICAL_VARIABLE.getId().equals(measurementVariable.getDataTypeId())) {
			cell.setCellValue(
				getCategoricalCellValue(measurementVariable.getValue(), measurementVariable.getPossibleValues()));
		} else {
			cell.setCellValue(measurementVariable.getValue());
		}
	}

	private static String getCategoricalCellValue(final String idValue, final List<ValueReference> possibleValues) {
		// With the New Data Table, the idValue will contain the long text instead of the id.
		if (idValue != null && possibleValues != null && !possibleValues.isEmpty()) {
			for (final ValueReference possibleValue : possibleValues) {
				if (idValue.equalsIgnoreCase(possibleValue.getDescription())) {
					return possibleValue.getName();
				}
			}
		}
		// just in case an id was passed, but this won't be the case most of the time
		if (NumberUtils.isNumber(idValue)) {
			for (final ValueReference ref : possibleValues) {
				// Needs to convert to double to facilitate retrieving decimal value from categorical values
				if (Double.valueOf(ref.getId()).equals(Double.valueOf(idValue))) {
					return ref.getName();
				}
			}
		}
		return idValue;
	}

	private String getPossibleValueDetailAsStringBasedOnDataType(final MeasurementVariable measurementVariable) {

		if (DataType.CATEGORICAL_VARIABLE.getId().equals(measurementVariable.getDataTypeId())) {
			return this.convertPossibleValuesToString(measurementVariable.getPossibleValues(), POSSIBLE_VALUES_AS_STRING_DELIMITER);
		} else if (DataType.NUMERIC_VARIABLE.getId().equals(measurementVariable.getDataTypeId())) {
			return this.concatenateMinMaxValueIfAvailable(measurementVariable);
		} else {
			return measurementVariable.getValue();
		}
	}

	private String convertPossibleValuesToString(final List<ValueReference> possibleValues, final String delimiter) {

		if (possibleValues == null) {
			return "";
		}

		final StringBuilder sb = new StringBuilder();
		final Iterator<ValueReference> iterator = possibleValues.iterator();
		while (iterator.hasNext()) {
			sb.append(iterator.next().getName());
			if (iterator.hasNext()) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	private String concatenateMinMaxValueIfAvailable(final MeasurementVariable measurementVariable) {

		if (measurementVariable.getMinRange() == null && measurementVariable.getMaxRange() == null) {
			return NO_RANGE;
		} else if (measurementVariable.getMaxRange() == null) {
			return measurementVariable.getMinRange().toString() + MIN_ONLY;
		} else if (measurementVariable.getMinRange() == null) {
			return measurementVariable.getMaxRange().toString() + MAX_ONLY;
		} else {
			return measurementVariable.getMinRange().toString() + " - " + measurementVariable.getMaxRange().toString();
		}
	}

	private void writeStudyDetailRow(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum, final String label,
		final String value) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		HSSFCell cell = row.createCell(0, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 153, 51, 0)));
		cell.setCellValue(this.messageSource.getMessage(label, null, locale));
		cell = row.createCell(1, CellType.STRING);
		cell.setCellValue(value);
	}

	private short getColorIndex(final HSSFWorkbook xlsBook, final int c1, final int c2, final int c3) {
		final HSSFPalette palette = xlsBook.getCustomPalette();
		final HSSFColor color = palette.findSimilarColor(c1, c2, c3);
		return color.getIndex();
	}

	private static List<MeasurementVariable> filterByVariableType(
		final List<MeasurementVariable> measurementVariables, final VariableType variableType) {
		final Collection<MeasurementVariable> variablesByType = CollectionUtils.select(measurementVariables, o -> {
			final MeasurementVariable measurementVariable = (MeasurementVariable) o;
			return measurementVariable.getVariableType().equals(variableType);
		});
		return Lists.newArrayList(variablesByType);
	}

	MeasurementVariable createLocationNameVariable(final String variableAlias, final String locationName) {
		final MeasurementVariable locationNameVariable = new MeasurementVariable();
		final StandardVariable standardVariable =
			this.ontologyDataManager.getStandardVariable(TermId.TRIAL_LOCATION.getId(), ContextHolder.getCurrentProgram());
		locationNameVariable.setAlias(variableAlias);
		locationNameVariable.setName(standardVariable.getName());
		locationNameVariable.setDescription(standardVariable.getDescription());
		locationNameVariable.setProperty(standardVariable.getProperty().getName());
		locationNameVariable.setScale(standardVariable.getScale().getName());
		locationNameVariable.setMethod(standardVariable.getMethod().getName());
		locationNameVariable.setDataType(standardVariable.getDataType().getName());
		locationNameVariable.setDataTypeId(standardVariable.getDataType().getId());
		locationNameVariable.setValue(locationName);
		locationNameVariable.setLabel(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0));
		locationNameVariable.setTermId(TermId.TRIAL_LOCATION.getId());
		locationNameVariable.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
		locationNameVariable.setVariableType(VariableType.ENVIRONMENT_DETAIL);
		return locationNameVariable;
	}

	void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setIncludeSampleGenotypeValues(final boolean includeSampleGenotypeValues) {
		this.includeSampleGenotypeValues = includeSampleGenotypeValues;
	}
}
