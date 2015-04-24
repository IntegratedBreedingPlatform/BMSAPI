package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiTestUtilities;
import org.ibp.api.domain.ontology.MethodRequest;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

public class MethodRequestValidatorTest {

	private MethodRequestValidator methodRequestValidator;

	@Mock
	private OntologyManagerService ontologyManagerService;
	
	Integer cvId = CvId.METHODS.getId();
	String methodName = "MyMethod";
	String description = "Method Description";

	@Before
	public void beforeEachTest() {
		MockitoAnnotations .initMocks(this);
		methodRequestValidator = new MethodRequestValidator();
		methodRequestValidator.setOntologyManagerService(ontologyManagerService);
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

		Mockito.doReturn(null).when(this.ontologyManagerService)
		.getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodRequest request = new MethodRequest();
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

		Mockito.doReturn(new Term(10, this.methodName, this.description))
		.when(this.ontologyManagerService).getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodRequest request = new MethodRequest();
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

		MethodRequest request = new MethodRequest();
		request.setId("10");
		request.setName(this.methodName);
		request.setDescription(this.description);

		Mockito.doReturn(new Term(10, this.methodName, this.description))
		.when(this.ontologyManagerService).getTermByNameAndCvId(this.methodName, this.cvId);
		Mockito.doReturn(true).when(this.ontologyManagerService).isTermReferred(CommonUtil.tryParseSafe(request.getId()));

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
		Mockito.doReturn(null).when(this.ontologyManagerService)
		.getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodRequest request = new MethodRequest();
		request.setName(ApiTestUtilities.randomString(201));
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
		Mockito.doReturn(null).when(this.ontologyManagerService)
		.getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodRequest request = new MethodRequest();
		request.setName(this.methodName);
		request.setDescription(ApiTestUtilities.randomString(260));

		this.methodRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for valid request
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareException {

		Mockito.doReturn(null).when(this.ontologyManagerService)
		.getTermByNameAndCvId(this.methodName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		MethodRequest request = new MethodRequest();
		request.setName(this.methodName);
		request.setDescription(this.description);

		this.methodRequestValidator.validate(request, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

}
