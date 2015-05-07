package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.ibp.api.domain.ontology.ExpectedRange;
import org.ibp.api.domain.ontology.VariableRequest;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.builders.ScaleBuilder;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class VariableRequestValidatorTest {

	@Mock
	private OntologyBasicDataManager ontologyBasicDataManager;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	private VariableRequestValidator variableRequestValidator;

	Integer cvId = CvId.VARIABLES.getId();
	String variableName = "My Variable";
	String description = "Variable Description";

	String methodName = "Method Name";
	String methodDescription = "Method Description";

	String propertyName = "Property Name";
	String propertyDescription = "Property Description";

	String scaleName = "Scale Name";
	String scaleDescription = "Scale Description";

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		variableRequestValidator = new VariableRequestValidator();
		variableRequestValidator.setOntologyBasicDataManager(this.ontologyBasicDataManager);
		variableRequestValidator.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		variableRequestValidator.setOntologyScaleDataManager(this.ontologyScaleDataManager);
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


		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for to check name length not exceed 32 characters
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		VariableRequest request = new VariableRequest();
		request.setName(RandomStringUtils.random(210));
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.variableRequestValidator.validate(request, bindingResult);
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

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(RandomStringUtils.random(260));
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("description"));
	}

	/**
	 * Test for Name is unique
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithUniqueNonNullVariableName() throws MiddlewareException {

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for Name contain special character and start with digit
	 */
	@Test
	public void testWithSpecialCharacterAnsStartWithDigitVariableName() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName("V@riable");
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for propertyId is required
	 */
	@Test
	public void testWithPropertyIdRequiredRequest() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for property id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithPropertyIdExistOrNotRequest() throws MiddlewareException {

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(RandomStringUtils.random(260));
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for methodId is required
	 */
	@Test
	public void testWithMethodIdRequiredRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setMethodId("");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for method id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithMethodIdExistOrNotRequest() throws MiddlewareException {

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(RandomStringUtils.random(260));
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for scale id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleIdExistOrNotRequest() throws MiddlewareException {

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(RandomStringUtils.random(260));
		request.setPropertyId("10");
		request.setMethodId("20");
		request.setScaleId("30");
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for scale data type is Numeric and expected range min is less than
	 * scale valid values min
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleDataTypeNumericAndExpectedRangeLessThanScaleValidValues()
			throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("8");
		expectedRange.setMax("12");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setScaleId("20");
		request.setMethodId("30");
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange.min"));
	}

	/**
	 * Test for Scale data type is Numeric and expected range min is greater
	 * than max
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleDataTypeNumericAndExpectedRangeMinGreaterThanMax()
			throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("16");
		expectedRange.setMax("12");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setScaleId("12");
		request.setMethodId("30");
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange"));
	}

	/**
	 * Test for method, property and scale combination already exist for other
	 * variable
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithMethodPropertyScaleCombinationExist() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

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
		request.setPropertyId("10");
		request.setScaleId("20");
		request.setMethodId("30");
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for variable type id should not be null
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithVariableTypeShouldNotBeNull() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setScaleId("20");
		request.setMethodId("30");
		request.setExpectedRange(expectedRange);
	  	request.setVariableTypeIds(new ArrayList<String>());

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("variableTypeIds"));
	}

	/**
	 * Test for invalid variable type
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithVariableTypeIsNotValid() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setScaleId("20");
		request.setMethodId("30");
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList("12"));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("variableTypeIds"));
	}

	/**
	 * Test for valid variable request
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableRequest request = new VariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.description);
		request.setPropertyId("10");
		request.setScaleId("20");
		request.setMethodId("30");
		request.setExpectedRange(expectedRange);
		request.setVariableTypeIds(Collections.singletonList("1"));

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableRequestValidator.validate(request, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
