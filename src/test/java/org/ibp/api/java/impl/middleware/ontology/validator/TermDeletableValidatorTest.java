
package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;

public class TermDeletableValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private FormulaService formulaService;

	@InjectMocks
	private TermDeletableValidator termDeletableValidator;

	@Before
	public void reset() {
		MockitoAnnotations.initMocks(this);
		doReturn(Collections.EMPTY_LIST).when(this.formulaService).getByInputId(anyInt());
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
		doReturn(true).when(this.termDataManager).isTermReferred(termMethod.getId());

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

		doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		doReturn(false).when(this.termDataManager).isTermReferred(methodTerm.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(methodTerm.getId()), "method", CvId.METHODS.getId()),
				bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	@Test
	public void testWithVariableTermHasNoUsage() throws MiddlewareException {
		Term variableTerm = TestDataProvider.getVariableTerm();
		doReturn(variableTerm).when(this.termDataManager).getTermById(variableTerm.getId());
		doReturn(false).when(this.ontologyVariableDataManager).isVariableUsedInStudy(variableTerm.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(variableTerm.getId()), "variableName", CvId.VARIABLES.getId()),
				bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	@Test
	public void testWithVariableTermHasUsage() throws MiddlewareException {
		Term variableTerm = TestDataProvider.getVariableTerm();
		doReturn(variableTerm).when(this.termDataManager).getTermById(variableTerm.getId());
		doReturn(true).when(this.ontologyVariableDataManager).hasUsage(variableTerm.getId());
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(variableTerm.getId()), "variableName", CvId.VARIABLES.getId()),
				bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}
}
