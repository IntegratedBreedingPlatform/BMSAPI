
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.TermSummary;
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();

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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		// Setting invalid dataTypeId
		scaleSummary.setDataType(new DataType());
		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
	}

	/**
	 * Test for to Check System DataType
	 */
	@Test
	public void testWithSystemDataType() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		// Setting system dataTypeId
		scaleSummary.setDataType(new DataType(String.valueOf(org.generationcp.middleware.domain.ontology.DataType.LOCATION.getId()), "", true));
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setDataType(TestDataProvider.CATEGORICAL_DATA_TYPE);

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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setDataType(TestDataProvider.CATEGORICAL_DATA_TYPE);

		List<TermSummary> categories = new ArrayList<>();
		TermSummary category = new TermSummary();
		category.setName("1");
		category.setDescription("description");
		categories.add(category);
		category = new TermSummary();
		category.setName("11");
		category.setDescription("description");
		categories.add(category);
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setDataType(TestDataProvider.CATEGORICAL_DATA_TYPE);

		List<TermSummary> categories = new ArrayList<>();
		TermSummary category = new TermSummary();
		category.setName(RandomStringUtils.randomAlphanumeric(205));
		category.setDescription("description");
		categories.add(category);
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setDataType(TestDataProvider.CATEGORICAL_DATA_TYPE);

		List<TermSummary> categories = new ArrayList<>();
		TermSummary category = new TermSummary();
		category.setName("Name");
		category.setDescription(RandomStringUtils.randomAlphanumeric(256));
		categories.add(category);
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setId(null);
		scaleSummary.setMinValue("10");
		scaleSummary.setMaxValue("5");

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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setName(RandomStringUtils.randomAlphanumeric(205));
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setDescription(RandomStringUtils.randomAlphanumeric(1025));

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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setId(null);
		scaleSummary.setMinValue(null);
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

		ScaleDetails scaleSummary = TestDataProvider.getTestScaleDetails();
		scaleSummary.setId(null);
		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(scaleSummary.getName(), CvId.SCALES.getId());

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
