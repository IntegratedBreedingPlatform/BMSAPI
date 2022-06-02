package org.ibp.api.rest.ontology;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.jexl3.JexlException;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.ApiUnitTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class FormulaResourceTest extends ApiUnitTestBase {

	private static final String ERROR_JEXL_EXCEPTION = "Some jexl exception";

	private static final String PROGRAM_UUID = "50a7e02e-db60-4240-bd64-417b34606e46";


	private static final Locale locale = Locale.getDefault();

	@Autowired
	private FormulaService service;

	@Autowired
	private ProgramService programService;

	@Autowired
	private DerivedVariableProcessor processor;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Autowired
	protected TermDataManager termDataManager;

	@Autowired
	protected OntologyVariableDataManager ontologyVariableDataManager;

	@Before
	public void setup() throws Exception {
		super.setUp();

		doReturn("").when(this.processor).evaluateFormula(anyString(), anyMapOf(String.class, Object.class));
		final Term term = new Term();
		term.setVocabularyId(CvId.VARIABLES.getId());
		doReturn(term).when(this.termDataManager).getTermById(anyInt());
		doReturn(term).when(this.termDataManager).getTermByName(anyString());

		doReturn(Lists.newArrayList(VariableType.ENVIRONMENT_DETAIL, VariableType.TRAIT))
			.when(this.ontologyVariableDataManager).getVariableTypes(anyInt());
		doReturn(Optional.of(DataType.NUMERIC_VARIABLE)).when(this.ontologyVariableDataManager).getDataType(anyInt());

		final Project project = new Project();
		project.setUniqueID(FormulaResourceTest.PROGRAM_UUID);
		project.setProjectId(1l);
		ContextHolder.setCurrentCrop(this.cropName);
		ContextHolder.setCurrentProgram(this.programUuid);
		Mockito.doReturn(project).when(this.programService).getLastOpenedProjectAnyUser();
	}

	@Test
	public void testSave_Success() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		final int targetTermId = nextInt();
		final int inputId = nextInt();
		formulaDto.setTarget(new FormulaVariable(targetTermId, "", null));
		final String inputName = "SomeInvalidInputName";
		formulaDto.setDefinition("{{" + inputName + "}} + {{" + inputName + "}}");
		final List<FormulaVariable> inputs = new ArrayList<>();
		final FormulaVariable input = new FormulaVariable();
		input.setName(inputName);
		input.setId(inputId);
		inputs.add(input);
		inputs.add(input);
		formulaDto.setInputs(inputs);

		final Term term = new Term();
		term.setId(inputId);
		when(this.termDataManager.getTermByNameAndCvId(inputName, CvId.VARIABLES.getId())).thenReturn(term);

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/crops/{cropname}/formula?programUUID=" + this.programUuid, this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andExpect(MockMvcResultMatchers.status().isCreated()) //
		;

		final ArgumentCaptor<FormulaDto> captor = ArgumentCaptor.forClass(FormulaDto.class);
		Mockito.verify(this.service, atLeastOnce()).save(captor.capture());
		Assert.assertThat("Should store formula definition with termid", captor.getValue().getDefinition(),
			is("{{" + inputId + "}} + {{" + inputId + "}}"));
	}

	@Test
	public void testCreateFormula_NoDefinition() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTarget(new FormulaVariable(nextInt(), "", null));
		formulaDto.setDefinition(null);

		doReturn(new Term()).when(this.termDataManager).getTermByName(anyString());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/crops/{cropname}/formula?programUUID=" + this.programUuid, this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.errors[0].message", is(this.getMessage("variable.formula.definition.required")))) //
		;

	}

	@Test
	public void testSave_NoTarget() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setDefinition("{{1}}");

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/crops/{cropname}/formula?programUUID=" + this.programUuid, this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is(this.getMessage("variable.formula.targetid.required")))) //
		;

	}

	@Test
	public void testSave_TargetNotExist() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		final int targetTermId = nextInt();
		formulaDto.setTarget(new FormulaVariable(targetTermId, "", null));
		formulaDto.setDefinition("{{1}}");

		doReturn(null).when(this.termDataManager).getTermById(anyInt());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/crops/{cropname}/formula?programUUID=" + this.programUuid, this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers
				.jsonPath(
					"$.errors[0].message",
					is(this.getMessage("id.does.not.exist", new Object[] {"target variable", String.valueOf(targetTermId)}))))
		;

	}

	@Test
	public void testSave_InputNotExist() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		final int targetTermId = nextInt();
		formulaDto.setTarget(new FormulaVariable(targetTermId, "", null));
		final String inputName = "SomeInvalidInputName";
		formulaDto.setDefinition("{{" + inputName + "}}");


		doReturn(null).when(this.termDataManager).getTermByNameAndCvId(inputName, CvId.VARIABLES.getId());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/crops/{cropname}/formula?programUUID=" + this.programUuid, this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers
				.jsonPath(
					"$.errors[0].message",
					is(this.getMessage("variable.input.not.exists", new Object[] {inputName}))))
		;

	}

	@Test
	public void testSave_TargetNotATrait() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		final int targetTermId = nextInt();
		formulaDto.setTarget(new FormulaVariable(targetTermId, "", null));
		final String inputName = "SomeInvalidInputName";
		formulaDto.setDefinition("{{" + inputName + "}}");

		doReturn(new Term()).when(this.termDataManager).getTermByName(inputName);
		doReturn(Lists.newArrayList(VariableType.TREATMENT_FACTOR, VariableType.SELECTION_METHOD))
			.when(this.ontologyVariableDataManager).getVariableTypes(anyInt());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/crops/{cropname}/formula?programUUID=" + this.programUuid, this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers
				.jsonPath(
					"$.errors[0].message",
					is(this.getMessage("variable.formula.target.not.valid", new Object[] {String.valueOf(targetTermId)}))))
		;

	}

	@Test
	public void testSave_InvalidFormula() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTarget(new FormulaVariable(nextInt(), "", null));
		formulaDto.setDefinition("{{1}}");

		final int inputId = nextInt();
		final Term term = new Term();
		term.setId(inputId);
		when(this.termDataManager.getTermByNameAndCvId(anyString(), anyInt())).thenReturn(term);


		doThrow(new JexlException(null, ERROR_JEXL_EXCEPTION)).when(this.processor).evaluateFormula(anyString(), anyMapOf(String.class, Object.class));

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/crops/{cropname}/formula?programUUID=" + this.programUuid, this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", containsString(ERROR_JEXL_EXCEPTION))) //
		;

	}

	@Test
	public void testDelete() throws Exception {
		final Integer formulaId = nextInt();
		final FormulaDto formulaDto = new FormulaDto();
		final Optional<FormulaDto> formula = Optional.of(formulaDto);
		formulaDto.setFormulaId(formulaId);
		formulaDto.setTarget(new FormulaVariable(nextInt(), "", null));

		doReturn(formula).when(this.service).getById(formulaId);
		doReturn(false).when(this.ontologyVariableDataManager).isVariableUsedInStudy(formulaDto.getTarget().getId());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.delete("/crops/{cropname}/formula/{formulaId}?programUUID=" + this.programUuid, this.cropName, formulaId)) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.status().isNoContent()) //
		;
	}

	@Test
	public void testDelete_VariableUsedInStudy() throws Exception {
		final Integer formulaId = nextInt();
		final FormulaDto formulaDto = new FormulaDto();
		final Optional<FormulaDto> formula = Optional.of(formulaDto);
		formulaDto.setFormulaId(formulaId);
		formulaDto.setTarget(new FormulaVariable(nextInt(), "", null));

		doReturn(formula).when(this.service).getById(formulaId);
		doReturn(true).when(this.ontologyVariableDataManager).isVariableUsedInStudy(formulaDto.getTarget().getId());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.delete("/crops/{cropname}/formula/{formulaId}?programUUID=" + this.programUuid, this.cropName, formulaId)) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.status().isNoContent())
		;
	}

	@Test
	public void testUpdate() throws Exception {
		final Integer formulaId = nextInt();
		final FormulaDto formulaDto = this.buildFormulaDto(formulaId);
		final FormulaVariable input = formulaDto.getInputs().get(0);
		final Term term = new Term();
		term.setId(input.getId());

		final Optional<FormulaDto> formula = Optional.of(formulaDto);

		when(this.termDataManager.getTermByNameAndCvId(input.getName(), CvId.VARIABLES.getId())).thenReturn(term);

		doReturn(formula).when(this.service).getById(formulaId);

		this.mockMvc //
			.perform(MockMvcRequestBuilders.put("/crops/{cropname}/formula/{formulaId}?programUUID=" + this.programUuid, this.cropName, formulaId) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
		;
	}

	@Test
	public void testUpdate_formulaIdNotExists() throws Exception {
		final Integer formulaId = nextInt();
		final FormulaDto formulaDto = this.buildFormulaDto(formulaId);
		final FormulaVariable input = formulaDto.getInputs().get(0);
		final Term term = new Term();
		term.setId(input.getId());

		final Optional<FormulaDto> formula = Optional.of(formulaDto);

		when(this.termDataManager.getTermByName(input.getName())).thenReturn(term);

		doReturn(Optional.absent()).when(this.service).getById(formulaId);

		this.mockMvc //
			.perform(MockMvcRequestBuilders.put("/crops/{cropname}/formula/{formulaId}?programUUID=" + this.programUuid, this.cropName, formulaId) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is(
				this.getMessage("variable.formula.not.exist", new Integer[] {formulaDto.getFormulaId()})))) //
		;
	}

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	private String getMessage(final String code, final Object[] args) {
		return this.resourceBundleMessageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

	private FormulaDto buildFormulaDto(final Integer formulaId) {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setFormulaId(formulaId);
		final int targetTermId = nextInt();
		final int inputId = nextInt();
		formulaDto.setTarget(new FormulaVariable(targetTermId, "", null));
		final String inputName = "SomeInvalidInputName";
		formulaDto.setDefinition("{{" + inputName + "}} + {{" + inputName + "}}");
		final List<FormulaVariable> inputs = new ArrayList<>();
		final FormulaVariable input = this.buildInput(inputName, inputId);
		inputs.add(input);
		inputs.add(input);
		formulaDto.setInputs(inputs);

		return formulaDto;
	}

	private FormulaVariable buildInput(final String inputName, final int inputId) {
		final FormulaVariable input = new FormulaVariable();
		input.setName(inputName);
		input.setId(inputId);
		return input;
	}
}
