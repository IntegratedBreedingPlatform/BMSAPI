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
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.ExpectedRange;
import org.ibp.api.domain.ontology.VariableSummary;
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

public class VariableValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	private VariableValidator variableValidator;

	Integer cvId = CvId.VARIABLES.getId();
	String variableName = "My Variable";
	String description = "Variable Description";

	String methodName = "Method Name";
	String methodDescription = "Method Description";

	String propertyName = "Property Name";
	String propertyDescription = "Property Description";

	String scaleName = "Scale Name";
	String scaleDescription = "Scale Description";

	private static final org.ibp.api.domain.ontology.TermSummary methodSummary = new org.ibp.api.domain.ontology.TermSummary();
	private static final org.ibp.api.domain.ontology.TermSummary propertySummary = new org.ibp.api.domain.ontology.TermSummary();
	private static final org.ibp.api.domain.ontology.ScaleSummary scaleSummary = new org.ibp.api.domain.ontology.ScaleSummary();

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		variableValidator = new VariableValidator();
		variableValidator.setTermDataManager(this.termDataManager);
		variableValidator.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		variableValidator.setOntologyScaleDataManager(this.ontologyScaleDataManager);
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

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for to check name length not exceed 32 characters
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(RandomStringUtils.random(210));
		variable.setDescription(this.description);
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.variableValidator.validate(variable, bindingResult);
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

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(RandomStringUtils.random(260));
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.variableValidator.validate(variable, bindingResult);
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

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for Name contain special character and start with digit
	 */
	@Test
	public void testWithSpecialCharacterAnsStartWithDigitVariableName() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName("V@riable");
		variable.setDescription(this.description);
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for propertyId is required
	 */
	@Test
	public void testWithPropertyIdRequiredRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for property id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithPropertyIdExistOrNotRequest() throws MiddlewareException {

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(RandomStringUtils.random(260));
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(null).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for methodId is required
	 */
	@Test
	public void testWithMethodIdRequiredRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(new Term(10, this.variableName, this.description)).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for method id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithMethodIdExistOrNotRequest() throws MiddlewareException {

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(RandomStringUtils.random(260));
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(null).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for scale id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleIdExistOrNotRequest() throws MiddlewareException {

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(RandomStringUtils.random(260));
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);
		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(this.variableName, this.cvId);
		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(null).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.variableValidator.validate(variable, bindingResult);
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

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setExpectedMin("8");
		variable.setExpectedMax("12");
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);

		this.variableValidator.validate(variable, bindingResult);
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

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setExpectedMin("16");
		variable.setExpectedMax("12");
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(12, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);

		this.variableValidator.validate(variable, bindingResult);
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

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();
		OntologyVariableSummary variableSummary = new OntologyVariableSummary(1, "Name", "Description");
		variableSummary.setMethodSummary(new TermSummary(11, methodName, methodDescription));
		variableSummary.setPropertySummary(new TermSummary(10, propertyName, propertyDescription));
		variableSummary.setScaleSummary(new Scale(new Term(12, scaleName, scaleDescription)));
		variableSummaries.add(variableSummary);

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setExpectedMin("12");
		variable.setExpectedMax("16");
		variable.setVariableTypeIds(Collections.singletonList(VariableType.getById(1).getId().toString()));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	/**
	 * Test for variable type id should not be null
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithVariableTypeShouldNotBeNull() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setExpectedMin("12");
		variable.setExpectedMax("16");
		variable.setVariableTypeIds(new ArrayList<String>());
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableValidator.validate(variable, bindingResult);
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

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setExpectedMin("12");
		variable.setExpectedMax("16");
		variable.setVariableTypeIds(Collections.singletonList("12"));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableValidator.validate(variable, bindingResult);
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

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		methodSummary.setId("11");
		propertySummary.setId("10");
		scaleSummary.setId("12");

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		VariableSummary variable = new VariableSummary();
		variable.setName(this.variableName);
		variable.setDescription(this.description);
		variable.setExpectedMin("12");
		variable.setExpectedMax("16");
		variable.setVariableTypeIds(Collections.singletonList("1"));
		variable.setPropertySummary(propertySummary);
		variable.setMethodSummary(methodSummary);
		variable.setScaleSummary(scaleSummary);

		Scale scale = new ScaleBuilder().build(12, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
		Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
		Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

		Mockito.doReturn(new Term(10, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false)).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(20, methodName, methodDescription, CvId.METHODS.getId(), false)).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(30, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
