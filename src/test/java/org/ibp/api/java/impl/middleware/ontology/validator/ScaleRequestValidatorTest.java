package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.VariableCategory;
import org.ibp.api.domain.ontology.ScaleRequest;
import org.ibp.api.domain.ontology.ValidValues;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

public class ScaleRequestValidatorTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public OntologyManagerService ontologyManagerService() {
			return Mockito.mock(OntologyManagerService.class);
		}

		@Bean
		@Primary
		public ScaleRequestValidator scaleRequestValidator() {
			return Mockito.mock(ScaleRequestValidator.class);
		}
	}

	@Autowired
	OntologyManagerService ontologyManagerService;

	@Autowired
	ScaleRequestValidator scaleRequestValidator;

	Integer cvId = CvId.SCALES.getId();
	String scaleName = "MyScale";
	String description = "Scale Description";

	@Before
	public void reset() {
		Mockito.reset(this.ontologyManagerService);
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

		Mockito.doReturn(null).when(this.ontologyManagerService)
		.getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleRequest request = new ScaleRequest();
		request.setName("");
		request.setDescription(this.description);

		this.scaleRequestValidator.validate(request, bindingResult);
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
		.when(this.ontologyManagerService).getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);

		this.scaleRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for DataType Required
	 */
	@Test
	public void testWithDataTypeRequired() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);
		request.setDataTypeId(null);

		this.scaleRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
	}

	/**
	 * Test for to Check Valid DataType
	 */
	@Test
	public void testWithValidDataType() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);
		request.setDataTypeId(0);

		this.scaleRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
	}

	/**
	 * Test for, If DataType is Categorical and No Categorical Valid Values
	 */
	@Test
	public void testWithAtLeastOneCategory() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);
		request.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		ValidValues validValues = new ValidValues();
		validValues.setCategories(new ArrayList<VariableCategory>());
		request.setValidValues(validValues);

		this.scaleRequestValidator.validate(request, bindingResult);
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

		List<VariableCategory> categories = new ArrayList<>();
		categories.add(new VariableCategory("1", "description"));
		categories.add(new VariableCategory("1", "description1"));

		ValidValues validValues = new ValidValues();
		validValues.setCategories(categories);

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);
		request.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		request.setValidValues(validValues);

		this.scaleRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.categories[2].name"));
	}

	/**
	 * Test for If DataType is Numeric and Min or Max value is Non-Numeric
	 */
	@Test
	public void testWithMinMaxValueNonNumericDataValue() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ValidValues validValues = new ValidValues();
		validValues.setMin("a");
		validValues.setMax("10");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);
		request.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		request.setValidValues(validValues);

		this.scaleRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("validValues.min"));
	}

	/**
	 * Test for If DataType is Numeric and Min value is Greater than Max value.
	 */
	@Test
	public void testWithMinValueGreater() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ValidValues validValues = new ValidValues();
		validValues.setMin("10");
		validValues.setMax("1");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);
		request.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		request.setValidValues(validValues);

		this.scaleRequestValidator.validate(request, bindingResult);
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

		Mockito.doReturn(null).when(this.ontologyManagerService)
		.getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.randomString(205));
		request.setDescription(this.description);

		this.scaleRequestValidator.validate(request, bindingResult);
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

		Mockito.doReturn(null).when(this.ontologyManagerService)
		.getTermByNameAndCvId(this.scaleName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.randomString(260));

		this.scaleRequestValidator.validate(request, bindingResult);
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

		ScaleRequest request = new ScaleRequest();
		request.setName(this.scaleName);
		request.setDescription(this.description);
		request.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		request.setValidValues(validValues);

		this.scaleRequestValidator.validate(request, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
