package org.ibp.api.java.impl.middleware.dataset;

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
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class DatasetXLSGenerator {

	private static final String NUMERIC_DATA_TYPE = "NUMERIC_DATA_TYPE ";
	private static final int PIXEL_SIZE = 250;

	@Resource
	private MessageSource messageSource;

	@Resource
	private StudyDataManager studyDataManager;

	protected File generateXLSFile(final Integer studyId,
		final List<MeasurementVariable> columns, final List<ObservationUnitRow> reorderedObservationUnitRows,
		final String fileNamePath) throws IOException {
		final File newFile = new File(fileNamePath);
		FileOutputStream fos = null;
		final HSSFWorkbook xlsBook = new HSSFWorkbook();
		final HSSFSheet xlsSheet = xlsBook.createSheet("Observation");
		this.writeDescriptionSheet(xlsBook, studyId);
		this.writeObservationHeader(xlsBook, xlsSheet, columns);
		int currentRowNum = 1;
		for (final ObservationUnitRow dataRow : reorderedObservationUnitRows) {
			this.writeObservationRow(currentRowNum++, xlsSheet, dataRow, columns);
		}

		try {
			final File file = new File(fileNamePath);
			fos = new FileOutputStream(file);
			xlsBook.write(fos);

		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		return newFile;
	}

	private void writeObservationRow(
		final int currentRowNum, final HSSFSheet xlsSheet, final ObservationUnitRow dataRow,
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

	private void writeObservationHeader(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
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

	private void writeDescriptionSheet(final HSSFWorkbook xlsBook, final Integer studyId) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet = xlsBook.createSheet("Description");
		int currentRowNum = 0;

		final StudyDetails studyDetails = studyDataManager.getStudyDetails(studyId);
		currentRowNum = this.writeStudyDetails(currentRowNum, xlsBook, xlsSheet, studyDetails);
		xlsSheet.createRow(currentRowNum++);

		xlsSheet.setColumnWidth(0, 20 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(1, 24 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(2, 30 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(3, 18 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(4, 18 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(5, 15 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(6, 20 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(7, 20 * PIXEL_SIZE);
	}

	private int writeStudyDetails(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
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
			studyDetails.getStudyType().getName());

		return rowNumIndex;
	}

	private void writeStudyDetailRow(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum, final String label,
		final String value) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		HSSFCell cell = row.createCell(0, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, 153, 51, 0));
		cell.setCellValue(this.messageSource.getMessage(label, null, locale));
		cell = row.createCell(1, CellType.STRING);
		cell.setCellValue(value);
	}

}
