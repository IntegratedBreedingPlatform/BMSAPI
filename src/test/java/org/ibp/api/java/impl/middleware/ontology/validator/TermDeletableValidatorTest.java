package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
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

public class TermDeletableValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	private TermDeletableValidator termDeletableValidator;

	Integer cvId = CvId.METHODS.getId();

	@Before
	public void reset() {
		MockitoAnnotations.initMocks(this);
		termDeletableValidator = new TermDeletableValidator();
		termDeletableValidator.setTermDataManager(this.termDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * Test for Term Referred
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithTermReferred() throws MiddlewareException {

		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(10);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest("10", "method", this.cvId),
				bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for Term Not Referred
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithTermNotReferred() throws MiddlewareException {

		Mockito.doReturn(new Term(10, "name", "", CvId.METHODS.getId(), false))
		.when(this.termDataManager).getTermById(10);
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(10);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest("10", "method", this.cvId),
				bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
