package org.ibp.api.rest.ontology;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.ApiUnitTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class FormulaResourceTest extends ApiUnitTestBase {

	private static final String ERROR_JEXL_EXCEPTION = "Some jexl exception";

	private static Locale locale = Locale.getDefault();

	@Autowired
	private FormulaService service;

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

		doReturn("").when(this.processor).evaluateFormula(anyString(), anyMap());
		final Term term = new Term();
		term.setVocabularyId(CvId.VARIABLES.getId());
		doReturn(term).when(this.termDataManager).getTermById(anyInt());
		doReturn(term).when(this.termDataManager).getTermByName(anyString());

		doReturn(Lists.newArrayList(VariableType.ENVIRONMENT_DETAIL, VariableType.TRAIT))
			.when(this.ontologyVariableDataManager).getVariableTypes(anyInt());
	}

	@Test
	public void testSave_Success() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		final int targetTermId = nextInt();
		final int inputId = nextInt();
		formulaDto.setTarget(new FormulaVariable(targetTermId, "", null));
		final String inputName = "SomeInvalidInputName";
		formulaDto.setDefinition("{{" + inputName + "}}");
		final List<FormulaVariable> inputs = new ArrayList<>();
		final FormulaVariable input = new FormulaVariable();
		input.setName(inputName);
		input.setId(inputId);
		inputs.add(input);
		formulaDto.setInputs(inputs);

		final Term term = new Term();
		term.setId(inputId);
		when(this.termDataManager.getTermByName(inputName)).thenReturn(term);

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andExpect(MockMvcResultMatchers.status().isCreated()) //
		;

		final ArgumentCaptor<FormulaDto> captor = ArgumentCaptor.forClass(FormulaDto.class);
		Mockito.verify(this.service, atLeastOnce()).save(captor.capture());
		Assert.assertThat("Should store formula definition with termid", captor.getValue().getDefinition(), is("{{" + inputId + "}}"));
	}

	@Test
	public void testCreateFormula_NoDefinition() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTarget(new FormulaVariable(nextInt(), "", null));
		formulaDto.setDefinition(null);

		doReturn(new Term()).when(this.termDataManager).getTermByName(anyString());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
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
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
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
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
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


		doReturn(null).when(this.termDataManager).getTermByName(inputName);

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
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
		doReturn(Lists.newArrayList(VariableType.ENVIRONMENT_DETAIL, VariableType.GERMPLASM_DESCRIPTOR))
			.when(this.ontologyVariableDataManager).getVariableTypes(anyInt());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
				.contentType(this.contentType) //
				.locale(locale) //
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers
				.jsonPath(
					"$.errors[0].message",
					is(this.getMessage("variable.formula.target.not.trait", new Object[] {String.valueOf(targetTermId)}))))
		;

	}

	@Test
	public void testSave_InvalidFormula() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTarget(new FormulaVariable(nextInt(), "", null));
		formulaDto.setDefinition("{{1}}");

		doThrow(new JexlException(null, ERROR_JEXL_EXCEPTION)).when(this.processor).evaluateFormula(anyString(), anyMap());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
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
			.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/formula/{formulaId}", this.cropName, formulaId)) //
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
			.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/formula/{formulaId}", this.cropName, formulaId)) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is(getMessage("variable.formula.invalid.is.not.deletable"))))
		;
	}

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	private String getMessage(final String code, final Object[] args) {
		return this.resourceBundleMessageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}
}
