
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.MethodDetails;
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

public class MethodValidatorTest {

	private MethodValidator methodValidator;

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private OntologyMethodDataManager ontologyMethodDataManager;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.methodValidator = new MethodValidator();
		this.methodValidator.setTermDataManager(this.termDataManager);
		this.methodValidator.setOntologyMethodDataManager(this.ontologyMethodDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * Test for Name is required
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithNullNameRequest() throws MiddlewareException {

		MethodDetails methodDetails = TestDataProvider.getTestMethodDetails();
		methodDetails.setName("");

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.methodValidator.validate(methodDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Name is unique
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithUniqueNonNullMethodName() throws MiddlewareException {

		Term methodTerm = TestDataProvider.getMethodTerm();

		// Changing method term with another id to validateBasicData uniqueness by validator
		methodTerm.setId(methodTerm.getId() + 100);

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermByNameAndCvId(methodTerm.getName(), CvId.METHODS.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodDetails methodDetails = TestDataProvider.getTestMethodDetails();

		this.methodValidator.validate(methodDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Name cannot change if the method is already in use
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithNonEditableRequest() throws MiddlewareException {

		Method method = TestDataProvider.getTestMethod();

		Mockito.doReturn(TestDataProvider.getMethodTerm()).when(this.termDataManager)
				.getTermByNameAndCvId(method.getName(), CvId.METHODS.getId());
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(method.getId());
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(method.getId(), true);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodDetails methodDetails = TestDataProvider.getTestMethodDetails();
		methodDetails.setName("ChangedName");

		this.methodValidator.validate(methodDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertTrue(bindingResult.getAllErrors().size() == 1);
	}

	/**
	 * Test for to check name length not exceed 200 characters
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		MethodDetails methodDetails = TestDataProvider.getTestMethodDetails();
		methodDetails.setName(RandomStringUtils.randomAlphanumeric(201));

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.methodValidator.validate(methodDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check description length not exceed 1024 characters
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareException {

		MethodDetails methodSummary = TestDataProvider.getTestMethodDetails();
		methodSummary.setDescription(RandomStringUtils.randomAlphanumeric(1025));

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(methodSummary.getName(), CvId.METHODS.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.methodValidator.validate(methodSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for valid request
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareException {

		MethodDetails methodDetails = TestDataProvider.getTestMethodDetails();

		// Post request does not expect method id.
		methodDetails.setId(null);

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(methodDetails.getName(), CvId.METHODS.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.methodValidator.validate(methodDetails, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	/**
	 * Test for to check Method is Editable or Not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithCheckEditableMethod() throws MiddlewareException {

		MethodDetails methodDetails = TestDataProvider.getTestMethodDetails();
		Method method = TestDataProvider.getTestMethod();

		Integer methodId = StringUtil.parseInt(methodDetails.getId(), null);

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(methodDetails.getName(), CvId.METHODS.getId());
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(methodId, true);
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(methodId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.methodValidator.validate(methodDetails, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
