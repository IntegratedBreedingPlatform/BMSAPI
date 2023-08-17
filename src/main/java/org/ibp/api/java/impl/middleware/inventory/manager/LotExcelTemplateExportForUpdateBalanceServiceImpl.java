package org.ibp.api.java.impl.middleware.inventory.manager;

import com.google.common.io.Files;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.generationcp.middleware.api.location.LocationDTO;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.inventory.manager.LotTemplateExportService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Service
public class LotExcelTemplateExportForUpdateBalanceServiceImpl extends AbstractLotExcelTemplateExportService implements LotTemplateExportService {

    private static final String FILE_NAME = "basic_template_import_update_balance_";
    private static final int LOTS_SHEET_LOT_UID_COLUMN_INDEX = 0;
    private static final int LOTS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX = 1;
    private static final int LOTS_SHEET_NEW_BALANCE_COLUMN_INDEX = 2;
    private static final int LOTS_SHEET_NOTES_COLUMN_INDEX = 3;

    @Override
    public File export(final String programUUID, final String cropName, final List<LocationDTO> locations, final List<VariableDetails> units) {
        try {
            final File temporaryFolder = Files.createTempDir();

            final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator
                    + FILE_NAME + cropName.toLowerCase()
                    + AbstractLotExcelTemplateExportService.FILE_EXTENSION;
            return this.generateTemplateFile(fileNameFullPath, locations, units, programUUID);
        } catch (final IOException e) {
            final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
            errors.reject("cannot.exportAsXLS.lot.template", "");
            throw new ResourceNotFoundException(errors.getAllErrors().get(0));
        }
    }

    @Override
    void writeLotsHeader(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
        final Locale locale = LocaleContextHolder.getLocale();
        final HSSFRow row = xlsSheet.createRow(currentRowNum);
        row.setHeightInPoints(16);

        this.setCustomColorAtIndex(xlsBook, IndexedColors.ORANGE, 248, 203, 173);
        this.setCustomColorAtIndex(xlsBook, IndexedColors.AQUA, 218, 227, 243);
        this.setCustomColorAtIndex(xlsBook, IndexedColors.YELLOW, 255, 230, 153);

        HSSFCell cell = row.createCell(LOTS_SHEET_LOT_UID_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.ORANGE.getIndex()));
        cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.lotuid.column", null, locale));

        cell = row.createCell(LOTS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
        cell.setCellValue(
                this.messageSource.getMessage("export.inventory.manager.lot.template.storage.location.abbr.column", null, locale));

        cell = row.createCell(LOTS_SHEET_NEW_BALANCE_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
        cell.setCellValue(
                this.messageSource.getMessage("export.inventory.manager.lot.template.new.balance.column", null, locale));

        cell = row.createCell(LOTS_SHEET_NOTES_COLUMN_INDEX, CellType.STRING);
        cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex()));
        cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.lot.template.notes.column", null, locale));

        xlsSheet.setColumnWidth(LOTS_SHEET_LOT_UID_COLUMN_INDEX, 8 * 250);
        xlsSheet.setColumnWidth(LOTS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, 34 * 250);
        xlsSheet.setColumnWidth(LOTS_SHEET_NEW_BALANCE_COLUMN_INDEX, 15 * 250);
        xlsSheet.setColumnWidth(LOTS_SHEET_NOTES_COLUMN_INDEX, 10 * 250);
    }

    @Override
    void writeOntologyVariableSheet(final HSSFWorkbook xlsBook, final String programUUID) {
        // Do nothing. Ontology Variable sheet should not be availble for this type of export.
    }
}