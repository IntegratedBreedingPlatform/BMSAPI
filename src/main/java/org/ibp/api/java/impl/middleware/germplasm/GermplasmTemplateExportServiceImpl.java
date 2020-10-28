package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.io.Files;
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
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.generationcp.middleware.pojos.Method;
import org.ibp.api.domain.germplasm.GermplasmName;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmTemplateExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Service
public class GermplasmTemplateExportServiceImpl implements GermplasmTemplateExportService {

	private static final String FILE_NAME = "GermplasmImportTemplate.xls";
	private static final int OBSERVATION_SHEET_ENTRY_NO_COLUMN_INDEX = 0;
	private static final int OBSERVATION_SHEET_LNAME_COLUMN_INDEX = 1;
	private static final int OBSERVATION_SHEET_DRVNM_COLUMN_INDEX = 2;
	private static final int OBSERVATION_SHEET_PREFERRED_NAME_COLUMN_INDEX = 3;
	private static final int OBSERVATION_SHEET_ENTRY_CODE_COLUMN_INDEX = 4;
	private static final int OBSERVATION_SHEET_LOCATION_ABBR_COLUMN_INDEX = 5;
	private static final int OBSERVATION_SHEET_REFERENCE_COLUMN_INDEX = 6;
	private static final int BSERVATION_SHEET_CREATION_DATE_COLUMN_INDEX = 7;
	private static final int BSERVATION_SHEET_BREEDING_METHOD_COLUMN_INDEX = 8;
	private static final int BSERVATION_SHEET_NOTES_COLUMN_INDEX = 9;
	private static final int BSERVATION_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX = 10;
	private static final int BSERVATION_SHEET_UNITS_COLUMN_INDEX = 11;
	private static final int BSERVATION_SHEET_AMOUNT_COLUMN_INDEX = 12;
	private static final int BSERVATION_SHEET_STOCK_ID_COLUMN_INDEX = 13;
	private static final int BSERVATION_SHEET_GUID_COLUMN_INDEX = 14;

	private static final int CODES_SHEET_FIRST_COLUMN_INDEX = 0;
	private static final int CODES_SHEET_SECOND_COLUMN_INDEX = 1;

	@Autowired
	ResourceBundleMessageSource messageSource;

	@Override
	public File export(final List<Method> breedingMethods, final List<GermplasmName> germplasmNames,
		final List<org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO> germplasmAttributeDTOS,
		final List<LocationDto> locationDtos, final List<LocationDto> storagelocationDtos, final List<VariableDetails> units) {

		try {
			final File temporaryFolder = Files.createTempDir();

			final String fileNameFullPath =
				temporaryFolder.getAbsolutePath() + File.separator + GermplasmTemplateExportServiceImpl.FILE_NAME;
			return this.generateTemplateFile(fileNameFullPath, breedingMethods, germplasmNames, germplasmAttributeDTOS, locationDtos,
				storagelocationDtos, units);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.germplasm.template", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	private File generateTemplateFile(final String fileNamePath, final List<Method> breedingMethods,
		final List<GermplasmName> germplasmNames,
		final List<org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO> germplasmAttributeDTOS,
		final List<LocationDto> locationDtos, final List<LocationDto> storagelocationDtos,
		final List<VariableDetails> units) throws IOException {
		final HSSFWorkbook xlsBook = new HSSFWorkbook();

		final File file = new File(fileNamePath);
		this.writeObservationSheet(xlsBook);
		this.writeCodesSheet(xlsBook, breedingMethods, germplasmNames, germplasmAttributeDTOS, locationDtos, storagelocationDtos, units);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			xlsBook.write(fos);

		}
		return file;
	}

	private void writeObservationSheet(final HSSFWorkbook xlsBook) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet =
			xlsBook.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.observation", null, locale));
		int currentRowNum = 0;

