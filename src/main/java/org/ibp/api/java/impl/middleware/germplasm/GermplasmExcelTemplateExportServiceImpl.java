package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.io.Files;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.germplasm.GermplasmTemplateExportService;
import org.ibp.api.java.impl.middleware.germplasm.workbook.common.ExcelCellStyleBuilder;
import org.ibp.api.java.impl.middleware.germplasm.workbook.generator.OntologyVariableSheetGenerator;
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

	private static final Map<String, ExcelCellStyleBuilder.ExcelCellStyle> IMPORT_HEADERS;

	static {
		IMPORT_HEADERS = new LinkedHashMap<>();
		IMPORT_HEADERS.put("export.germplasm.list.template.entry.no.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.lname.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.drvnm.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.preferred.name.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.entry.code.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.location.abbr.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.reference.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.creation.date.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.breeding.method.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.progenitor1.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.progenitor2.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS.put("export.germplasm.list.template.note.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_PALE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.storage.location.abbr.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.units.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.amount.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.stock.id.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_BLUE);
		IMPORT_HEADERS.put("export.germplasm.list.template.pui.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_ORANGE);
	}

	private static final Map<String, ExcelCellStyleBuilder.ExcelCellStyle> IMPORT_HEADERS_FOR_UPDATE;

	static {
		IMPORT_HEADERS_FOR_UPDATE = new LinkedHashMap<>();
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.gid.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.guid.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_ORANGE);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.breeding.method.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.progenitor1.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.progenitor2.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.preferred.name.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.location.abbr.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.creation.date.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.reference.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.drvnm.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_HEADERS_FOR_UPDATE.put("export.germplasm.list.template.note.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_PALE_BLUE);
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

	@Autowired
	protected OntologyVariableSheetGenerator ontologyVariableSheetGenerator;
	
	private HSSFWorkbook wb;

	private ExcelCellStyleBuilder sheetStyles;

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
		this.sheetStyles =  new ExcelCellStyleBuilder(this.wb);
		this.writeObservationSheet(isGermplasmUpdateFormat);

		final VariableFilter passportFilter = new VariableFilter();
		passportFilter.setProgramUuid(programUUID);
		passportFilter.addVariableType(org.generationcp.middleware.domain.ontology.VariableType.GERMPLASM_PASSPORT);
		final List<Variable> passportVariables = this.ontologyVariableDataManager.getWithFilter(passportFilter);

		this.ontologyVariableSheetGenerator.writeOntologyVariableSheet(this.wb,"export.germplasm.list.template.sheet.passport", passportVariables);

		final VariableFilter attributeFilter = new VariableFilter();
		attributeFilter.setProgramUuid(programUUID);
		attributeFilter.addVariableType(org.generationcp.middleware.domain.ontology.VariableType.GERMPLASM_ATTRIBUTE);
		final List<Variable> attributeVariables = this.ontologyVariableDataManager.getWithFilter(attributeFilter);

		this.ontologyVariableSheetGenerator.writeOntologyVariableSheet(this.wb,"export.germplasm.list.template.sheet.attributes", attributeVariables);

		this.writeOtherCodesSheet(cropName, isGermplasmUpdateFormat);

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

		final Map<String, ExcelCellStyleBuilder.ExcelCellStyle> headers = isGermplasmUpdateFormat ? IMPORT_HEADERS_FOR_UPDATE : IMPORT_HEADERS;
		final Iterator<Map.Entry<String, ExcelCellStyleBuilder.ExcelCellStyle>> iterator = headers.entrySet().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			final Map.Entry<String, ExcelCellStyleBuilder.ExcelCellStyle> entry = iterator.next();
			final HSSFCell cell = row.createCell(index, CellType.STRING);
			cell.setCellStyle(this.sheetStyles.getCellStyle(entry.getValue()));
			final String headerColumn = this.getMessageSource().getMessage(entry.getKey(), null, locale);
			cell.setCellValue(headerColumn);
			observationSheet.setColumnWidth(index, (headerColumn.length() + COLUMN_WIDTH_PADDING) * CHARACTER_WIDTH);
			index++;
		}
	}

	private void writeOtherCodesSheet(final String cropName, final boolean isGermplasmUpdateFormat) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet otherCodesSheet =
			this.wb.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.other.codes", null, locale));
		otherCodesSheet.setDefaultRowHeightInPoints(16);

		int currentRowNum = 0;

		final VariableFilter inventoryPropertyFilter = new VariableFilter();
		inventoryPropertyFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<Variable> units = this.ontologyVariableDataManager.getWithFilter(inventoryPropertyFilter);


		final List<LocationDTO> storageLocations =
			this.locationService
				.searchLocations(cropName,
					new LocationSearchRequest(GermplasmExcelTemplateExportServiceImpl.STORAGE_LOCATION_TYPE, null, null, null), null, null);

		final List<LocationDTO> locations =
			this.locationService
				.searchLocations(cropName,
					new LocationSearchRequest(GermplasmExcelTemplateExportServiceImpl.LOCATION_TYPE, null, null, null),
					null, null);

		final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest();
		final List<BreedingMethodDTO> breedingMethodDTOs = this.breedingMethodService.searchBreedingMethods(searchRequest, null, null);

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
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(germplasmName.getCode());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
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
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(breedingMethodDTO.getCode());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(breedingMethodDTO.getName());

			count--;
		}
		return rowNumIndex;
	}

	private int writeLocationAbbrSection(final HSSFSheet codesSheet, final int currentRowNum, final List<LocationDTO> storageLocations) {
		int rowNumIndex = currentRowNum;
		int count = storageLocations.size();
		for (final LocationDTO locationDto : storageLocations) {
			final HSSFRow row = codesSheet.createRow(rowNumIndex++);

			HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(locationDto.getAbbreviation());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
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
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_AQUA_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variable.getName());

			cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
			cell.setCellStyle(count != 1 ? this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_BORDER) :
				this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.STYLE_OLIVE_GREEN_WITH_LATERAL_AND_BOTTOM_BORDER));
			cell.setCellValue(variable.getDefinition());
			count--;
		}

		return rowNumIndex;
	}

	private void writeCodesHeader(final HSSFSheet codesSheet, final int currentRowNum, final String columnName) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = codesSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_FIRST_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_AQUA));
		cell.setCellValue(this.getMessageSource().getMessage(columnName, null, locale));

		cell = row.createCell(GermplasmExcelTemplateExportServiceImpl.CODES_SHEET_SECOND_COLUMN_INDEX, CellType.STRING);
		cell.setCellStyle(this.sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_OLIVE_GREEN));
		cell.setCellValue(this.getMessageSource().getMessage("export.germplasm.list.template.description.column", null, locale));

	}

	public ResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

}
