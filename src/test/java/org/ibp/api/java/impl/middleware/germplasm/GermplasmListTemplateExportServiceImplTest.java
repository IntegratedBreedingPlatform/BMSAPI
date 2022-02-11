package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.java.impl.middleware.germplasm.workbook.common.ExcelCellStyleBuilder;
import org.ibp.api.java.impl.middleware.germplasm.workbook.generator.OntologyVariableSheetGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GermplasmListTemplateExportServiceImplTest {

	@InjectMocks
	private GermplasmListTemplateExportServiceImpl germplasmListTemplateExportServiceImpl;

	@Mock
	private OntologyVariableSheetGenerator ontologyVariableSheetGenerator;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private HSSFWorkbook workbook;

	@Mock
	private HSSFSheet observationSheet;

	@Mock
	private HSSFRow headerRow;

	@Mock
	private HSSFCell header1;

	@Mock
	private HSSFCell header2;

	@Mock
	private HSSFCell header3;

	@Mock
	private HSSFCell header4;

	@Mock
	private HSSFCell header5;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private ExcelCellStyleBuilder sheetStyles;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	private void setupMockObservationSheet() {
		Mockito.doReturn(this.observationSheet).when(this.workbook).createSheet("Observation");
		Mockito.doReturn(this.headerRow).when(this.observationSheet).createRow(0);
		Mockito.doReturn(this.header1).when(this.headerRow).createCell(0, CellType.STRING);
		Mockito.doReturn(this.header2).when(this.headerRow).createCell(1, CellType.STRING);
		Mockito.doReturn(this.header3).when(this.headerRow).createCell(2, CellType.STRING);
		Mockito.doReturn(this.header4).when(this.headerRow).createCell(3, CellType.STRING);
		Mockito.doReturn(this.header5).when(this.headerRow).createCell(4, CellType.STRING);
	}

	private void setupMockMessages() {
		Mockito.doReturn("Observation").when(this.messageSource)
			.getMessage(ArgumentMatchers.eq("export.germplasm.list.template.sheet.observation"), ArgumentMatchers.isNull(),
				ArgumentMatchers.any());
		Mockito.doReturn("ENTRY_NO").when(this.messageSource)
			.getMessage(ArgumentMatchers.eq("export.germplasm.list.template.entry.no.column"), ArgumentMatchers.isNull(),
				ArgumentMatchers.any());
		Mockito.doReturn("GID").when(this.messageSource)
			.getMessage(ArgumentMatchers.eq("export.germplasm.list.template.gid.column"), ArgumentMatchers.isNull(),
				ArgumentMatchers.any());
		Mockito.doReturn("GUID").when(this.messageSource)
			.getMessage(ArgumentMatchers.eq("export.germplasm.list.template.guid.column"), ArgumentMatchers.isNull(),
				ArgumentMatchers.any());
		Mockito.doReturn("DESIGNATION").when(this.messageSource)
			.getMessage(ArgumentMatchers.eq("export.germplasm.list.template.designation.column"), ArgumentMatchers.isNull(),
				ArgumentMatchers.any());
		Mockito.doReturn("ENTRY_CODE").when(this.messageSource)
			.getMessage(ArgumentMatchers.eq("export.germplasm.list.template.entry.code.column"), ArgumentMatchers.isNull(),
				ArgumentMatchers.any());
	}

	@Test
	public void testWriteEntryDetailsSheet() {
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final Variable variable = new Variable();
		variable.setId(new Random().nextInt());
		final List<Variable> entryDetailVariables = Collections.singletonList(variable);
		final ArgumentCaptor<VariableFilter> variableFilterCaptor = ArgumentCaptor.forClass(VariableFilter.class);
		Mockito.doReturn(entryDetailVariables).when(this.ontologyVariableDataManager).getWithFilter(variableFilterCaptor.capture());
		this.germplasmListTemplateExportServiceImpl.writeEntryDetailsSheet(programUUID);
		Assert.assertEquals(programUUID, variableFilterCaptor.getValue().getProgramUuid());
		Assert.assertTrue(variableFilterCaptor.getValue().getVariableTypes().contains(VariableType.ENTRY_DETAIL));
		Mockito.verify(this.ontologyVariableSheetGenerator).writeOntologyVariableSheet(this.workbook, "export.germplasm.list.template.sheet.entry.details",
			entryDetailVariables);
	}

	@Test
	public void testWriteObservationSheet_ImportListsFormat(){
		this.setupMockMessages();
		this.setupMockObservationSheet();

		this.germplasmListTemplateExportServiceImpl.writeObservationSheet(false);
		Mockito.verify(this.header1).setCellValue("ENTRY_NO");
		Mockito.verify(this.header2).setCellValue("GID");
		Mockito.verify(this.header3).setCellValue("GUID");
		Mockito.verify(this.header4).setCellValue("DESIGNATION");
		Mockito.verify(this.header5).setCellValue("ENTRY_CODE");
	}

	@Test
	public void testWriteObservationSheet_ImportListsUpdateFormat(){
		this.setupMockMessages();
		this.setupMockObservationSheet();

		this.germplasmListTemplateExportServiceImpl.writeObservationSheet(true);
		Mockito.verify(this.header1).setCellValue("ENTRY_NO");
		Mockito.verify(this.header2).setCellValue("ENTRY_CODE");
		Mockito.verifyNoInteractions(this.header3);
		Mockito.verifyNoInteractions(this.header4);
		Mockito.verifyNoInteractions(this.header5);
	}

}
