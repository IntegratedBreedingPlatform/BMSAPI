package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.TermRequest;
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

public class TermDeletableValidatorTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public OntologyManagerService ontologyManagerService() {
			return Mockito.mock(OntologyManagerService.class);
		}

		@Bean
		@Primary
		public TermDeletableValidator termDeletableValidator() {
			return Mockito.mock(TermDeletableValidator.class);
		}
	}

	@Autowired
	OntologyManagerService ontologyManagerService;

	@Autowired
	TermDeletableValidator termDeletableValidator;

	Integer cvId = CvId.METHODS.getId();

	@Before
	public void reset() {
		Mockito.reset(this.ontologyManagerService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * Test for Term with Null request
	 *
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithNullRequest() throws MiddlewareQueryException {
		Mockito.doReturn(true).when(this.ontologyManagerService).isTermReferred(10);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(null, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for Term Referred
	 *
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithTermReferred() throws MiddlewareQueryException {

		Mockito.doReturn(true).when(this.ontologyManagerService).isTermReferred(10);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest(10, "method", this.cvId),
				bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for Term Not Referred
	 *
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithTermNotReferred() throws MiddlewareQueryException {

		Mockito.doReturn(new Term(10, "name", "", CvId.METHODS.getId(), false))
		.when(this.ontologyManagerService).getTermById(10);
		Mockito.doReturn(false).when(this.ontologyManagerService).isTermReferred(10);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest(10, "method", this.cvId),
				bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
