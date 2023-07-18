package org.ibp.api.java.impl.middleware.inventory.manager;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.java.impl.middleware.germplasm.workbook.generator.OntologyVariableSheetGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public abstract class AbstractLotExcelTemplateExportService {

    protected static final String FILE_EXTENSION = ".xls";
    protected static final int CODES_SHEET_FIRST_COLUMN_INDEX = 0;
    protected static final int CODES_SHEET_SECOND_COLUMN_INDEX = 1;

    @Autowired
    protected ResourceBundleMessageSource messageSource;

    @Autowired
    protected OntologyVariableDataManager ontologyVariableDataManager;

    @Autowired
    protected OntologyVariableSheetGenerator ontologyVariableSheetGenerator;

    abstract File export(final String programUUID, final String cropName, final List<LocationDTO> locations,
                         final List<VariableDetails> units);

    abstract void writeLotsHeader(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum);

    abstract void writeOntologyVariableSheet(final HSSFWorkbook xlsBook, final String programUUID);

    protected File generateTemplateFile(final String fileNamePath, final List<LocationDTO> locations, final List<VariableDetails> units,
                                        final String programUUID)
            throws IOException {
        final HSSFWorkbook xlsBook = new HSSFWorkbook();

        final File file = new File(fileNamePath);
        this.writeLotsSheet(xlsBook);
        this.writeCodesSheet(xlsBook, locations, units, programUUID);
        this.writeOntologyVariableSheet(xlsBook, programUUID);

        try (final FileOutputStream fos = new FileOutputStream(file)) {
            xlsBook.write(fos);

        }
        return file;
    }

    protected void writeLotsSheet(final HSSFWorkbook xlsBook) {
        final Locale locale = LocaleContextHolder.getLocale();
        final HSSFSheet xlsSheet =
                xlsBook.createSheet(this.messageSource.getMessage("export.inventory.manager.lot.template.sheet.lots", null, locale));
        int currentRowNum = 0;

        this.writeLotsHeader(xlsBook, xlsSheet, currentRowNum++);
    }

    protected void writeCodesSheet(final HSSFWorkbook xlsBook, final List<LocationDTO> locations, final List<VariableDetails> units,
                                   final String programUUID) {
        final Locale locale = LocaleContextHolder.getLocale();
        final HSSFSheet xlsSheet =
                xlsBook.createSheet(this.messageSource.getMessage("export.inventory.manager.lot.template.sheet.codes", null, locale));
        int currentRowNum = 0;

        this.writeLocationHeader(xlsBook, xlsSheet, currentRowNum++);
        currentRowNum = this.writeLocationSection(currentRowNum, xlsBook, xlsSheet, locations);
        xlsSheet.createRow(currentRowNum++);

        this.writeUnitsHeader(xlsBook, xlsSheet, currentRowNum++);
        currentRowNum = this.writeUnitsSection(currentRowNum, xlsBook, xlsSheet, units);
        xlsSheet.createRow(currentRowNum);
        xlsSheet.setColumnWidth(AbstractLotExcelTemplateExportService.CODES_SHEET_FIRST_COLUMN_INDEX, 34 * 250);
        xlsSheet.setColumnWidth(AbstractLotExcelTemplateExportService.CODES_SHEET_SECOND_COLUMN_INDEX, 65 * 250);
    }

    protected int writeLocationSection(
            final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final List<LocationDTO> locations) {
        final CellStyle backgroundStyle = xlsBook.createCellStyle();
        final HSSFFont blackFont = xlsBook.createFont();
        blackFont.setFontName("calibri");
        blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        blackFont.setFontHeightInPoints((short) 11);
        backgroundStyle.setFont(blackFont);

        int rowNumIndex = currentRowNum;
        int count = locations.size();
        for (final LocationDTO locationDto : locations) {
            final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
            row.setHeightInPoints(16);
            this.writeCell(AbstractLotExcelTemplateExportService.CODES_SHEET_FIRST_COLUMN_INDEX, locationDto.getAbbreviation(), count, xlsBook,
                    row);
            this.writeCell(AbstractLotExcelTemplateExportService.CODES_SHEET_SECOND_COLUMN_INDEX, locationDto.getName(), count, xlsBook, row);
            count--;
        }
        return rowNumIndex;
    }

    protected int writeUnitsSection(
            final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final List<VariableDetails> units) {
        final CellStyle backgroundStyle = xlsBook.createCellStyle();
        final HSSFFont blackFont = xlsBook.createFont();
        blackFont.setFontName("calibri");
        blackFont.setFontHeightInPoints((short) 11);
        blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        backgroundStyle.setFont(blackFont);

        int rowNumIndex = currentRowNum;
        int count = units.size();

        for (final VariableDetails variableDetail : units) {
            final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
            row.setHeightInPoints(16);
            this.writeCell(AbstractLotExcelTemplateExportService.CODES_SHEET_FIRST_COLUMN_INDEX, variableDetail.getName(), count, xlsBook, row);
            this.writeCell(
                    AbstractLotExcelTemplateExportService.CODES_SHEET_SECOND_COLUMN_INDEX, variableDetail.getDescription(), count, xlsBook, row);
            count--;
        }

        return rowNumIndex;
    }

    private void writeCell(
            final int codesSheetFirstColumnIndex, final String value, final int count, final HSSFWorkbook xlsBook, final HSSFRow row) {
        final HSSFCell cell = row.createCell(codesSheetFirstColumnIndex, CellType.STRING);
        cell.setCellStyle(this.getCellStyle(
                xlsBook,
                codesSheetFirstColumnIndex == 0 ? IndexedColors.AQUA.getIndex() : IndexedColors.OLIVE_GREEN.getIndex()));
        cell.setCellValue(value);
        cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
        cell.getCellStyle().setBorderRight(BorderStyle.THIN);
        if (count == 1) {
            cell.getCellStyle().setBorderBottom(BorderStyle.THIN);
        }
    }

    protected void writeLocationHeader(
            final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
        final Locale locale = LocaleContextHolder.getLocale();
        final HSSFRow row = xlsSheet.createRow(currentRowNum);
        row.setHeightInPoints(16);

        this.setCustomColorAtIndex(xlsBook, IndexedColors.AQUA, 218, 227, 243);
        this.setCustomColorAtIndex(xlsBook, IndexedColors.OLIVE_GREEN, 235, 241, 222);

        HSSFCell cell = row.createCell(AbstractLotExcelTemplateExportService.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
        cell.setCellValue(
                this.messageSource.getMessage("export.inventory.manager.lot.template.storage.location.abbr.column", null, locale));

        cell = row.createCell(AbstractLotExcelTemplateExportService.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.OLIVE_GREEN.getIndex()));
        cell.setCellValue(
                this.messageSource.getMessage("export.inventory.manager.lot.template.storage.location.name.column", null, locale));

    }

    protected void writeUnitsHeader(
            final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
        final Locale locale = LocaleContextHolder.getLocale();
        final HSSFRow row = xlsSheet.createRow(currentRowNum);
        row.setHeightInPoints(16);

        HSSFCell cell = row.createCell(AbstractLotExcelTemplateExportService.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
        cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.units.column", null, locale));

        cell = row.createCell(AbstractLotExcelTemplateExportService.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.OLIVE_GREEN.getIndex()));
        cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.units.description.column", null, locale));

    }

    protected CellStyle getHeaderStyle(final HSSFWorkbook xlsBook, final short colorIndex) {
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

    protected CellStyle getCellStyle(final HSSFWorkbook xlsBook, final short colorIndex) {
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

    protected void setCustomColorAtIndex(
            final HSSFWorkbook wb, final IndexedColors indexedColor, final int red, final int green,
            final int blue) {
        final HSSFPalette customPalette = wb.getCustomPalette();
        customPalette.setColorAtIndex(indexedColor.index, (byte) red, (byte) green, (byte) blue);
    }
}
