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
import org.apache.poi.ss.util.CellRangeAddress;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
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
	private static final int COLUMN_WIDTH_PADDING = 6;
	private static final int CHARACTER_WIDTH = 250;

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
		IMPORT_HEADERS.put("export.germplasm.list.template.progenitor1.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.progenitor2.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
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
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.breeding.method.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.progenitor1.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.progenitor2.column", ExcelCellStyle.HEADING_STYLE_YELLOW);
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

	@Autowired
	protected GermplasmService germplasmService;

	@Autowired
	protected BreedingMethodService breedingMethodService;

	@Autowired
	protected GermplasmAttributeService germplasmAttributeService;

	@Autowired
	protected OntologyVariableDataManager ontologyVariableDataManager;

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
		STYLE_AQUA_GREEN_WITH_BORDER,
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
			final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
			errors.reject("cannot.export.as.xls.germplasm.template", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	private File generateTemplateFile(final String fileNamePath, final String cropName, final String programUUID,
		final boolean isGermplasmUpdateFormat) throws IOException {
		this.wb = new HSSFWorkbook();

		final File file = new File(fileNamePath);
		this.sheetStylesMap = this.createStyles();
		this.writeObservationSheet(isGermplasmUpdateFormat);

		final VariableFilter passportFilter = new VariableFilter();
		passportFilter.setProgramUuid(programUUID);
		passportFilter.addVariableType(org.generationcp.middleware.domain.ontology.VariableType.GERMPLASM_PASSPORT);
		final List<Variable> passportVariables = this.ontologyVariableDataManager.getWithFilter(passportFilter);

		this.writeOntologyVariableSheet("export.germplasm.list.template.sheet.passport", passportVariables);

		final VariableFilter attributeFilter = new VariableFilter();
		attributeFilter.setProgramUuid(programUUID);
		attributeFilter.addVariableType(org.generationcp.middleware.domain.ontology.VariableType.GERMPLASM_ATTRIBUTE);
		final List<Variable> attributeVariables = this.ontologyVariableDataManager.getWithFilter(attributeFilter);

		this.writeOntologyVariableSheet("export.germplasm.list.template.sheet.attributes", attributeVariables);

		this.writeOtherCodesSheet(cropName, programUUID, isGermplasmUpdateFormat);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			this.wb.write(fos);

		}
		return file;
	}

	private void writeOntologyVariableSheet(final String sheetName, final List<Variable> variableList) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet hSSFSheet = this.wb.createSheet(this.getMessageSource().getMessage(sheetName, null, locale));
		hSSFSheet.setDefaultRowHeightInPoints(16);
		int currentRowNum = 0;

		currentRowNum = this.writeOntologyVariableHeader(hSSFSheet, currentRowNum);
		int count = variableList.size();

		for (final Variable variable : variableList) {
			final HSSFRow row = hSSFSheet.createRow(currentRowNum++);

			final CellStyle cellStyle = count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER);

			HSSFCell cell = row.createCell(0, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variable.getName());

			cell = row.createCell(1, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
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
		HSSFRow row = sheet.createRow(rowNumIndex++);

		HSSFCell cell = row.createCell(0, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_GREEN_WITH_BORDER));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.variable", null, locale));
		sheet.setColumnWidth(0, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(1, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_GREEN_WITH_BORDER));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.alias", null, locale));
		sheet.setColumnWidth(1, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(2, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.description", null, locale));
		sheet.setColumnWidth(2, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(3, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.property", null, locale));
		sheet.setColumnWidth(3, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(4, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.scale", null, locale));
		sheet.setColumnWidth(4, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(5, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.method", null, locale));
		sheet.setColumnWidth(5, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(6, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.data.type", null, locale));
		sheet.setColumnWidth(6, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		cell = row.createCell(7, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.expected.values", null, locale));
		sheet.setColumnWidth(7, (cell.getStringCellValue().length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH * 2);

		return rowNumIndex;
	}

	private String getDataTypeCode(final Integer dataTypeId) {
		return DataType.getById(dataTypeId).getDataTypeCode();
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
			final Map.Entry<String, ExcelCellStyle> entry = iterator.next();
			final HSSFCell cell = row.createCell(index, CellType.STRING);
			cell.setCellStyle(this.sheetStylesMap.get(entry.getValue()));
			final String headerColumn = this.getMessageSource().getMessage(entry.getKey(), null, locale);
			cell.setCellValue(headerColumn);
			observationSheet.setColumnWidth(index, (headerColumn.length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH);
			index++;
		}
	}

	private void writeOtherCodesSheet(final String cropName, final String programUUID, final boolean isGermplasmUpdateFormat) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet otherCodesSheet =
			this.wb.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.other.codes", null, locale));
		otherCodesSheet.setDefaultRowHeightInPoints(16);

		int currentRowNum = 0;

		final VariableFilter inventoryPropertyFilter = new VariableFilter();
		inventoryPropertyFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<Variable> units = this.ontologyVariableDataManager.getWithFilter(inventoryPropertyFilter);


		final List<LocationDto> storageLocations =
			this.locationService
				.getLocations(cropName,
					new LocationSearchRequest(programUUID, GermplasmExcelTemplateExportServiceImpl.STORAGE_LOCATION_TYPE, null, null, null,
						false), null);

		final List<LocationDto> locations =
			this.locationService
				.getLocations(cropName,
					new LocationSearchRequest(programUUID, GermplasmExcelTemplateExportServiceImpl.LOCATION_TYPE, null, null, null, false),
					null);

		final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest(programUUID, null, false);
		final List<BreedingMethodDTO> breedingMethodDTOs = this.breedingMethodService.getBreedingMethods(searchRequest, null);

		final List<GermplasmNameTypeDTO> germplasmNames = this.germplasmService.filterGermplasmNameTypes(null);

		this.writeCodesHeader(otherCodesSheet, currentRowNum++, "export.germplasm.list.template.breeding.methods.column");
		currentRowNum = this.writeBreedingMethodSection(otherCodesSheet, currentRowNum, breedingMethodDTOs);
		otherCodesSheet.createRow(currentRowNum++);

		this.writeCodesHeader(otherCodesSheet, currentRowNum++, "export.germplasm.list.template.location.abbr.column");
		currentRowNum = this.writeLocationAbbrSection(otherCodesSheet, currentRowNum, locations);
		otherCodesSheet.createRow(currentRowNum++);

		this.writeCodesHeader(otherCodesSheet, currentRowNum++, "export.germplasm.list.template.name.column");
		currentRowNum = this.writeNameSection(otherCodesSheet, currentRowNum, germplasmNames);
		otherCodesSheet.createRow(currentRowNum++);

		if (!isGermplasmUpdateFormat) {
			this.writeCodesHeader(otherCodesSheet, currentRowNum++, "export.germplasm.list.template.storage.location.abbr.column");
			currentRowNum = this.writeLocationAbbrSection(otherCodesSheet, currentRowNum, storageLocations);
			otherCodesSheet.createRow(currentRowNum++);

			this.writeCodesHeader(otherCodesSheet, currentRowNum++, "export.germplasm.list.template.units.column");
			currentRowNum = this.writeUnitsSection(otherCodesSheet, currentRowNum, units);
			otherCodesSheet.createRow(currentRowNum++);
		}

		otherCodesSheet.setColumnWidth(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, 34 * 250);
		otherCodesSheet.setColumnWidth(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, 65 * 250);
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

	private int writeLocationAbbrSection(final HSSFSheet codesSheet, final int currentRowNum, final List<LocationDto> storageLocations) {
		int rowNumIndex = currentRowNum;
		int count = storageLocations.size();
		for (final LocationDto locationDto : storageLocations) {
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

	private int writeUnitsSection(final HSSFSheet codesSheet, final int currentRowNum, final List<Variable> units) {
		int rowNumIndex = currentRowNum;
		int count = units.size();

		for (final Variable variable : units) {
			final HSSFRow row = codesSheet.createRow(rowNumIndex++);
			HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variable.getName());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStylesMap.get(ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variable.getDefinition());
			count--;
		}

		return rowNumIndex;
	}

	private void writeCodesHeader(final HSSFSheet codesSheet, final int currentRowNum, final String columnName) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = codesSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.sheetStylesMap.get(ExcelCellStyle.HEADING_STYLE_AQUA));
		cell.setCellValue(this.getMessageSource().getMessage(columnName, null, locale));

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
