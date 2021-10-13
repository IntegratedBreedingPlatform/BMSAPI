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
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmListTemplateExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class GermplasmListTemplateExportServiceImpl implements GermplasmListTemplateExportService {

	private static final String FILE_NAME = "GermplasmListImportTemplate.xls";

	private static final int COLUMN_WIDTH_PADDING = 6;
	private static final int CHARACTER_WIDTH = 250;
	private static final Map<String, ExcelCellStyle> IMPORT_HEADERS;

	static {
		IMPORT_HEADERS = new LinkedHashMap<>();
		IMPORT_HEADERS.put("export.germplasm.list.template.gid.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.guid.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.designation.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
	}

	private Map<ExcelCellStyle, CellStyle> sheetStylesMap;

	private HSSFWorkbook wb;


	public enum ExcelCellStyle {
		HEADING_STYLE_YELLOW,
	}


	@Autowired
	protected ResourceBundleMessageSource messageSource;

	@Override
	public File export(final String cropName, final String programUUID) {
		try {
			final File temporaryFolder = Files.createTempDir();

			final String fileNameFullPath =
				temporaryFolder.getAbsolutePath() + File.separator + GermplasmListTemplateExportServiceImpl.FILE_NAME;
			return this.generateTemplateFile(fileNameFullPath, cropName, programUUID);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
			errors.reject("cannot.export.as.xls.germplasm.template", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	private File generateTemplateFile(final String fileNamePath, final String cropName, final String programUUID) throws IOException {
		this.wb = new HSSFWorkbook();

		final File file = new File(fileNamePath);
		this.sheetStylesMap = this.createStyles();
		this.writeObservationSheet();

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			this.wb.write(fos);

		}
		return file;
	}

	private void writeObservationSheet() {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet observationSheet =
			this.wb.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.observation", null, locale));
		observationSheet.setDefaultRowHeightInPoints(16);
		this.writeObservationHeader(observationSheet);
	}

	private Map<ExcelCellStyle, CellStyle> createStyles() {
		final Map<ExcelCellStyle, CellStyle> styles = new EnumMap<>(ExcelCellStyle.class);
		this.setCustomColorAtIndex(IndexedColors.YELLOW, 255, 255, 204);

		final Font headerFontObservation = this.buildFont("arial", 10, true);

		final CellStyle headingStyleYellow = this.createStyleWithBorder();
		headingStyleYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		headingStyleYellow.setAlignment(HorizontalAlignment.CENTER);
		headingStyleYellow.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_YELLOW, headingStyleYellow);

		return styles;
	}

	private CellStyle createStyleWithBorder() {
		final CellStyle cellStyle = this.createStyle();
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		return cellStyle;
	}

	private CellStyle createStyle() {
		final CellStyle cellStyle = this.wb.createCellStyle();
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	private void writeObservationHeader(final HSSFSheet observationSheet) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = observationSheet.createRow(0);

		final Map<String, ExcelCellStyle> headers = IMPORT_HEADERS;
		final Iterator<Map.Entry<String, ExcelCellStyle>> iterator = headers.entrySet().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			final Map.Entry<String, ExcelCellStyle> entry = iterator.next();
			final HSSFCell cell = row.createCell(index, CellType.STRING);
			cell.setCellStyle(this.sheetStylesMap.get(entry.getValue()));
			final String headerColumn = this.getMessageSource().getMessage(entry.getKey(), null, locale);
			cell.setCellValue(headerColumn);
			observationSheet.setColumnWidth(index, (headerColumn.length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH);
			index++;
		}
	}

	private void setCustomColorAtIndex(final IndexedColors indexedColor, final int red, final int green,
		final int blue) {
		final HSSFPalette customPalette = this.wb.getCustomPalette();
		customPalette.setColorAtIndex(indexedColor.index, (byte) red, (byte) green, (byte) blue);
	}

	private HSSFFont buildFont(final String fontName, final int fontHeight, final boolean bold) {
		final HSSFFont hSSFFont = this.wb.createFont();
		hSSFFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		hSSFFont.setFontName(fontName);
		hSSFFont.setFontHeightInPoints((short) fontHeight);
		hSSFFont.setBold(bold);
		return hSSFFont;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}
}
