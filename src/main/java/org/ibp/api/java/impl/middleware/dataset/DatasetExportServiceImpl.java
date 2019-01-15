package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.io.Files;
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
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class DatasetExportServiceImpl implements DatasetExportService {

	private static final String NUMERIC_DATA_TYPE = "NUMERIC_DATA_TYPE ";

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private DatasetCollectionOrderService datasetCollectionOrderService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetCSVGenerator datasetCSVGenerator;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Resource
	private MessageSource messageSource;

	private ZipUtil zipUtil = new ZipUtil();

	@Override
	public File exportAsCSV(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId) {

		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.instanceValidator.validate(datasetId, instanceIds);

		final Study study = this.studyDataManager.getStudy(studyId);
		final DatasetDTO dataSet = this.datasetService.getDataset(datasetId);
		final List<StudyInstance> selectedDatasetInstances = this.getSelectedDatasetInstances(dataSet.getInstances(), instanceIds);

		try {
			return this.generateCSVFiles(study, dataSet, selectedDatasetInstances, collectionOrderId);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsCSV.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	protected File generateCSVFiles(
		final Study study, final DatasetDTO dataSetDto, final List<StudyInstance> studyInstances, final int collectionOrderId)
		throws IOException {
		final List<File> csvFiles = new ArrayList<>();

		// Get the visible variables in SubObservation table
		final List<MeasurementVariable> columns =
			this.studyDatasetService.getSubObservationSetColumns(study.getId(), dataSetDto.getDatasetId());

		final int trialDatasetId = this.studyDataManager.getDataSetsByType(study.getId(), DataSetType.SUMMARY_DATA).get(0).getId();
		final File temporaryFolder = Files.createTempDir();

		for (final StudyInstance studyInstance : studyInstances) {
			final List<ObservationUnitRow> observationUnitRows =
				this.studyDatasetService
					.getObservationUnitRows(study.getId(), dataSetDto.getDatasetId(), studyInstance.getInstanceDbId(), Integer.MAX_VALUE,
						Integer.MAX_VALUE, null,
						"");

			final DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder =
				DatasetCollectionOrderServiceImpl.CollectionOrder.findById(collectionOrderId);
			final List<ObservationUnitRow> reorderedObservationUnitRows = this.datasetCollectionOrderService
				.reorder(collectionOrder, trialDatasetId, String.valueOf(studyInstance.getInstanceNumber()), observationUnitRows);

			// Build the filename with the following format:
			// study_name + location_number + location_name +  dataset_type + dataset_name
			final String sanitizedFileName = FileUtils.sanitizeFileName(String
				.format(
					"%s_%s_%s_%s_%s.csv", study.getName(), studyInstance.getInstanceNumber(), studyInstance.getLocationName(),
					DataSetType.findById(dataSetDto.getDatasetTypeId()).name(), dataSetDto.getName()));

			final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

			final CSVWriter csvWriter =
				new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',');
			csvFiles.add(this.datasetCSVGenerator.generateCSVFile(columns, reorderedObservationUnitRows, fileNameFullPath, csvWriter));
		}

		if (csvFiles.size() == 1) {
			return csvFiles.get(0);
		} else {
			return this.zipUtil.zipFiles(study.getName(), csvFiles);
		}
	}

	protected List<StudyInstance> getSelectedDatasetInstances(final List<StudyInstance> studyInstances, final Set<Integer> instanceIds) {
		final Iterator<StudyInstance> iterator = studyInstances.iterator();
		while (iterator.hasNext()) {
			final StudyInstance studyInstance = iterator.next();
			if (!instanceIds.contains(studyInstance.getInstanceDbId())) {
				iterator.remove();
			}
		}
		return studyInstances;
	}

	@Override
	public File exportAsExcel(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId) {

		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.instanceValidator.validate(datasetId, instanceIds);

		final Study study = this.studyDataManager.getStudy(studyId);
		final DatasetDTO dataSet = this.datasetService.getDataset(datasetId);
		final List<StudyInstance> selectedDatasetInstances = this.getSelectedDatasetInstances(dataSet.getInstances(), instanceIds);

		try {
			return this.generateExcelFiles(study, dataSet, selectedDatasetInstances, collectionOrderId);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsCSV.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	protected File generateExcelFiles(
		final Study study, final DatasetDTO dataSetDto, final List<StudyInstance> studyInstances, final int collectionOrderId)
		throws IOException {
		final List<File> files = new ArrayList<>();

		// Get the visible variables in SubObservation table
		final List<MeasurementVariable> columns =
			this.studyDatasetService.getSubObservationSetColumns(study.getId(), dataSetDto.getDatasetId());

		final int trialDatasetId = this.studyDataManager.getDataSetsByType(study.getId(), DataSetType.SUMMARY_DATA).get(0).getId();
		final File temporaryFolder = Files.createTempDir();

		for (final StudyInstance studyInstance : studyInstances) {
			final List<ObservationUnitRow> observationUnitRows =
				this.studyDatasetService
					.getObservationUnitRows(study.getId(), dataSetDto.getDatasetId(), studyInstance.getInstanceDbId(), Integer.MAX_VALUE,
						Integer.MAX_VALUE, null,
						"");

			final DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder =
				DatasetCollectionOrderServiceImpl.CollectionOrder.findById(collectionOrderId);
			final List<ObservationUnitRow> reorderedObservationUnitRows = this.datasetCollectionOrderService
				.reorder(collectionOrder, trialDatasetId, String.valueOf(studyInstance.getInstanceNumber()), observationUnitRows);

			// Build the filename with the following format:
			// study_name + location_number + location_name +  dataset_type + dataset_name
			final String sanitizedFileName = FileUtils.sanitizeFileName(String
				.format(
					"%s_%s_%s_%s_%s.csv", study.getName(), studyInstance.getInstanceNumber(), studyInstance.getLocationName(),
					DataSetType.findById(dataSetDto.getDatasetTypeId()).name(), dataSetDto.getName()));

			final String fileNamePath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

			FileOutputStream fos = null;

			final HSSFWorkbook xlsBook = new HSSFWorkbook();

			final Locale locale = LocaleContextHolder.getLocale();
			final HSSFSheet xlsSheet = xlsBook.createSheet(this.messageSource.getMessage("export.dataset.sheet.observation", null, locale));
			int currentRowNum = 0;

			this.writeObservationHeader(xlsBook, xlsSheet, columns);

			for (final ObservationUnitRow dataRow : reorderedObservationUnitRows) {
				this.writeObservationRow(currentRowNum++, xlsSheet, dataRow, columns);
			}

			try {
				final File file = new File(fileNamePath);
				fos = new FileOutputStream(file);
				xlsBook.write(fos);
				files.add(file);
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}

		if (files.size() == 1) {
			return files.get(0);
		} else {
			return this.zipUtil.zipFiles(study.getName(), files);
		}
	}

	private void writeObservationRow(final int currentRowNum, final HSSFSheet xlsSheet, final ObservationUnitRow dataRow,
		final List<MeasurementVariable> columns) {

		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		int currentColNum = 0;

		for (final MeasurementVariable column : columns) {
			final ObservationUnitData observationUnitData = dataRow.getVariables().get(column.getName());

			final String dataCell = observationUnitData.getValue();
			if (dataCell != null) {
				final HSSFCell cell = row.createCell(currentColNum++);
				if (NUMERIC_DATA_TYPE.equalsIgnoreCase(column.getDataType())) {
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
	}

	private void writeObservationHeader(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<MeasurementVariable> variables) {
		if (variables != null && !variables.isEmpty()) {
			int currentColNum = 0;
			int rowNumIndex = currentColNum;
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			for (final MeasurementVariable variable : variables) {
				final HSSFCell cell = row.createCell(currentColNum++);
				cell.setCellStyle(this.getObservationHeaderStyle(variable.isFactor(), xlsBook));
				cell.setCellValue(variable.getName());
			}
		}
	}

	protected CellStyle getObservationHeaderStyle(final boolean isFactor, final HSSFWorkbook xlsBook) {
		final CellStyle style;
		if (isFactor) {
			style = this.getHeaderStyle(xlsBook, 51, 153, 102);
		} else {
			style = this.getHeaderStyle(xlsBook, 51, 51, 153);
		}
		return style;
	}

	private CellStyle getHeaderStyle(final HSSFWorkbook xlsBook, final int c1, final int c2, final int c3) {
		final HSSFPalette palette = xlsBook.getCustomPalette();
		final HSSFColor color = palette.findSimilarColor(c1, c2, c3);
		final short colorIndex = color.getIndex();

		final HSSFFont whiteFont = xlsBook.createFont();
		whiteFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());

		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(whiteFont);

		return cellStyle;
	}

	protected void setZipUtil(final ZipUtil zipUtil) {
		this.zipUtil = zipUtil;
	}
}
