package org.ibp.api.java.impl.middleware.germplasm.workbook.generator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.java.impl.middleware.germplasm.workbook.common.ExcelCellStyleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class OntologyVariableSheetGenerator {

	private static final int COLUMN_WIDTH_PADDING = 6;
	private static final int CHARACTER_WIDTH = 250;

	@Autowired
	protected ResourceBundleMessageSource messageSource;

	private ExcelCellStyleBuilder sheetStyles;

	HSSFWorkbook hSSFWorkbook;

	public void writeOntologyVariableSheet(final HSSFWorkbook hSSFWorkbook, final String sheetName, final List<Variable> variableList) {
		this.hSSFWorkbook = hSSFWorkbook;
		this.sheetStyles = new ExcelCellStyleBuilder(this.hSSFWorkbook);
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet hSSFSheet = hSSFWorkbook.createSheet(this.getMessageSource().getMessage(sheetName, null, locale));
		hSSFSheet.setDefaultRowHeightInPoints(16);
		int currentRowNum = 0;

		currentRowNum = this.writeOntologyVariableHeader(hSSFSheet, currentRowNum);
		int count = variableList.size();

		for (final Variable variable : variableList) {
			final HSSFRow row = hSSFSheet.createRow(currentRowNum++);

			final CellStyle cellStyle =
				count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
					this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER);

			HSSFCell cell = row.createCell(0, CellType.STRING);
			cell.setCellStyle(
				count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
					this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variable.getName());

			cell = row.createCell(1, CellType.STRING);
			cell.setCellStyle(
				count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
					this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variable.getAlias());

			cell = row.createCell(2, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(variable.getDefinition());

			cell = row.createCell(3, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(variable.getProperty().getName());

			cell = row.createCell(4, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(variable.getScale().getName());

			cell = row.createCell(5, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(variable.getMethod().getName());

			final String dataTypeCode = this.getDataTypeCode(variable.getScale().getDataType().getId());
			cell = row.createCell(6, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(dataTypeCode);

			final String expectedRange = VariableValueUtil.getExpectedRange(variable);
			cell = row.createCell(7, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(expectedRange);
			count--;
		}

	}

	private int writeOntologyVariableHeader(final HSSFSheet sheet, final int currentRowNum) {
		final Locale locale = LocaleContextHolder.getLocale();
		int rowNumIndex = currentRowNum;
		final HSSFRow row = sheet.createRow(rowNumIndex++);

		HSSFCell cell = row.createCell(0, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_GREEN_WITH_BORDER));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.variable", null, locale));
		sheet.setColumnWidth(0, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(1, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_GREEN_WITH_BORDER));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.alias", null, locale));
		sheet.setColumnWidth(1, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(2, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.description", null, locale));
		sheet.setColumnWidth(2, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(3, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.property", null, locale));
		sheet.setColumnWidth(3, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(4, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.scale", null, locale));
		sheet.setColumnWidth(4, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(5, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.method", null, locale));
		sheet.setColumnWidth(5, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(6, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.data.type", null, locale));
		sheet.setColumnWidth(6, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(7, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.expected.values", null, locale));
		sheet.setColumnWidth(7, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		return rowNumIndex;
	}

	private String getDataTypeCode(final Integer dataTypeId) {
		return DataType.getById(dataTypeId).getDataTypeCode();
	}

	public ResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
