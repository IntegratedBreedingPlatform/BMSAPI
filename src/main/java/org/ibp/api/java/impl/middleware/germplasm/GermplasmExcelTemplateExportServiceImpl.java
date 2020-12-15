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
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.MethodService;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.germplasm.GermplasmTemplateExportService;
import org.ibp.api.java.location.LocationService;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class GermplasmExcelTemplateExportServiceImpl implements GermplasmTemplateExportService {

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));
	private static final Set<Integer> LOCATION_TYPE = new HashSet<>(Arrays.asList(410, 405));

	private static final String FILE_NAME = "GermplasmImportTemplate.xls";
	private static final String FILE_NAME_FOR_IMPORT_UPDATE = "GermplasmUpdateTemplate.xls";
	private static final int CODES_SHEET_FIRST_COLUMN_INDEX = 0;
	private static final int CODES_SHEET_SECOND_COLUMN_INDEX = 1;

	private static final Map<String, ExcelCellStyle> IMPORT_HEADERS;

	static {
		IMPORT_HEADERS = new LinkedHashMap<>();
		IMPORT_HEADERS.put("export.germplasm.list.template.entry.no.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.lname.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.drvnm.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.preferred.name.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.entry.code.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.location.abbr.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.reference.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.creation.date.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.breeding.method.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.note.column", ExcelCellStyle.HEADING_STYLE_PALE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.storage.location.abbr.column", ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.units.column", ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.amount.column", ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.stock.id.column", ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.guid.column", ExcelCellStyle.HEADING_STYLE_ORANGE);
	}

	private static final Map<String, ExcelCellStyle> IMPORT_HEADERS_FOR_UPDATE;

	static {
		IMPORT_HEADERS_FOR_UPDATE = new LinkedHashMap<>();
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.gid.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.guid.column", ExcelCellStyle.HEADING_STYLE_ORANGE);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.preferred.name.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.location.abbr.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.creation.date.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.reference.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.drvnm.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.note.column", ExcelCellStyle.HEADING_STYLE_PALE_BLUE);
	}

	@Autowired
	protected ResourceBundleMessageSource messageSource;

	@Autowired
	protected LocationService locationService;

	@Autowired
	protected VariableService variableService;

	@Resource
	protected MethodService methodService;

	@Autowired
	protected GermplasmService germplasmService;

	@Autowired
	BreedingMethodService breedingMethodService;


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
	}

	private Map<ExcelCellStyle, CellStyle> sheetStylesMap;

	private HSSFWorkbook wb;

	@Override
	public File export(final String cropName, final String programUUID, final boolean isGermplasmUpdateFormat) {

		try {
			final File temporaryFolder = Files.createTempDir();

			final String fileNameFullPath =
				temporaryFolder.getAbsolutePath() + File.separator + (isGermplasmUpdateFormat ?
					GermplasmExcelTemplateExportServiceImpl.FILE_NAME_FOR_IMPORT_UPDATE :
					GermplasmExcelTemplateExportServiceImpl.FILE_NAME);
			return this.generateTemplateFile(fileNameFullPath, cropName, programUUID, isGermplasmUpdateFormat);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.export.as.xls.germplasm.template", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	private File generateTemplateFile(final String fileNamePath, final String cropName, final String programUUID,
		final boolean isGermplasmUpdateFormat) throws IOException {
		this.wb = new HSSFWorkbook();

		final File file = new File(fileNamePath);
		this.sheetStylesMap = createStyles();
		this.writeObservationSheet(isGermplasmUpdateFormat);
		this.writeCodesSheet(cropName, programUUID, isGermplasmUpdateFormat);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			this.wb.write(fos);

		}
		return file;
	}

	private void writeObservationSheet(final boolean isGermplasmUpdateFormat) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet observationSheet =
			this.wb.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.observation", null, locale));
		observationSheet.setDefaultRowHeightInPoints(16);
		this.writeObservationHeader(observationSheet, isGermplasmUpdateFormat);
	}

	private void writeObservationHeader(final HSSFSheet observationSheet, final boolean isGermplasmUpdateFormat) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = observationSheet.createRow(0);

		final Map<String, ExcelCellStyle> headers = isGermplasmUpdateFormat ? IMPORT_HEADERS_FOR_UPDATE : IMPORT_HEADERS;
		final Iterator<Map.Entry<String, ExcelCellStyle>> iterator = headers.entrySet().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			Map.Entry<String, ExcelCellStyle> entry = iterator.next();
			HSSFCell cell = row.createCell(index, CellType.STRING);
			cell.setCellStyle(this.sheetStylesMap.get(entry.getValue()));
			final String headerColumn = this.getMessageSource().getMessage(entry.getKey(), null, locale);
			cell.setCellValue(headerColumn);

			switch (entry.getKey()){
				case "export.germplasm.list.template.preferred.name.column":
				case "export.germplasm.list.template.location.abbr.column":
					observationSheet.setColumnWidth(index, 20 * 250);
					break;
				case "export.germplasm.list.template.entry.code.column":
					observationSheet.setColumnWidth(index, 16 * 250);
					break;
				case "export.germplasm.list.template.creation.date.column":
					observationSheet.setColumnWidth(index, 18 * 250);
					break;
				case "export.germplasm.list.template.breeding.method.column":
					observationSheet.setColumnWidth(index, 22 * 250);
					break;
				case "export.germplasm.list.template.storage.location.abbr.column":
					observationSheet.setColumnWidth(index, 28 * 250);
					break;
				default:
					observationSheet.setColumnWidth(index, 13 * 250);
					break;
			}
			index++;
		}
	}

	private void writeCodesSheet(final String cropName, final String programUUID, final boolean isGermplasmUpdateFormat) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet codesSheet =
			this.wb.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.codes", null, locale));
		codesSheet.setDefaultRowHeightInPoints(16);

		int currentRowNum = 0;

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<VariableDetails> units = this.variableService.getVariablesByFilter(variableFilter);

		final List<LocationDto> storageLocations =
			this.locationService
				.getLocations(cropName, programUUID, GermplasmExcelTemplateExportServiceImpl.STORAGE_LOCATION_TYPE, null, null, false);

		final List<LocationDto> locations =
			this.locationService
				.getLocations(cropName, programUUID, GermplasmExcelTemplateExportServiceImpl.LOCATION_TYPE, null, null, false);

		final List<AttributeDTO> attributeDTOs =
			this.germplasmService.filterGermplasmAttributes(null);

		final List<BreedingMethodDTO> BreedingMethodDTOs = this.breedingMethodService.getBreedingMethods(programUUID, null, false);

		final List<GermplasmNameTypeDTO> germplasmNames = this.germplasmService.filterGermplasmNameTypes(null);

		this.writeCodesHeader(codesSheet, currentRowNum++, "export.germplasm.list.template.breeding.methods.column");
		currentRowNum = this.writeBreedingMethodSection(codesSheet, currentRowNum, BreedingMethodDTOs);
		codesSheet.createRow(currentRowNum++);

		this.writeCodesHeader(codesSheet, currentRowNum++, "export.germplasm.list.template.attributes.column");
		currentRowNum = this.writeAttributeSection(codesSheet, currentRowNum, attributeDTOs);
		codesSheet.createRow(currentRowNum++);

		this.writeCodesHeader(codesSheet, currentRowNum++, "export.germplasm.list.template.location.abbr.column");
		currentRowNum = this.writeLocationAbbrSection(codesSheet, currentRowNum, locations);
		codesSheet.createRow(currentRowNum++);

		this.writeCodesHeader(codesSheet, currentRowNum++, "export.germplasm.list.template.name.column");
		currentRowNum = this.writeNameSection(codesSheet, currentRowNum, germplasmNames);
		codesSheet.createRow(currentRowNum++);

		if (!isGermplasmUpdateFormat) {
			this.writeCodesHeader(codesSheet, currentRowNum++, "export.germplasm.list.template.storage.location.abbr.column");
			currentRowNum = this.writeLocationAbbrSection(codesSheet, currentRowNum, storageLocations);
			codesSheet.createRow(currentRowNum++);

			this.writeCodesHeader(codesSheet, currentRowNum++, "export.germplasm.list.template.units.column");
			currentRowNum = this.writeUnitsSection(codesSheet, currentRowNum, units);
			codesSheet.createRow(currentRowNum++);
		}

		codesSheet.setColumnWidth(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, 34 * 250);
		codesSheet.setColumnWidth(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, 65 * 250);
	}

	private int writeNameSection(final HSSFSheet codesSheet, final int currentRowNum, final List<GermplasmNameTypeDTO> germplasmNames) {
		int rowNumIndex = currentRowNum;
		int count = germplasmNames.size();
		for (final GermplasmNameTypeDTO germplasmName : germplasmNames) {
			final HSSFRow row = codesSheet.createRow(rowNumIndex++);

			HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(germplasmName.getCode());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(germplasmName.getName());

			count--;
		}
		return rowNumIndex;
	}

	private int writeAttributeSection(final HSSFSheet codesSheet, final int currentRowNum,
		final List<AttributeDTO> germplasmAttributeDTOS) {
		int rowNumIndex = currentRowNum;
		int count = germplasmAttributeDTOS.size();
		for (final AttributeDTO germplasmAttributeDTO : germplasmAttributeDTOS) {
			final HSSFRow row = codesSheet.createRow(rowNumIndex++);
			HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(germplasmAttributeDTO.getCode());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(germplasmAttributeDTO.getName());
			count--;
		}
		return rowNumIndex;
	}

	private int writeBreedingMethodSection(final HSSFSheet codesSheet, final int currentRowNum,
		final List<BreedingMethodDTO> breedingMethodDTOs) {
		int rowNumIndex = currentRowNum;
		int count = breedingMethodDTOs.size();
		for (final BreedingMethodDTO breedingMethodDTO : breedingMethodDTOs) {
			final HSSFRow row = codesSheet.createRow(rowNumIndex++);
			HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(breedingMethodDTO.getCode());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(breedingMethodDTO.getName());

			count--;
		}
		return rowNumIndex;
	}

	private int writeLocationAbbrSection(final HSSFSheet codesSheet, final int currentRowNum, final List<LocationDto> storagelocationDtos) {
		int rowNumIndex = currentRowNum;
		int count = storagelocationDtos.size();
		for (final LocationDto locationDto : storagelocationDtos) {
			final HSSFRow row = codesSheet.createRow(rowNumIndex++);

			HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(locationDto.getAbbreviation());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(locationDto.getName());
			count--;
		}
		return rowNumIndex;
	}

	private int writeUnitsSection(final HSSFSheet codesSheet, final int currentRowNum, final List<VariableDetails> units) {
		int rowNumIndex = currentRowNum;
		int count = units.size();

		for (final VariableDetails variableDetail : units) {
			final HSSFRow row = codesSheet.createRow(rowNumIndex++);
			HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variableDetail.getName());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variableDetail.getDescription());
			count--;
		}

		return rowNumIndex;
	}

	private void writeCodesHeader(final HSSFSheet codesSheet, final int currentRowNum, final String colunmName) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = codesSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_AQUA));
		cell.setCellValue(this.getMessageSource().getMessage(colunmName, null, locale));

		cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.description.column", null, locale));

	}

	private HSSFFont buildFont(final String fontName, final int fontHeight, final boolean bold) {
		final HSSFFont hSSFFont = this.wb.createFont();
		hSSFFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		hSSFFont.setFontName(fontName);
		hSSFFont.setFontHeightInPoints((short) fontHeight);
		hSSFFont.setBold(bold);
		return hSSFFont;
	}

	private void setCustomColorAtIndex(final IndexedColors indexedColor, final int red, final int green,
		final int blue) {
		final HSSFPalette customPalette = this.wb.getCustomPalette();
		customPalette.setColorAtIndex(indexedColor.index, (byte) red, (byte) green, (byte) blue);
	}

	public ResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	private Map<ExcelCellStyle, CellStyle> createStyles() {
		final Map<ExcelCellStyle, CellStyle> styles = new HashMap<>();
		final DataFormat format = this.wb.createDataFormat();

		this.setCustomColorAtIndex(IndexedColors.YELLOW, 255, 255, 204);
		this.setCustomColorAtIndex(IndexedColors.PALE_BLUE, 197, 217, 241);
		this.setCustomColorAtIndex(IndexedColors.BLUE, 91, 156, 220);
		this.setCustomColorAtIndex(IndexedColors.ORANGE, 255, 102, 0);

		this.setCustomColorAtIndex(IndexedColors.AQUA, 218, 227, 243);
		this.setCustomColorAtIndex(IndexedColors.OLIVE_GREEN, 235, 241, 222);

		final Font headerFontObservation = this.buildFont("arial", 10, true);
		final Font headerFontCodes = this.buildFont("calibri", 11, true);
		final Font fontCodes = this.buildFont("calibri", 11, false);

		final CellStyle HeadingStyleYellow = this.createStyleWithBorder();
		HeadingStyleYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		HeadingStyleYellow.setAlignment(HorizontalAlignment.CENTER);
		HeadingStyleYellow.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_YELLOW, HeadingStyleYellow);

		final CellStyle HeadingStylePaleBlue = this.createStyleWithBorder();
		HeadingStylePaleBlue.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		HeadingStylePaleBlue.setAlignment(HorizontalAlignment.CENTER);
		HeadingStylePaleBlue.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_PALE_BLUE, HeadingStylePaleBlue);

		final CellStyle HeadingStyleBlue = this.createStyleWithBorder();
		HeadingStyleBlue.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		HeadingStyleBlue.setAlignment(HorizontalAlignment.CENTER);
		HeadingStyleBlue.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_BLUE, HeadingStyleBlue);

		final CellStyle hedingStyleOrange = this.createStyleWithBorder();
		hedingStyleOrange.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
		hedingStyleOrange.setAlignment(HorizontalAlignment.CENTER);
		hedingStyleOrange.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_ORANGE, hedingStyleOrange);

		final CellStyle hedingStyleAqua = this.createStyleWithBorder();
		hedingStyleAqua.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		hedingStyleAqua.setAlignment(HorizontalAlignment.LEFT);
		hedingStyleAqua.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_AQUA, hedingStyleAqua);

		final CellStyle hedingStyleOliveGreen = this.createStyleWithBorder();
		hedingStyleOliveGreen.setFillForegroundColor(IndexedColors.OLIVE_GREEN.getIndex());
		hedingStyleOliveGreen.setAlignment(HorizontalAlignment.LEFT);
		hedingStyleOliveGreen.setFont(headerFontObservation);
		styles.put(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN, hedingStyleOliveGreen);

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