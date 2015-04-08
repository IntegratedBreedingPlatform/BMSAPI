package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.ExpectedRange;
import org.ibp.api.domain.ontology.VariableRequest;
import org.ibp.builders.ScaleBuilder;
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

public class VariableRequestValidatorTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public OntologyManagerService ontologyManagerService() {
			return Mockito.mock(OntologyManagerService.class);
		}

		@Bean
		@Primary
		public VariableRequestValidator variableRequestValidator() {
			return Mockito.mock(VariableRequestValidator.class);
		}
	}

	@Autowired
	OntologyManagerService ontologyManagerService;

	@Autowired
	VariableRequestValidator variableRequestValidator;

	Integer cvId = CvId.VARIABLES.getId();
	String variableName = "My Variable";
	String description = "Variable Description";

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
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	@Test
	public void testWithNullNameRequest() throws MiddlewareQueryException {

		Mockito.doReturn(null).when(this.ontologyManagerService)
				.getTermByNameAndCvId(this.variableName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName("");
		request.setDescription(this.description);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check name length not exceed 32 characters
	 * 
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareQueryException {

		Mockito.doReturn(null).when(this.ontologyManagerService)
				.getTermByNameAndCvId(this.variableName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.randomString(35));
		request.setDescription(this.description);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check description length not exceed 255 characters
	 * 
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareQueryException {

		Mockito.doReturn(null).when(this.ontologyManagerService)
				.getTermByNameAndCvId(this.variableName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.randomString(260));

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for Name is unique
	 * 
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	@Test
	public void testWithUniqueNonNullVariableName() throws MiddlewareQueryException {

		Mockito.doReturn(new Term(10, this.variableName, this.description))
				.when(this.ontologyManagerService)
				.getTermByNameAndCvId(this.variableName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Name contain special character and start with digit
	 */
	@Test
	public void testWithSpecialCharacterAnsStartWithDigitVariableName() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName("V@riable");
		request.setDescription(this.description);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for propertyId is required
	 */
	@Test
	public void testWithPropertyIdRequiredRequest() {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("propertyId"));
	}

	/**
	 * Test for property id exist or not
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithPropertyIdExistOrNotRequest() throws MiddlewareQueryException {

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setScaleId(20);
		request.setMethodId(30);

		Mockito.doReturn(null).when(this.ontologyManagerService).getTermById(10);
		Mockito.doReturn(new Term(20, "scale", "", CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(20);
		Mockito.doReturn(new Term(30, "method", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(30);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for methodId is required
	 */
	@Test
	public void testWithMethodIdRequiredRequest() throws MiddlewareQueryException {

		Mockito.doReturn(new Term(10, "name", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(10);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("methodId"));
	}

	/**
	 * Test for method id exist or not
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithMethodIdExistOrNotRequest() throws MiddlewareQueryException {

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setScaleId(20);
		request.setMethodId(30);

		Mockito.doReturn(new Term(10, "property", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(10);
		Mockito.doReturn(new Term(20, "scale", "", CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(20);
		Mockito.doReturn(null).when(this.ontologyManagerService).getTermById(30);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");
		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for scaleId is required
	 */
	@Test
	public void testWithScaleIdRequiredRequest() throws MiddlewareQueryException {

		Mockito.doReturn(new Term(10, "pName", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(10);
		Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(11);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("scaleId"));
	}

	/**
	 * Test for scale id exist or not
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithScaleIdExistOrNotRequest() throws MiddlewareQueryException {

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setScaleId(20);
		request.setMethodId(30);

		Mockito.doReturn(new Term(10, "property", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(10);
		Mockito.doReturn(null).when(this.ontologyManagerService).getTermById(20);
		Mockito.doReturn(new Term(30, "scale", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(30);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for scale data type does not exist or null
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithScaleDataTypeNull() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";
		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, null, "", "", null);

		Mockito.doReturn(new Term(10, "pName", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("scaleId"));
	}

	/**
	 * Test for scale data type non numeric and expected range given in request
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithScaleDataTypeNotNumericAndExpectedRangeGiven()
			throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("20");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.CATEGORICAL_VARIABLE, "", "", null);

		Mockito.doReturn(new Term(10, "pName", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange"));
	}

	/**
	 * Test for scale data type is Numeric and Expected range is not given
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithScaleDataTypeNumericAndExpectedRangeNotGiven()
			throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("");
		expectedRange.setMax("");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(new Term(10, "pName", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange"));
	}

	/**
	 * Test for scale data type is Numeric and expected range min is less than
	 * scale valid values min
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithScaleDataTypeNumericAndExpectedRangeLessThanScaleValidValues()
			throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("8");
		expectedRange.setMax("12");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(new Term(10, "pName", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange"));
	}

	/**
	 * Test for Scale data type is Numeric and expected range min is greater
	 * than max
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithScaleDataTypeNumericAndExpectedRangeMinGreaterThanMax()
			throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("16");
		expectedRange.setMax("12");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(new Term(10, "pName", "", CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange"));
	}

	/**
	 * Test for method, property and scale combination already exist for other
	 * variable
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithMethodPropertyScaleCombinationExist() throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String methodName = "Method Name";
		String methodDescription = "Method Description";

		String propertyName = "Property Name";
		String propertyDescription = "Property Description";

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();
		OntologyVariableSummary variableSummary = new OntologyVariableSummary(1, "Name",
				"Description");
		variableSummary.setMethodSummary(new TermSummary(11, methodName, methodDescription));
		variableSummary.setPropertySummary(new TermSummary(10, propertyName, propertyDescription));
		variableSummary.setScaleSummary(new TermSummary(12, scaleName, scaleDescription));
		variableSummaries.add(variableSummary);

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(
				new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, methodName, methodDescription, CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());
		Mockito.doReturn(variableSummaries)
				.when(this.ontologyManagerService)
				.getWithFilter(null, null, request.getMethodId(), request.getPropertyId(),
						request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("methodId"));
	}

	/**
	 * Test for variable type id should not be null
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithVariableTypeShouldNotBeNull() throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String methodName = "Method Name";
		String methodDescription = "Method Description";

		String propertyName = "Property Name";
		String propertyDescription = "Property Description";

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(
				new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, methodName, methodDescription, CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());
		Mockito.doReturn(variableSummaries)
				.when(this.ontologyManagerService)
				.getWithFilter(null, null, request.getMethodId(), request.getPropertyId(),
						request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("variableTypeIds"));
	}

	/**
	 * Test for invalid variable type
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithVariableTypeIsNotValid() throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String methodName = "Method Name";
		String methodDescription = "Method Description";

		String propertyName = "Property Name";
		String propertyDescription = "Property Description";

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(12));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(
				new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, methodName, methodDescription, CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());
		Mockito.doReturn(variableSummaries)
				.when(this.ontologyManagerService)
				.getWithFilter(null, null, request.getMethodId(), request.getPropertyId(),
						request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("variableTypeIds"));
	}

	/**
	 * Test for valid variable request
	 * 
	 * @throws MiddlewareQueryException
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		String methodName = "Method Name";
		String methodDescription = "Method Description";

		String propertyName = "Property Name";
		String propertyDescription = "Property Description";

		String scaleName = "Scale Name";
		String scaleDescription = "Scale Description";

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId(10);
		request.setMethodId(11);
		request.setScaleId(12);
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(1));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,
				DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(
				new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getPropertyId());
		Mockito.doReturn(new Term(11, methodName, methodDescription, CvId.METHODS.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getMethodId());
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false))
				.when(this.ontologyManagerService).getTermById(request.getScaleId());
		Mockito.doReturn(scale).when(this.ontologyManagerService)
				.getScaleById(request.getScaleId());
		Mockito.doReturn(variableSummaries)
				.when(this.ontologyManagerService)
				.getWithFilter(null, null, request.getMethodId(), request.getPropertyId(),
						request.getScaleId());

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
