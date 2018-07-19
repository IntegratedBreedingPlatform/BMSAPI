package org.ibp.api.rest.ontology;

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

public class FormulaResourceTest extends ApiUnitTestBase {

	private static final String ERROR_FORMULA_REQUIRED = "Formula required";
	private static final String ERROR_FORMULA_TARGET_REQUIRED = "Target required";
	private static final String ERROR_FORMULA_DEFINITION_REQUIRED = "Target required";
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

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
