package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.OntologyProperty;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;

public class PropertyRequestValidatorTest {

	private PropertyRequestValidator propertyRequestValidator;

	@Mock
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Mock
	private OntologyBasicDataManager ontologyBasicDataManager;

	Integer cvId = CvId.PROPERTIES.getId();
	String propertyName = "MyProperty";
	String description = "Property Description";

	@Before
	public void beforeEachTest() {
		MockitoAnnotations .initMocks(this);
		propertyRequestValidator = new PropertyRequestValidator();
		propertyRequestValidator.setOntologyBasicDataManager(ontologyBasicDataManager);
		propertyRequestValidator.setOntologyPropertyDataManager(ontologyPropertyDataManager);
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

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertyRequest request = new PropertyRequest();
		request.setName("");
		request.setDescription(this.description);

		this.propertyRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Name is unique
	 * 
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithUniqueNonNullPropertyName() throws MiddlewareException {

		Mockito.doReturn(new Term(10, this.propertyName, this.description))
				.when(this.ontologyBasicDataManager)
				.getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertyRequest request = new PropertyRequest();
		request.setName(this.propertyName);
		request.setDescription(this.description);

		this.propertyRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for should have at least one class and that is valid
	 * 
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithClassNameNonEmptyUniqueValues() throws MiddlewareException {

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		PropertyRequest request = new PropertyRequest();
		request.setName(this.propertyName);
		request.setDescription(this.description);

		// Assert for no class defined
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("classes"));
	}

	/**
	 * Test for Name cannot change if the property is already in use
	 * 
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithNonEditableRequest() throws MiddlewareException {

		Integer requestId = 10;

		Term dbTerm = new Term(requestId, this.propertyName, this.description);
		OntologyProperty toReturn = new OntologyProperty(dbTerm);

		PropertyRequest request = new PropertyRequest();
		request.setId(String.valueOf(requestId));
		request.setName(this.propertyName + "0");
		request.setDescription(this.description);

		Mockito.doReturn(dbTerm).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);
		Mockito.doReturn(true).when(this.ontologyBasicDataManager).isTermReferred(requestId);
		Mockito.doReturn(toReturn).when(this.ontologyPropertyDataManager).getProperty(requestId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for to check name length not exceed 200 characters
	 * 
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertyRequest request = new PropertyRequest();
		request.setName(RandomStringUtils.random(205));
		request.setDescription(this.description);

		this.propertyRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check description length not exceed 255 characters
	 * 
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareException {

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertyRequest request = new PropertyRequest();
		request.setName(this.propertyName);
		request.setDescription(RandomStringUtils.random(260));

		this.propertyRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for valid request
	 * 
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareException {

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertyRequest request = new PropertyRequest();
		request.setName(this.propertyName);
		request.setDescription(this.description);
		request.setClasses(Arrays.asList("Class1", "Class2"));

		this.propertyRequestValidator.validate(request, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
