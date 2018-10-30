
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.PropertyDetails;
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

/**
 * Test each property validation
 */
public class PropertyValidatorTest {

	private PropertyValidator propertyValidator;

	@Mock
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Mock
	private TermDataManager termDataManager;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.propertyValidator = new PropertyValidator();
		this.propertyValidator.setTermDataManager(this.termDataManager);
		this.propertyValidator.setOntologyPropertyDataManager(this.ontologyPropertyDataManager);
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

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();
		propertyDetails.setName("");

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyValidator.validate(propertyDetails, bindingResult);
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

		Term propertyTerm = TestDataProvider.getPropertyTerm();

		// Changing term with another id to validateBasicData uniqueness by validator
		propertyTerm.setId(propertyTerm.getId() + 100);

		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermByNameAndCvId(propertyTerm.getName(), CvId.PROPERTIES.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();

		this.propertyValidator.validate(propertyDetails, bindingResult);
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

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();

		// Set empty classes to check nonempty
		propertyDetails.setClasses(new HashSet<String>());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(propertyDetails.getName(), CvId.PROPERTIES.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyValidator.validate(propertyDetails, bindingResult);
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

		Property property = TestDataProvider.getTestProperty();

		Mockito.doReturn(TestDataProvider.getPropertyTerm()).when(this.termDataManager)
				.getTermByNameAndCvId(property.getName(), CvId.PROPERTIES.getId());
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(property.getId());
		Mockito.doReturn(property).when(this.ontologyPropertyDataManager).getProperty(property.getId(), true);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();
		propertyDetails.setName("ChangedName");

		this.propertyValidator.validate(propertyDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertTrue(bindingResult.getAllErrors().size() == 1);
	}

	/**
	 * Test for to check name length not exceed 200 characters
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();
		propertyDetails.setName(RandomStringUtils.randomAlphanumeric(205));

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyValidator.validate(propertyDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check description length not exceed 1024 characters
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareException {

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();
		propertyDetails.setDescription(RandomStringUtils.randomAlphanumeric(1025));

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(propertyDetails.getName(), CvId.PROPERTIES.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

		this.propertyValidator.validate(propertyDetails, bindingResult);
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

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();

		// Post request does not expect property id.
		propertyDetails.setId(null);

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(propertyDetails.getName(), CvId.PROPERTIES.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.propertyValidator.validate(propertyDetails, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
