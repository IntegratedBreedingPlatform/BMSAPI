
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

public class TermDeletableValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	private TermDeletableValidator termDeletableValidator;

	@Before
	public void reset() {
		MockitoAnnotations.initMocks(this);
		this.termDeletableValidator = new TermDeletableValidator();
		this.termDeletableValidator.setTermDataManager(this.termDataManager);
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

		Term termMethod = TestDataProvider.getMethodTerm();
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(termMethod.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(termMethod.getId()), "method", CvId.METHODS.getId()),
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

		Term methodTerm = TestDataProvider.getMethodTerm();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(methodTerm.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(methodTerm.getId()), "method", CvId.METHODS.getId()),
				bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
