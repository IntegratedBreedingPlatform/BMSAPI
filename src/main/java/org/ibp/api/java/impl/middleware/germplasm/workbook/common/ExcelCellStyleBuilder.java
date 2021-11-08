package org.ibp.api.java.impl.middleware.germplasm.workbook.common;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.EnumMap;
import java.util.Map;

public class ExcelCellStyleBuilder {

	public enum ExcelCellStyle {
		HEADING_STYLE_YELLOW,
		HEADING_STYLE_PALE_BLUE,
		HEADING_STYLE_BLUE,
		HEADING_STYLE_ORANGE,

		HEADING_STYLE_AQUA,
		HEADING_STYLE_OLIVE_GREEN,
		STYLE_AQUA_WITH_LATERAL_BORDER,
		STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER,
		STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER,
		STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER,
		STYLE_AQUA_GREEN_WITH_BORDER
	}

	private final Map<ExcelCellStyle, CellStyle> stylesMap;
	private final HSSFWorkbook wb;

	public ExcelCellStyleBuilder(final HSSFWorkbook wb) {
		this.wb = wb;
		this.stylesMap = this.createStyles();
	}

	public CellStyle getCellStyle(final ExcelCellStyle style) {
		return this.stylesMap.get(style);
	}

	private void setCustomColorAtIndex(final IndexedColors indexedColor, final int red, final int green,
		final int blue) {
		final HSSFPalette customPalette = this.wb.getCustomPalette();
		customPalette.setColorAtIndex(indexedColor.index, (byte) red, (byte) green, (byte) blue);
	}

	private Map<ExcelCellStyle, CellStyle> createStyles() {
		final Map<ExcelCellStyle, CellStyle> styles = new EnumMap<>(ExcelCellStyle.class);

		this.setCustomColorAtIndex(IndexedColors.YELLOW, 255, 255, 204);
		this.setCustomColorAtIndex(IndexedColors.PALE_BLUE, 197, 217, 241);
		this.setCustomColorAtIndex(IndexedColors.BLUE, 91, 156, 220);
		this.setCustomColorAtIndex(IndexedColors.ORANGE, 255, 102, 0);

		this.setCustomColorAtIndex(IndexedColors.AQUA, 218, 227, 243);
		this.setCustomColorAtIndex(IndexedColors.OLIVE_GREEN, 235, 241, 222);

		final Font headerFontObservation = this.buildFont("arial", 10, true);
		final Font fontCodes = this.buildFont("calibri", 11, false);

		final CellStyle headingStyleYellow = this.createStyleWithBorder();
		headingStyleYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		headingStyleYellow.setAlignment(HorizontalAlignment.CENTER);
		headingStyleYellow.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_YELLOW, headingStyleYellow);

		final CellStyle headingStylePaleBlue = this.createStyleWithBorder();
		headingStylePaleBlue.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		headingStylePaleBlue.setAlignment(HorizontalAlignment.CENTER);
		headingStylePaleBlue.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_PALE_BLUE, headingStylePaleBlue);

		final CellStyle headingStyleBlue = this.createStyleWithBorder();
		headingStyleBlue.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		headingStyleBlue.setAlignment(HorizontalAlignment.CENTER);
		headingStyleBlue.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_BLUE, headingStyleBlue);

		final CellStyle headingStyleOrange = this.createStyleWithBorder();
		headingStyleOrange.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
		headingStyleOrange.setAlignment(HorizontalAlignment.CENTER);
		headingStyleOrange.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_ORANGE, headingStyleOrange);

		final CellStyle headingStyleAqua = this.createStyleWithBorder();
		headingStyleAqua.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		headingStyleAqua.setAlignment(HorizontalAlignment.LEFT);
		headingStyleAqua.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_AQUA, headingStyleAqua);

		final CellStyle headingStyleOliveGreen = this.createStyleWithBorder();
		headingStyleOliveGreen.setFillForegroundColor(IndexedColors.OLIVE_GREEN.getIndex());
		headingStyleOliveGreen.setAlignment(HorizontalAlignment.LEFT);
		headingStyleOliveGreen.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN, headingStyleOliveGreen);

		final CellStyle cellStyleAqua = this.createStyleWithLateralBorder();
		cellStyleAqua.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		cellStyleAqua.setAlignment(HorizontalAlignment.LEFT);
		cellStyleAqua.setFont(fontCodes);
		styles.put(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER, cellStyleAqua);

		final CellStyle cellStyleOliveGreen = this.createStyleWithLateralBorder();
		cellStyleOliveGreen.setFillForegroundColor(IndexedColors.OLIVE_GREEN.getIndex());
		cellStyleOliveGreen.setAlignment(HorizontalAlignment.LEFT);
		cellStyleOliveGreen.setFont(fontCodes);
		styles.put(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER, cellStyleOliveGreen);

		final CellStyle cellStyleAquaWithLateralAndBottomBorder = this.createStyleWithLateralAndBottomBorder();
		cellStyleAquaWithLateralAndBottomBorder.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		cellStyleAquaWithLateralAndBottomBorder.setAlignment(HorizontalAlignment.LEFT);
		cellStyleAquaWithLateralAndBottomBorder.setFont(fontCodes);
		styles.put(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER, cellStyleAquaWithLateralAndBottomBorder);

		final CellStyle cellStyleOliveGreenWithLateralAndBottomBorder = this.createStyleWithLateralAndBottomBorder();
		cellStyleOliveGreenWithLateralAndBottomBorder.setFillForegroundColor(IndexedColors.OLIVE_GREEN.getIndex());
		cellStyleOliveGreenWithLateralAndBottomBorder.setAlignment(HorizontalAlignment.LEFT);
		cellStyleOliveGreenWithLateralAndBottomBorder.setFont(fontCodes);
		styles.put(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER, cellStyleOliveGreenWithLateralAndBottomBorder);

		final CellStyle cellStyleAquaGreenWithBorder = this.createStyleWithBorder();
		cellStyleAquaGreenWithBorder.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		cellStyleAquaGreenWithBorder.setAlignment(HorizontalAlignment.CENTER);
		cellStyleAquaGreenWithBorder.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.STYLE_AQUA_GREEN_WITH_BORDER, cellStyleAquaGreenWithBorder);
		return styles;
	}

	private HSSFFont buildFont(final String fontName, final int fontHeight, final boolean bold) {
		final HSSFFont hSSFFont = this.wb.createFont();
		hSSFFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		hSSFFont.setFontName(fontName);
		hSSFFont.setFontHeightInPoints((short) fontHeight);
		hSSFFont.setBold(bold);
		return hSSFFont;
	}

	private CellStyle createStyleWithBorder() {
		final CellStyle cellStyle = this.createStyle();
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		return cellStyle;
	}

	private CellStyle createStyleWithLateralBorder() {
		final CellStyle cellStyle = this.createStyle();
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		return cellStyle;
	}

	private CellStyle createStyleWithLateralAndBottomBorder() {
		final CellStyle cellStyle = this.createStyle();
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		return cellStyle;
	}

	private CellStyle createStyle() {
		final CellStyle cellStyle = this.wb.createCellStyle();
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}
}
