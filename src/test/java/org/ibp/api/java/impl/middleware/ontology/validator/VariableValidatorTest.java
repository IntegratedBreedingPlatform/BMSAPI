
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.ontology.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.api.domain.ontology.VariableType;
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

public class VariableValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	private VariableValidator variableValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.variableValidator = new VariableValidator();
		this.variableValidator.setTermDataManager(this.termDataManager);
		this.variableValidator.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		this.variableValidator.setOntologyScaleDataManager(this.ontologyScaleDataManager);
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

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setName(null);
		variable.setId(null);

		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for to check name length not exceed 32 characters
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithNameLengthExceedMaxLimit() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setName(RandomStringUtils.random(201));

		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
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

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setDescription(RandomStringUtils.random(1025));

		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

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

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();
		Term variableTerm = TestDataProvider.getVariableTerm();
		variableTerm.setId(variableTerm.getId() + 100);

		Scale scale = TestDataProvider.getTestScale();
		Variable variable = TestDataProvider.getTestVariable();
		variable.setObservations(1);
		VariableSummary variableSummary = TestDataProvider.getTestVariableSummary();
		variableSummary.setName("ChangedName");

		Mockito.doReturn(variableTerm).when(this.termDataManager).getTermById(variableTerm.getId());
		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());
		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(variableSummary.getProgramUuid(), variable.getId());

		this.variableValidator.validate(variableSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Name contain special character and start with digit
	 */
	@Test
	public void testWithSpecialCharacterAnsStartWithDigitVariableName() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setName("V@riable");

		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Alias contain special character and start with digit
	 */
	@Test
	public void testWithSpecialCharacterAnsStartWithDigitVariableAlias() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variableSummary = TestDataProvider.getTestVariableSummary();
		variableSummary.setId("11");
		variableSummary.setProgramUuid("uuid");
		variableSummary.setName("Variable_Name");
		variableSummary.setAlias("2lia");

		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Variable variable = TestDataProvider.getTestVariable();
		variable.setMethod(new Method(methodTerm));
		variable.setProperty(new Property(propertyTerm));
		variable.setScale(scale);

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());
		Mockito.doReturn(variable).when(this.ontologyVariableDataManager)
				.getVariable(variableSummary.getProgramUuid(), StringUtil.parseInt(variableSummary.getId(), null));

		this.variableValidator.validate(variableSummary, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("alias"));
	}

	/**
	 * Test for propertyId is required
	 */
	@Test
	public void testWithPropertyIdRequiredRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		TermSummary propertySummary = new TermSummary();
		variable.setPropertySummary(propertySummary);
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("propertyId"));
	}

	/**
	 * Test for property id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithPropertyIdExistOrNotRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.getPropertySummary().setId("0");

		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("propertyId"));
	}

	/**
	 * Test for methodId is required
	 */
	@Test
	public void testWithMethodIdRequiredRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		TermSummary methodSummary = new TermSummary();
		variable.setMethodSummary(methodSummary);
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("methodId"));
	}

	/**
	 * Test for method id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithMethodIdExistOrNotRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.getMethodSummary().setId("0");
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("methodId"));
	}

	/**
	 * Test for scale id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleIdRequiredRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		ScaleSummary scaleSummary = new ScaleSummary();
		variable.setScaleSummary(scaleSummary);
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("scaleId"));
	}

	/**
	 * Test for scale id exist or not
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleIdExistOrNotRequest() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.getScaleSummary().setId("0");
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("scaleId"));
	}

	/**
	 * Test for scale data type is Numeric and expected range min is less than scale valid values min
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleDataTypeNumericAndExpectedRangeOutOfScaleValidValues() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setExpectedMin("9");
		variable.setExpectedMax("21");
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange.min"));
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange.max"));
	}

	/**
	 * Test for scale data type is Numeric and expected min can same as expected max
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithEqualExpectedMinAndMaxValues() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setExpectedMin("15");
		variable.setExpectedMax("15");
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	/**
	 * Test for scale data type is Numeric and expected min can same as scale min
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithEqualExpectedMinAndScaleMinValue() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setExpectedMin("10.01");
		variable.setExpectedMax("15");
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	/**
	 * Test for scale data type is Numeric and expected max can same as scale max
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithEqualExpectedMaxAndScaleMaxValue() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setExpectedMin("15");
		variable.setExpectedMax("20.02");
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

	/**
	 * Test for Variable expected min is greater than max
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithExpectedRangeMinGreaterThanMax() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setExpectedMin("32");
		variable.setExpectedMax("18");
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("expectedRange"));
	}

	/**
	 * Test for method, property and scale combination already exist for other variable
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithMethodPropertyScaleCombinationExist() throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		OntologyVariableSummary summary = new OntologyVariableSummary(100, "", "");

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<>(Collections.singletonList(summary))).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

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

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setVariableTypes(null);
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

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

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		variable.setVariableTypes(null);
		variable.getVariableTypes().add(new VariableType(0, "", ""));
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

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

		VariableSummary variable = TestDataProvider.getTestVariableSummary();
		variable.setId(null);
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager)
				.getWithFilter(null, null, methodTerm.getId(), propertyTerm.getId(), scale.getId());

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

}
