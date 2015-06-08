
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.ScaleSummary;
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

public class ScaleValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	private ScaleValidator scaleValidator;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Before
	public void reset() {
		MockitoAnnotations.initMocks(this);
		this.scaleValidator = new ScaleValidator();
		this.scaleValidator.setTermDataManager(this.termDataManager);
		this.scaleValidator.setOntologyScaleDataManager(this.ontologyScaleDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * Test for Name is required
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithNullNameRequest() throws MiddlewareException {

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setName("");

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Name is unique
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithUniqueNonNullScaleName() throws MiddlewareException {

		Term scaleTerm = TestDataProvider.getScaleTerm();

		scaleTerm.setId(scaleTerm.getId() + 100);

		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermByNameAndCvId(scaleTerm.getName(), CvId.SCALES.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for DataType Required
	 */
	@Test
	public void testWithDataTypeRequired() {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		// Remove dataType from test object.
		scaleSummary.setDataType(null);
		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
	}

	/**
	 * Test for to Check Valid DataType
	 */
	@Test
	public void testWithInvalidDataType() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		// Setting invalid dataTypeId
		scaleSummary.setDataType(new DataType());
		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
	}

	/**
	 * Test for, If DataType is Categorical and No Categorical Valid Values
	 */
	@Test
	public void testWithAtLeastOneCategory() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setDataType(TestDataProvider.categoricalDataType);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.categories"));
	}

	/**
	 * Test for to Check Label and Value Uniqueness in Categories if DataType is Categorical
	 */
	@Test
	public void testWithUniqueLabelNameInCategoricalDataType() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setDataType(TestDataProvider.categoricalDataType);

		Map<String, String> categories = new HashMap<>();
		categories.put("1", "description");
		categories.put("11", "description");
		scaleSummary.setCategories(categories);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.categories[2].description"));
	}

	/**
	 * Test for to Check Category name exceed limit
	 */
	@Test
	public void testWithCategoryNameExceedMaxLimit() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setDataType(TestDataProvider.categoricalDataType);

		Map<String, String> categories = new HashMap<>();
		categories.put(RandomStringUtils.random(205), "description");
		scaleSummary.setCategories(categories);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.categories[1].name"));
	}

	/**
	 * Test for to Check Category description exceed limit
	 */
	@Test
	public void testWithCategoryDescriptionExceedMaxLimit() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setDataType(TestDataProvider.categoricalDataType);

		Map<String, String> categories = new HashMap<>();
		categories.put("Name", RandomStringUtils.random(260));
		scaleSummary.setCategories(categories);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.categories[1].description"));
	}

	/**
	 * Test for If DataType is Numeric and Min value is Greater than Max value.
	 */
	@Test
	public void testWithMinValueGreater() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setId(null);
		scaleSummary.setMin(10);
		scaleSummary.setMax(5);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.min"));
	}

	/**
	 * Test for to check name length not exceed 200 characters
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setName(RandomStringUtils.random(205));
		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(scaleSummary.getName(), CvId.SCALES.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check description length not exceed 1024 characters
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareException {

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setDescription(RandomStringUtils.random(1025));

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(scaleSummary.getName(), CvId.SCALES.getId());

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for valid request
	 */
	@Test
	public void testWithMinNotGivenAndMaxGiven() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setId(null);
		scaleSummary.setMin(null);
		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(scaleSummary.getName(), CvId.SCALES.getId());

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	/**
	 * Test for valid request
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setId(null);
		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(scaleSummary.getName(), CvId.SCALES.getId());

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
