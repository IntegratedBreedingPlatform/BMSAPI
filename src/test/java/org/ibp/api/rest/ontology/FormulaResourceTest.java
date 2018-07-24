package org.ibp.api.rest.ontology;

import org.apache.commons.jexl3.JexlException;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.ApiUnitTestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Locale;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class FormulaResourceTest extends ApiUnitTestBase {

	private static final String ERROR_JEXL_EXCEPTION = "Some jexl exception";

	private static Locale locale = Locale.getDefault();


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public FormulaService formulaService() {
			return Mockito.mock(FormulaService.class);
		}

		@Bean
		@Primary
		public DerivedVariableProcessor derivedVariableProcessor() {
			return Mockito.mock(DerivedVariableProcessor.class);
		}
	}


	@Autowired
	private FormulaService service;

	@Autowired
	private DerivedVariableProcessor processor;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Before
	public void setup() throws Exception {
		super.setUp();

		doReturn("").when(this.processor).evaluateFormula(anyString(), anyMap());
	}

	@Test
	public void testCreateFormula_NoDefinition() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTargetTermId(RandomUtils.nextInt());
		formulaDto.setDefinition(null);

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
				.contentType(this.contentType) //
				.locale(locale)
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.errors[0].message", is(this.getMessage("variable.formula.definition.required")))) //
		;

		Mockito.verify(this.service, Mockito.times(0)).save(formulaDto);
	}

	@Test
	public void testSave_NoTarget() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTargetTermId(null);
		formulaDto.setDefinition("{{1}}");

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
				.contentType(this.contentType) //
				.locale(locale)
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is(this.getMessage("variable.formula.targetid.required")))) //
		;

		Mockito.verify(this.service, Mockito.times(0)).save(formulaDto);
	}

	@Test
	public void testSave_InvalidFormula() throws Exception {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTargetTermId(RandomUtils.nextInt());
		formulaDto.setDefinition("{{1}}");

		doThrow(new JexlException(null, ERROR_JEXL_EXCEPTION)).when(this.processor).evaluateFormula(anyString(), anyMap());

		this.mockMvc //
			.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/formula/", this.cropName) //
				.contentType(this.contentType) //
				.locale(locale)
				.content(this.convertObjectToByte(formulaDto))) //
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", containsString(ERROR_JEXL_EXCEPTION))) //
		;

		Mockito.verify(this.service, Mockito.times(0)).save(formulaDto);
	}

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
