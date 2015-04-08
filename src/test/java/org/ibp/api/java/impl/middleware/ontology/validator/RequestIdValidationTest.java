package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;

import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

public class RequestIdValidationTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public OntologyManagerService ontologyManagerService() {
			return Mockito.mock(OntologyManagerService.class);
		}

		@Bean
		@Primary
		public RequestIdValidator requestIdValidator() {
			return Mockito.mock(RequestIdValidator.class);
		}
	}

	@Autowired
	private OntologyManagerService ontologyManagerService;

	@Autowired
	RequestIdValidator requestIdValidator;

	@Before
	public void reset() {
		Mockito.reset(this.ontologyManagerService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testWithNullId() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.requestIdValidator.validate(null, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("id"));
	}

	@Test
	public void testWithEmptyId() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.requestIdValidator.validate("", bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("id"));
	}

	@Test
	public void testWithInvalidFormatId() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.requestIdValidator.validate("1L", bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testWithValidStringId() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.requestIdValidator.validate("1", bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	@Test
	public void testWithIdMoreThanMaximumLimit() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.requestIdValidator.validate("12345678901", bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testWithValidIntegerId() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.requestIdValidator.validate(1, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
