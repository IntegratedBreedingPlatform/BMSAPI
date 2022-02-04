package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.io.Files;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmListTemplateExportService;
import org.ibp.api.java.impl.middleware.germplasm.workbook.common.ExcelCellStyleBuilder;
import org.ibp.api.java.impl.middleware.germplasm.workbook.generator.OntologyVariableSheetGenerator;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GermplasmListTemplateExportServiceImpl implements GermplasmListTemplateExportService {

	private static final String FILE_NAME_IMPORT = "GermplasmListImportTemplate_";
	private static final String FILE_NAME_IMPORT_UPDATE = "GermplasmListImportUpdateTemplate_";
	private static final String FILE_NAME_EXTENSION = ".xls";
	private static final int COLUMN_WIDTH_PADDING = 6;
	private static final int CHARACTER_WIDTH = 250;
	private static final Map<String, ExcelCellStyleBuilder.ExcelCellStyle> IMPORT_LIST_HEADERS;

	static {
		IMPORT_LIST_HEADERS = new LinkedHashMap<>();
		IMPORT_LIST_HEADERS.put("export.germplasm.list.template.gid.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_LIST_HEADERS.put("export.germplasm.list.template.guid.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_LIST_HEADERS.put("export.germplasm.list.template.designation.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_LIST_HEADERS.put("export.germplasm.list.template.entry.code.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
	}

	private static final Map<String, ExcelCellStyleBuilder.ExcelCellStyle> IMPORT_LIST_UPDATES_HEADERS;

	static {
		IMPORT_LIST_UPDATES_HEADERS = new LinkedHashMap<>();
		IMPORT_LIST_UPDATES_HEADERS.put("export.germplasm.list.template.entry.no.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
		IMPORT_LIST_UPDATES_HEADERS.put("export.germplasm.list.template.entry.code.column", ExcelCellStyleBuilder.ExcelCellStyle.HEADING_STYLE_YELLOW);
	}

	private ExcelCellStyleBuilder sheetStyles;

	private HSSFWorkbook wb;

	@Autowired
	protected ResourceBundleMessageSource messageSource;

	@Autowired
	protected OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	protected OntologyVariableSheetGenerator ontologyVariableSheetGenerator;

	@Override
	public File export(final String cropName, final String programUUID, final boolean isGermplasmListUpdateFormat) {
		try {
			final File temporaryFolder = Files.createTempDir();
			final String filename = isGermplasmListUpdateFormat ? GermplasmListTemplateExportServiceImpl.FILE_NAME_IMPORT_UPDATE :
				GermplasmListTemplateExportServiceImpl.FILE_NAME_IMPORT;
			final String fileNameFullPath =	temporaryFolder.getAbsolutePath() + File.separator + filename + cropName.toLowerCase()
				+ GermplasmListTemplateExportServiceImpl.FILE_NAME_EXTENSION;
			return this.generateTemplateFile(fileNameFullPath, cropName, programUUID, isGermplasmListUpdateFormat);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
			errors.reject("cannot.export.as.xls.germplasm.template", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	private File generateTemplateFile(final String fileNamePath, final String cropName, final String programUUID, final boolean isGermplasmListUpdateFormat) throws IOException {
		this.wb = new HSSFWorkbook();

		final File file = new File(fileNamePath);
		this.sheetStyles = new ExcelCellStyleBuilder(this.wb);
		this.writeObservationSheet(isGermplasmListUpdateFormat);

		final VariableFilter entryDetailFilter = new VariableFilter();

		entryDetailFilter.setProgramUuid(programUUID);
		entryDetailFilter.addVariableType(VariableType.ENTRY_DETAIL);
		final List<Variable> entryDetailVariables = this.ontologyVariableDataManager.getWithFilter(entryDetailFilter);
		this.ontologyVariableSheetGenerator.writeOntologyVariableSheet(this.wb, "export.germplasm.list.template.sheet.entry.details",
			entryDetailVariables);

		try (final FileOutputStream fos = new FileOutputStream(file)) {
			this.wb.write(fos);

		}
		return file;
	}

	private void writeObservationSheet(final boolean isGermplasmListUpdateFormat) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet observationSheet =
			this.wb.createSheet(this.getMessageSource().getMessage("export.germplasm.list.template.sheet.observation", null, locale));
		observationSheet.setDefaultRowHeightInPoints(16);
		this.writeObservationHeader(observationSheet, isGermplasmListUpdateFormat);

	}

	private void writeObservationHeader(final HSSFSheet observationSheet, final boolean isGermplasmListUpdateFormat) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = observationSheet.createRow(0);

		final Map<String, ExcelCellStyleBuilder.ExcelCellStyle> headers =
			isGermplasmListUpdateFormat ? IMPORT_LIST_UPDATES_HEADERS : IMPORT_LIST_HEADERS;
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

	public ResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}
}