		this.writeObservationHeader(xlsBook, xlsSheet, currentRowNum++);
	}

	private void writeObservationHeader(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		row.setHeightInPoints(16);

		this.setCustomColorAtIndex(xlsBook, IndexedColors.ORANGE, 255, 102, 0);
		this.setCustomColorAtIndex(xlsBook, IndexedColors.PALE_BLUE, 197, 217, 241);
		this.setCustomColorAtIndex(xlsBook, IndexedColors.YELLOW, 255, 255, 204);
		this.setCustomColorAtIndex(xlsBook, IndexedColors.BLUE, 153, 204, 255);

		HSSFCell cell = row.createCell(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_ENTRY_NO_COLUMN_INDEX, CellType.STRING);
		final HSSFFont observationFont = this.buildFont(xlsBook, "arial", 10, true);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.entry.no.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_LNAME_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(
			this.getMessageSource().getMessage("export.germplasm.list.template.lname.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_DRVNM_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.drvnm.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_PREFERRED_NAME_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.preferred.name.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_ENTRY_CODE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.entry.code.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_LOCATION_ABBR_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.location.abbr.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_REFERENCE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.reference.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_CREATION_DATE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.creation.date.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_BREEDING_METHOD_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.breeding.method.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_NOTES_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(
			this.buildHeaderStyle(xlsBook, IndexedColors.PALE_BLUE.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.notes.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.BLUE.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.storage.location.abbr.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_UNITS_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.BLUE.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.units.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_AMOUNT_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.BLUE.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.amount.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_STOCK_ID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.BLUE.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.stock.id.column", null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_GUID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.ORANGE.getIndex(), HorizontalAlignment.CENTER, observationFont));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.guid.column", null, locale));

		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_ENTRY_NO_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_LNAME_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_DRVNM_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_PREFERRED_NAME_COLUMN_INDEX, 20 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_ENTRY_CODE_COLUMN_INDEX, 16 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_LOCATION_ABBR_COLUMN_INDEX, 20 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.OBSERVATION_SHEET_REFERENCE_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_CREATION_DATE_COLUMN_INDEX, 18 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_BREEDING_METHOD_COLUMN_INDEX, 22 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_NOTES_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, 28 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_UNITS_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_AMOUNT_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_STOCK_ID_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.BSERVATION_SHEET_GUID_COLUMN_INDEX, 13 * 250);
	}

	private void writeCodesSheet(final HSSFWorkbook xlsBook, final List<Method> breedingMethods, final List<GermplasmName> germplasmNames,
		final List<org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO> germplasmAttributeDTOS,
		final List<LocationDto> locationDtos,
		final List<LocationDto> storagelocationDtos, final List<VariableDetails> units) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet =
			xlsBook.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.codes", null, locale));
		int currentRowNum = 0;

		this.setCustomColorAtIndex(xlsBook, IndexedColors.AQUA, 218, 227, 243);
		this.setCustomColorAtIndex(xlsBook, IndexedColors.OLIVE_GREEN, 235, 241, 222);

		final CellStyle backgroundStyle = xlsBook.createCellStyle();
		final HSSFFont codesSheetFont = this.buildFont(xlsBook, "calibri", 11,false);
		backgroundStyle.setFont(codesSheetFont);

		this.writeCodesHeader(xlsBook, xlsSheet, currentRowNum++, "export.germplasm.list.template.breeding.methods.column");
		currentRowNum = this.writeBreedingMethodSection(currentRowNum, xlsBook, xlsSheet, breedingMethods);
		xlsSheet.createRow(currentRowNum++);

		this.writeCodesHeader(xlsBook, xlsSheet, currentRowNum++, "export.germplasm.list.template.attributes.column");
		currentRowNum = this.writeAttributeSection(currentRowNum, xlsBook, xlsSheet, germplasmAttributeDTOS);
		xlsSheet.createRow(currentRowNum++);

		this.writeCodesHeader(xlsBook, xlsSheet, currentRowNum++, "export.germplasm.list.template.location.abbr.column");
		currentRowNum = this.writeLocationAbbrSection(currentRowNum, xlsBook, xlsSheet, locationDtos);
		xlsSheet.createRow(currentRowNum++);

		this.writeCodesHeader(xlsBook, xlsSheet, currentRowNum++, "export.germplasm.list.template.name.column");
		currentRowNum = this.writeNameSection(currentRowNum, xlsBook, xlsSheet, germplasmNames);
		xlsSheet.createRow(currentRowNum++);

		this.writeCodesHeader(xlsBook, xlsSheet, currentRowNum++, "export.germplasm.list.template.storage.location.abbr.column");
		currentRowNum = this.writeLocationAbbrSection(currentRowNum, xlsBook, xlsSheet, storagelocationDtos);
		xlsSheet.createRow(currentRowNum++);

		this.writeCodesHeader(xlsBook, xlsSheet, currentRowNum++, "export.germplasm.list.template.units.column");
		currentRowNum = this.writeUnitsSection(currentRowNum, xlsBook, xlsSheet, units);
		xlsSheet.createRow(currentRowNum++);

		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, 34 * 250);
		xlsSheet.setColumnWidth(GermplasmTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, 65 * 250);

	}

	private int writeNameSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<GermplasmName> germplasmNames) {
		int rowNumIndex = currentRowNum;
		int count = germplasmNames.size();
		for (final GermplasmName germplasmName : germplasmNames) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, germplasmName.getNameTypeCode(), count,
				xlsBook,
				row);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, germplasmName.getName(), count, xlsBook,
				row);
			count--;
		}
		return rowNumIndex;
	}

	private int writeAttributeSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO> germplasmAttributeDTOS) {
		int rowNumIndex = currentRowNum;
		int count = germplasmAttributeDTOS.size();
		for (final org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO germplasmAttributeDTO : germplasmAttributeDTOS) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, germplasmAttributeDTO.getAttributeCode(),
				count, xlsBook,
				row);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, germplasmAttributeDTO.getAttributeName(),
				count, xlsBook, row);
			count--;
		}
		return rowNumIndex;
	}

	private int writeBreedingMethodSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<Method> breedingMethods) {
		int rowNumIndex = currentRowNum;
		int count = breedingMethods.size();
		for (final Method method : breedingMethods) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, method.getMcode(), count, xlsBook,
				row);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, method.getMname(), count, xlsBook, row);
			count--;
		}
		return rowNumIndex;
	}

	private int writeLocationAbbrSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<LocationDto> storagelocationDtos) {
		int rowNumIndex = currentRowNum;
		int count = storagelocationDtos.size();
		for (final LocationDto locationDto : storagelocationDtos) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, locationDto.getAbbreviation(), count, xlsBook,
				row);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, locationDto.getName(), count, xlsBook, row);
			count--;
		}
		return rowNumIndex;
	}

	private int writeUnitsSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
		final List<VariableDetails> units) {
		int rowNumIndex = currentRowNum;
		int count = units.size();

		for (final VariableDetails variableDetail : units) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, variableDetail.getName(), count, xlsBook,
				row);
			this.writeCell(
				GermplasmTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, variableDetail.getDescription(), count, xlsBook, row);
			count--;
		}

		return rowNumIndex;
	}

	private void writeCell(
		final int codesSheetFirstColumnIndex, final String value, final int count, final HSSFWorkbook xlsBook, final HSSFRow row) {
		HSSFCell cell = row.createCell(codesSheetFirstColumnIndex, CellType.STRING);

		final HSSFFont hSSFFont = this.buildFont(xlsBook,"calibri",11,false);
		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(codesSheetFirstColumnIndex == 0 ? IndexedColors.AQUA.getIndex() : IndexedColors.OLIVE_GREEN.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(hSSFFont);

		cell.setCellStyle(cellStyle);
		cell.setCellValue(value);
		cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
		cell.getCellStyle().setBorderRight(BorderStyle.THIN);
		if (count == 1) {
			cell.getCellStyle().setBorderBottom(BorderStyle.THIN);
		}
	}

	private void writeCodesHeader(
		final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum, final String colunmName) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		row.setHeightInPoints(16);

		HSSFCell cell = row.createCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
		final HSSFFont codesFont = this.buildFont(xlsBook, "calibri", 11,true);

		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex(), HorizontalAlignment.LEFT, codesFont));//
		cell.setCellValue(
			this.getMessageSource().getMessage(colunmName, null, locale));

		cell = row.createCell(GermplasmTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.buildHeaderStyle(xlsBook, IndexedColors.OLIVE_GREEN.getIndex(), HorizontalAlignment.LEFT, codesFont));
		cell.setCellValue(
			this.getMessageSource().getMessage("export.germplasm.list.template.description.column", null, locale));

	}

	private HSSFFont buildFont(final HSSFWorkbook xlsBook, final String fontName, final int fontHeight, final boolean bold) {
		final HSSFFont hSSFFont = xlsBook.createFont();
		hSSFFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		hSSFFont.setFontName(fontName);
		hSSFFont.setFontHeightInPoints((short) fontHeight);
		hSSFFont.setBold(bold);
		return hSSFFont;
	}

	private void setCustomColorAtIndex(
		final HSSFWorkbook wb, final IndexedColors indexedColor, final int red, final int green,
		final int blue) {
		final HSSFPalette customPalette = wb.getCustomPalette();
		customPalette.setColorAtIndex(indexedColor.index, (byte) red, (byte) green, (byte) blue);
	}

	private CellStyle buildHeaderStyle(final HSSFWorkbook xlsBook, final short colorIndex, final HorizontalAlignment horizontalAlignment,
		final HSSFFont hSSFFont) {
		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setAlignment(horizontalAlignment);
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(hSSFFont);

		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);

		return cellStyle;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}
}
