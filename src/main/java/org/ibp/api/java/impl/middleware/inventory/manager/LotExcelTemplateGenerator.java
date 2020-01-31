package org.ibp.api.java.impl.middleware.inventory.manager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class LotExcelTemplateGenerator {

	private static final int LOTS_SHEET_GID_COLUMN_INDEX = 0;
	private static final int LOTS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX = 1;
	private static final int LOTS_SHEET_UNITS_COLUMN_INDEX = 2;
	private static final int LOTS_SHEET_AMOUNT_COLUMN_INDEX = 3;
	private static final int LOTS_SHEET_STOCK_ID_COLUMN_INDEX = 4;
	private static final int LOTS_SHEET_NOTES_COLUMN_INDEX = 5;

	private static final int CODES_SHEET_FIRST_COLUMN_INDEX = 0;
	private static final int CODES_SHEET_SECOND_COLUMN_INDEX = 1;

	@Autowired
	ResourceBundleMessageSource messageSource;

	public File generateTemplateFile(final String fileNamePath, final List<LocationDto> locations, final List<VariableDetails> units)
		throws IOException {
		final HSSFWorkbook xlsBook = new HSSFWorkbook();

		final File file = new File(fileNamePath);
		this.writeLotsSheet(xlsBook);
		this.writeCodesSheet(xlsBook, locations, units);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			xlsBook.write(fos);

		}
		return file;
	}

	private void writeCodesSheet(final HSSFWorkbook xlsBook, final List<LocationDto> locations, final List<VariableDetails> units) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet =
			xlsBook.createSheet(this.messageSource.getMessage("export.inventory.manager.lot.template.sheet.codes", null, locale));
		int currentRowNum = 0;

		this.writeLocationHeader(xlsBook, xlsSheet, currentRowNum++);
		currentRowNum = this.writeLocationSection(currentRowNum, xlsBook, xlsSheet, locations);
		xlsSheet.createRow(currentRowNum++);

		this.writeUnitsHeader(xlsBook, xlsSheet, currentRowNum++);
		currentRowNum = this.writeUnitsSection(currentRowNum, xlsBook, xlsSheet, units);
		xlsSheet.createRow(currentRowNum++);
		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.CODES_SHEET_FIRST_COLUMN_INDEX, 34 * 250);
		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.CODES_SHEET_SECOND_COLUMN_INDEX, 65 * 250);

	}

	private int writeLocationSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final List<LocationDto> locations) {
		final CellStyle backgroundStyle = xlsBook.createCellStyle();
		final HSSFFont blackFont = xlsBook.createFont();
		blackFont.setFontName("calibri");
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		blackFont.setFontHeightInPoints((short) 11);
		backgroundStyle.setFont(blackFont);
		
		int rowNumIndex = currentRowNum;
		int count = locations.size();
		for (LocationDto locationDto : locations) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(LotExcelTemplateGenerator.CODES_SHEET_FIRST_COLUMN_INDEX, locationDto.getAbbreviation(), count, xlsBook,row);
			this.writeCell(LotExcelTemplateGenerator.CODES_SHEET_SECOND_COLUMN_INDEX, locationDto.getName(), count, xlsBook,row);
			count--;
		}
		return rowNumIndex;
	}

	private int writeUnitsSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final List<VariableDetails> units) {
		final CellStyle backgroundStyle = xlsBook.createCellStyle();
		final HSSFFont blackFont = xlsBook.createFont();
		blackFont.setFontName("calibri");
		blackFont.setFontHeightInPoints((short) 11);
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		backgroundStyle.setFont(blackFont);

		int rowNumIndex = currentRowNum;
		int count = units.size();

		for (VariableDetails variableDetail : units) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(LotExcelTemplateGenerator.CODES_SHEET_FIRST_COLUMN_INDEX, variableDetail.getName(), count, xlsBook,row);
			this.writeCell(LotExcelTemplateGenerator.CODES_SHEET_SECOND_COLUMN_INDEX, variableDetail.getDescription(), count, xlsBook,row);
			count--;
		}

		return rowNumIndex;
	}

	private void writeCell(final int codesSheetFirstColumnIndex, final String value, final int count, final HSSFWorkbook xlsBook, final HSSFRow row) {
		HSSFCell cell = row.createCell(codesSheetFirstColumnIndex, CellType.STRING);
		cell.setCellStyle(this.getcellStyle(
			xlsBook,
			codesSheetFirstColumnIndex == 0 ? this.getColorIndex(xlsBook, 127, 196, 249) : this.getColorIndex(xlsBook, 181, 241, 126)));
		cell.setCellValue(value);
		cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
		cell.getCellStyle().setBorderRight(BorderStyle.THIN);
		if (count == 1) {
			cell.getCellStyle().setBorderBottom(BorderStyle.THIN);
		}
	}


	private void writeLotsSheet(final HSSFWorkbook xlsBook) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet =
			xlsBook.createSheet(this.messageSource.getMessage("export.inventory.manager.lot.template.sheet.lots", null, locale));
		int currentRowNum = 0;

		this.writeLotsHeader(xlsBook, xlsSheet, currentRowNum++);
		xlsSheet.createRow(currentRowNum++);
	}

	private void writeLotsHeader(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		row.setHeightInPoints(16);
		HSSFCell cell = row.createCell(LotExcelTemplateGenerator.LOTS_SHEET_GID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 241, 196, 114)));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.gid.column", null, locale));

		cell = row.createCell(LotExcelTemplateGenerator.LOTS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 127, 196, 249)));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.lot.template.storage.location.abbr.column", null, locale));

		cell = row.createCell(LotExcelTemplateGenerator.LOTS_SHEET_UNITS_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 127, 196, 249)));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.units.column", null, locale));

		cell = row.createCell(LotExcelTemplateGenerator.LOTS_SHEET_AMOUNT_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 127, 196, 249)));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.amount.column", null, locale));

		cell = row.createCell(LotExcelTemplateGenerator.LOTS_SHEET_STOCK_ID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 127, 196, 249)));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.stock.id.column", null, locale));

		cell = row.createCell(LotExcelTemplateGenerator.LOTS_SHEET_NOTES_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 255, 233, 131)));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.notes.column", null, locale));

		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.LOTS_SHEET_GID_COLUMN_INDEX, 8 * 250);
		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.LOTS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, 34 * 250);
		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.LOTS_SHEET_UNITS_COLUMN_INDEX, 10 * 250);
		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.LOTS_SHEET_AMOUNT_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.LOTS_SHEET_STOCK_ID_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(LotExcelTemplateGenerator.LOTS_SHEET_NOTES_COLUMN_INDEX, 10 * 250);
	}

	private void writeLocationHeader(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		row.setHeightInPoints(16);

		HSSFCell cell = row.createCell(LotExcelTemplateGenerator.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 127, 196, 249)));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.lot.template.storage.location.abbr.column", null, locale));

		cell = row.createCell(LotExcelTemplateGenerator.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook,  181,  241,126)));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.lot.template.storage.location.name.column", null, locale));

	}

	private void writeUnitsHeader(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		row.setHeightInPoints(16);

		HSSFCell cell = row.createCell(LotExcelTemplateGenerator.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 127, 196, 249)));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.units.column", null, locale));

		cell = row.createCell(LotExcelTemplateGenerator.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, this.getColorIndex(xlsBook, 181,  241,126)));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.units.description.column", null, locale));

	}

	private CellStyle getHeaderStyle(final HSSFWorkbook xlsBook, final short colorIndex) {
		final HSSFFont blackFont = xlsBook.createFont();
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		blackFont.setFontName("calibri");
		blackFont.setFontHeightInPoints((short) 11);
		blackFont.setBold(true);

		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(blackFont);

		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);

		return cellStyle;
	}

	private CellStyle getcellStyle(final HSSFWorkbook xlsBook, final short colorIndex) {
		final HSSFFont blackFont = xlsBook.createFont();
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		blackFont.setFontName("calibri");
		blackFont.setFontHeightInPoints((short) 11);
		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(blackFont);
		return cellStyle;
	}

	private short getColorIndex(final HSSFWorkbook xlsBook, final int c1, final int c2, final int c3) {
		final HSSFPalette palette = xlsBook.getCustomPalette();
		final HSSFColor color = palette.findSimilarColor(c1, c2, c3);
		return color.getIndex();
	}

}
