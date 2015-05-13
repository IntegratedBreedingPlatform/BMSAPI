package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.PropertySummary;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Test each property validation
 */
public class PropertyValidatorTest {

	private PropertyValidator propertyValidator;

	@Mock
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Mock
	private TermDataManager termDataManager;

	private Set<String> classes = new HashSet<>();

	Integer cvId = CvId.PROPERTIES.getId();
	String propertyName = "MyProperty";
	String description = "Property Description";

	@Before
	public void beforeEachTest() {
		MockitoAnnotations .initMocks(this);
		propertyValidator = new PropertyValidator();
		propertyValidator.setTermDataManager(termDataManager);
		propertyValidator.setOntologyPropertyDataManager(ontologyPropertyDataManager);
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

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertySummary propertySummary = new PropertySummary();
		propertySummary.setName("");
		propertySummary.setDescription(this.description);
		propertySummary.setClasses(new HashSet<String>());

		this.propertyValidator.validate(propertySummary, bindingResult);
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
				.when(this.termDataManager)
				.getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertySummary request = new PropertySummary();
		request.setName(this.propertyName);
		request.setDescription(this.description);
		request.setClasses(new HashSet<String>());

		this.propertyValidator.validate(request, bindingResult);
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

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		PropertySummary request = new PropertySummary();
		request.setName(this.propertyName);
		request.setDescription(this.description);
		request.setClasses(new HashSet<String>());

		// Assert for no class defined
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyValidator.validate(request, bindingResult);
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
		Property toReturn = new Property(dbTerm);

		PropertySummary request = new PropertySummary();
		request.setId(String.valueOf(requestId));
		request.setName(this.propertyName + "0");
		request.setDescription(this.description);
		request.setClasses(new HashSet<String>());

		Mockito.doReturn(dbTerm).when(this.termDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(requestId);
		Mockito.doReturn(toReturn).when(this.ontologyPropertyDataManager).getProperty(requestId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for to check name length not exceed 200 characters
	 * 
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertySummary request = new PropertySummary();
		request.setName(RandomStringUtils.random(205));
		request.setDescription(this.description);
		request.setClasses(new HashSet<String>());

		this.propertyValidator.validate(request, bindingResult);
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

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertySummary request = new PropertySummary();
		request.setName(this.propertyName);
		request.setDescription(RandomStringUtils.random(260));
		request.setClasses(new HashSet<String>());

		this.propertyValidator.validate(request, bindingResult);
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

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.propertyName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertySummary propertySummary = new PropertySummary();
		propertySummary.setName(this.propertyName);
		propertySummary.setDescription(this.description);
		classes.add("Class1");
		classes.add("Class2");
		propertySummary.setClasses(classes);

		this.propertyValidator.validate(propertySummary, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
