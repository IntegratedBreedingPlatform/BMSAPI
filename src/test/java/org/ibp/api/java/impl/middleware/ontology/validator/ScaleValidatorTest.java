package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.IdName;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.domain.ontology.VariableCategory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScaleValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	private ScaleValidator scaleValidator;

	private Integer cvId = CvId.SCALES.getId();
	private String scaleName = "MyScale";
	private String description = "Scale Description";
	private IdName categoricalId = new IdName(1048, "Categorical");
	private IdName numericalId = new IdName(1110, "Numeric");

	@Before
	public void reset() {
		MockitoAnnotations .initMocks(this);
		scaleValidator = new ScaleValidator();
		scaleValidator.setTermDataManager(termDataManager);
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

		Mockito.doReturn(null).when(this.termDataManager)
		.getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName("");
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(numericalId);
		scaleSummary.setMinValue("1");
		scaleSummary.setMaxValue("10");

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

		Mockito.doReturn(new Term(10, this.scaleName, this.description))
		.when(this.termDataManager).getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(numericalId);
		scaleSummary.setMinValue("1");
		scaleSummary.setMaxValue("10");

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

		IdName dataType = new IdName();

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(dataType);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
	}

	/**
	 * Test for to Check Valid DataType
	 */
	@Test
	public void testWithValidDataType() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		IdName dataType = new IdName(1, "Invalid DataType");

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(dataType);

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

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(categoricalId);
		Map<String, String> categories = new HashMap<>();
		scaleSummary.setCategories(categories);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.categories"));
	}

	/**
	 * Test for to Check Label and Value Uniqueness in Categories if DataType is
	 * Categorical
	 */
	@Test
	public void testWithUniqueLabelNameInCategoricalDataType() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
		Map<String, String> categories = new HashMap<>();

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(categoricalId);
		categories.put("1", "description");
		categories.put("11", "description");
		scaleSummary.setCategories(categories);

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.categories[2].description"));
	}

	/**
	 * Test for If DataType is Numeric and Min value is Greater than Max value.
	 */
	@Test
	public void testWithMinValueGreater() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
		Map<String, String> categories = new HashMap<>();

		ValidValues validValues = new ValidValues();
		validValues.setMin("10");
		validValues.setMax("1");

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(numericalId);
		scaleSummary.setMinValue("10");
		scaleSummary.setMaxValue("1");
		categories.put("1", "description");
		categories.put("11", "description");
		scaleSummary.setCategories(categories);

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

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(RandomStringUtils.random(205));
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(numericalId);
		scaleSummary.setMinValue("1");
		scaleSummary.setMaxValue("10");

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check description length not exceed 255 characters
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareException {

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(RandomStringUtils.random(260));
		scaleSummary.setDataType(numericalId);
		scaleSummary.setMinValue("1");
		scaleSummary.setMaxValue("10");

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for valid request
	 */
	@Test
	public void testWithValidRequest() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		List<VariableCategory> categories = new ArrayList<>();
		categories.add(new VariableCategory("1", "description"));
		categories.add(new VariableCategory("11", "description1"));

		ValidValues validValues = new ValidValues();
		validValues.setCategories(categories);

		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setName(this.scaleName);
		scaleSummary.setDescription(this.description);
		scaleSummary.setDataType(numericalId);
		scaleSummary.setMinValue("1");
		scaleSummary.setMaxValue("10");

		this.scaleValidator.validate(scaleSummary, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
