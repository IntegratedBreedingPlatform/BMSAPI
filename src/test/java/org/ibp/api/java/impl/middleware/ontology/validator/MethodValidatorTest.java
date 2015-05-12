package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.builders.MethodBuilder;
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

	private MethodValidator methodRequestValidator;

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private OntologyMethodDataManager ontologyMethodDataManager;
	
	Integer cvId = CvId.METHODS.getId();
	String methodName = "MyMethod";
	String description = "Method Description";

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		methodRequestValidator = new MethodValidator();
		methodRequestValidator.setTermDataManager(termDataManager);
		methodRequestValidator.setOntologyMethodDataManager(ontologyMethodDataManager);
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

		Mockito.doReturn(null).when(this.termDataManager)
		.getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodSummary request = new MethodSummary();
		request.setName("");
		request.setDescription(this.description);

		this.methodRequestValidator.validate(request, bindingResult);
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

		Mockito.doReturn(new Term(10, this.methodName, this.description)).when(this.termDataManager).getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodSummary request = new MethodSummary();
		request.setName(this.methodName);
		request.setDescription(this.description);

		this.methodRequestValidator.validate(request, bindingResult);
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

		MethodSummary request = new MethodSummary();
		request.setId("10");
		request.setName(this.methodName);
		request.setDescription(this.description);

		Method method = new MethodBuilder().build(1, "m1", "d1");

		Mockito.doReturn(new Term(10, this.methodName, this.description))
		.when(this.termDataManager).getTermByNameAndCvId(this.methodName, this.cvId);
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(CommonUtil.tryParseSafe(request.getId()));
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(CommonUtil.tryParseSafe(request.getId()));

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.methodRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertTrue(bindingResult.getAllErrors().size() == 1);
	}

	/**
	 * Test for to check name length not exceed 200 characters
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {
		Mockito.doReturn(null).when(this.termDataManager)
		.getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodSummary request = new MethodSummary();
		request.setName(RandomStringUtils.random(201));
		request.setDescription(this.description);

		this.methodRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check description length not exceed 255 characters
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareException {
		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodSummary request = new MethodSummary();
		request.setName(this.methodName);
		request.setDescription(RandomStringUtils.random(260));

		this.methodRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for valid request
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareException {

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodSummary request = new MethodSummary();
		request.setName(this.methodName);
		request.setDescription(this.description);

		this.methodRequestValidator.validate(request, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

}
