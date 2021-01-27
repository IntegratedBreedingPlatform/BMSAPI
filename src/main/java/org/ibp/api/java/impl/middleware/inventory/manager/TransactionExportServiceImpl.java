package org.ibp.api.java.impl.middleware.inventory.manager;

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
import org.generationcp.commons.util.ExportFileName;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.inventory.manager.TransactionExportService;
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
public class TransactionExportServiceImpl implements TransactionExportService {

	private static final String FILE_NAME = "export transactions template.xls";

	private static final int TRANSACTIONS_SHEET_DESIGNATION_COLUMN_INDEX = 0;
	private static final int TRANSACTIONS_SHEET_GID_COLUMN_INDEX = 1;
	private static final int TRANSACTIONS_SHEET_LOT_UUID_COLUMN_INDEX = 2;
	private static final int TRANSACTIONS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX = 3;
	private static final int TRANSACTIONS_SHEET_STORAGE_LOCATION_COLUMN_INDEX = 4;
	private static final int TRANSACTIONS_SHEET_STOCK_ID_COLUMN_INDEX = 5;
	private static final int TRANSACTIONS_SHEET_LOT_AVAILABLE_COLUMN_INDEX = 6;
	private static final int TRANSACTIONS_SHEET_TRN_ID_COLUMN_INDEX = 7;
	private static final int TRANSACTIONS_SHEET_CREATED_COLUMN_INDEX = 8;
	private static final int TRANSACTIONS_SHEET_USERNAME_COLUMN_INDEX = 9;
	private static final int TRANSACTIONS_SHEET_STATUS_COLUMN_INDEX = 10;
	private static final int TRANSACTIONS_SHEET_TYPE_COLUMN_INDEX = 11;
	private static final int TRANSACTIONS_SHEET_UNITS_COLUMN_INDEX = 12;
	private static final int TRANSACTIONS_SHEET_AMOUNT_COLUMN_INDEX = 13;
	private static final int TRANSACTIONS_SHEET_NOTES_COLUMN_INDEX = 14;
	private static final int TRANSACTIONS_SHEET_NEW_AMOUNT_COLUMN_INDEX = 15;
	private static final int TRANSACTIONS_SHEET_NEW_BALANCE_COLUMN_INDEX = 16;
	private static final int TRANSACTIONS_SHEET_NEW_NOTES_COLUMN_INDEX = 17;

	@Autowired
	ResourceBundleMessageSource messageSource;

	@Override
	public File export(final List<TransactionDto> transactionDtoList) {
		try {
			final File temporaryFolder = Files.createTempDir();
			final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + ExportFileName.getInstance().generateFileName(TransactionExportServiceImpl.FILE_NAME);
			return this.generateTemplateFile(fileNameFullPath, transactionDtoList);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.lot.template", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	private File generateTemplateFile(final String fileNameFullPath, final List<TransactionDto> transactionDtoList) throws IOException {
		final HSSFWorkbook xlsBook = new HSSFWorkbook();

		final File file = new File(fileNameFullPath);
		this.writeTransactionsSheet(xlsBook, transactionDtoList);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			xlsBook.write(fos);
		}
		return file;
	}

	private void writeTransactionsSheet(final HSSFWorkbook xlsBook, final List<TransactionDto> transactionDtoList) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet =
			xlsBook.createSheet(
				this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.transactions", null, locale));
		int currentRowNum = 0;

		this.writeTransactionsHeader(xlsBook, xlsSheet, currentRowNum++);
		this.writeTransactions(currentRowNum, xlsBook, xlsSheet, transactionDtoList);
	}

