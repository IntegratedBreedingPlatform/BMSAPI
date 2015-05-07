package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

public class MiddlewareIdFormatValidatorTest {

	@Mock
	private OntologyBasicDataManager ontologyBasicDataManager;

	private MiddlewareIdFormatValidator requestIdValidator;

	@Before
	public void reset() {
		MockitoAnnotations.initMocks(this);
		requestIdValidator = new MiddlewareIdFormatValidator();
		requestIdValidator.setOntologyBasicDataManager(ontologyBasicDataManager);
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