	private void writeTransactions(
		final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final List<TransactionDto> transactionDtoList) {
		final CellStyle backgroundStyle = xlsBook.createCellStyle();
		final HSSFFont blackFont = xlsBook.createFont();
		blackFont.setFontName("Arial");
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		blackFont.setFontHeightInPoints((short) 10);
		backgroundStyle.setFont(blackFont);

		int rowNumIndex = currentRowNum;
		for (final TransactionDto transactionDto : transactionDtoList) {
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			row.setHeightInPoints(16);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_DESIGNATION_COLUMN_INDEX, transactionDto.getLot().getDesignation(),
				CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_GID_COLUMN_INDEX, transactionDto.getLot().getGid().toString(),
				CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_LOT_UUID_COLUMN_INDEX, transactionDto.getLot().getLotUUID(),
				CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX,
				transactionDto.getLot().getLocationAbbr(), CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_STORAGE_LOCATION_COLUMN_INDEX, transactionDto.getLot().getLocationName(),
				CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_STOCK_ID_COLUMN_INDEX, transactionDto.getLot().getStockId(),
				CellType.STRING, row);
			this.writeCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_LOT_AVAILABLE_COLUMN_INDEX,
				transactionDto.getLot().getAvailableBalance() == null ? "0" : transactionDto.getLot().getAvailableBalance().toString(),
				CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_TRN_ID_COLUMN_INDEX, transactionDto.getTransactionId().toString(),
				CellType.NUMERIC, row);
			this.writeCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_CREATED_COLUMN_INDEX,
				transactionDto.getCreatedDate() == null ? "" : transactionDto.getCreatedDate().toString(), CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_USERNAME_COLUMN_INDEX, transactionDto.getCreatedByUsername(),
				CellType.STRING, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_STATUS_COLUMN_INDEX, transactionDto.getTransactionStatus(), CellType.STRING,
				row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_TYPE_COLUMN_INDEX, transactionDto.getTransactionType(), CellType.STRING,
				row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_UNITS_COLUMN_INDEX, transactionDto.getLot().getUnitName(), CellType.STRING,
				row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_AMOUNT_COLUMN_INDEX, transactionDto.getAmount().toString(),
				CellType.NUMERIC, row);
			this.writeCell(
				TransactionExportServiceImpl.TRANSACTIONS_SHEET_NOTES_COLUMN_INDEX, transactionDto.getNotes(), CellType.STRING, row);
			this.writeCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_AMOUNT_COLUMN_INDEX, null, CellType.NUMERIC, row);
			this.writeCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_BALANCE_COLUMN_INDEX, null, CellType.NUMERIC, row);
			this.writeCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_NOTES_COLUMN_INDEX, null, CellType.STRING, row);
		}
	}

	private void writeCell(final int codesSheetFirstColumnIndex, final String value, final CellType type, final HSSFRow row) {
		final HSSFCell cell = row.createCell(codesSheetFirstColumnIndex, type);
		cell.setCellValue(value);
	}

	private void writeTransactionsHeader(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		row.setHeightInPoints(16);

		this.setCustomColorAtIndex(xlsBook, IndexedColors.YELLOW, 255, 255, 204);
		this.setCustomColorAtIndex(xlsBook, IndexedColors.AQUA, 218, 227, 243);
		this.setCustomColorAtIndex(xlsBook, IndexedColors.OLIVE_GREEN, 235, 241, 222);

		HSSFCell cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_DESIGNATION_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.designation.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_GID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.YELLOW.getIndex()));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.gid.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_LOT_UUID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.lot.uuid.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource
			.getMessage("export.inventory.manager.transaction.template.sheet.storage.location.abbr.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STORAGE_LOCATION_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource
			.getMessage("export.inventory.manager.transaction.template.sheet.storage.location.name.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STOCK_ID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.stock.id.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_LOT_AVAILABLE_COLUMN_INDEX, CellType.NUMERIC);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.lot.available.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_TRN_ID_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.trn.id.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_CREATED_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.created.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_USERNAME_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.username.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STATUS_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.status.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_TYPE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.type.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_UNITS_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.units.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_AMOUNT_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.amount.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NOTES_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.AQUA.getIndex()));
		cell.setCellValue(this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.notes.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_AMOUNT_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.OLIVE_GREEN.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.new.amount.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_BALANCE_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.OLIVE_GREEN.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.new.balance.column", null, locale));

		cell = row.createCell(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_NOTES_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, IndexedColors.OLIVE_GREEN.getIndex()));
		cell.setCellValue(
			this.messageSource.getMessage("export.inventory.manager.transaction.template.sheet.new.notes.column", null, locale));

		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_DESIGNATION_COLUMN_INDEX, 16 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_GID_COLUMN_INDEX, 8 * 250);

		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_LOT_UUID_COLUMN_INDEX, 36 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STORAGE_LOCATION_ABBR_COLUMN_INDEX, 26 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STORAGE_LOCATION_COLUMN_INDEX, 21 * 250);

		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STOCK_ID_COLUMN_INDEX, 13 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_LOT_AVAILABLE_COLUMN_INDEX, 20 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_TRN_ID_COLUMN_INDEX, 11 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_CREATED_COLUMN_INDEX, 12 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_USERNAME_COLUMN_INDEX, 13 * 250);

		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_STATUS_COLUMN_INDEX, 15 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_TYPE_COLUMN_INDEX, 12 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_UNITS_COLUMN_INDEX, 18 * 250);

		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_AMOUNT_COLUMN_INDEX, 10 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NOTES_COLUMN_INDEX, 20 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_AMOUNT_COLUMN_INDEX, 15 * 250);

		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_BALANCE_COLUMN_INDEX, 16 * 250);
		xlsSheet.setColumnWidth(TransactionExportServiceImpl.TRANSACTIONS_SHEET_NEW_NOTES_COLUMN_INDEX, 20 * 250);

	}

	private CellStyle getHeaderStyle(final HSSFWorkbook xlsBook, final short colorIndex) {
		final HSSFFont blackFont = xlsBook.createFont();
		blackFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		blackFont.setFontName("Arial");
		blackFont.setFontHeightInPoints((short) 10);
		blackFont.setBold(true);

		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(blackFont);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);

		return cellStyle;
	}

	private void setCustomColorAtIndex(
		final HSSFWorkbook wb, final IndexedColors indexedColor, final int red, final int green,
		final int blue) {
		final HSSFPalette customPalette = wb.getCustomPalette();
		customPalette.setColorAtIndex(indexedColor.index, (byte) red, (byte) green, (byte) blue);
	}

}
