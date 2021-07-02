package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableOverridesDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class VariableValidatorTest {

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

    @InjectMocks
	private VariableValidator variableValidator;

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setName(null);
		variable.setId(null);

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setName(RandomStringUtils.randomAlphanumeric(201));

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

    @Test
    public void testEmptyStringAgainstNullDBValue() throws MiddlewareException {
        final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
        final VariableDetails variable = TestDataProvider.getTestVariableDetails();
        variable.setAlias("");

        final Variable dbVariable = TestDataProvider.getTestVariable();
        dbVariable.setAlias(null);
        dbVariable.setObservations(5);

    		final Term methodTerm = TestDataProvider.getMethodTerm();
    		final Term propertyTerm = TestDataProvider.getPropertyTerm();
    		final Term scaleTerm = TestDataProvider.getScaleTerm();
    		final Scale scale = TestDataProvider.getTestScale();

    		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
    		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
    		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());    		
    		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleTerm.getId(), true);
			Mockito.doReturn(dbVariable).when(this.ontologyVariableDataManager).getVariable(variable.getProgramUuid(), dbVariable.getId(), true);

        this.variableValidator.validate(variable, bindingResult);

        Assert.assertNull("Validator throws a false negative for equality of empty value / string", bindingResult.getFieldError("alias"));
    }

    @Test
    public void testVariableShouldBeEditableNewValue() throws MiddlewareException {
        final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
        final VariableDetails variable = TestDataProvider.getTestVariableDetails();
        variable.setAlias("");

        final Variable dbVariable = TestDataProvider.getTestVariable();
        dbVariable.setHasUsage(true);

        dbVariable.setAlias("TEST");

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();
		final Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleTerm.getId(), true);
		Mockito.doReturn(dbVariable).when(this.ontologyVariableDataManager)
				.getVariable(variable.getProgramUuid(), dbVariable.getId(), true);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertEquals(0, bindingResult.getErrorCount());
	}

	@Test
	public void testVariableShouldBeNotEditableForANewValue() throws MiddlewareException {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		final VariableDetails variable = TestDataProvider.getTestVariableDetails(TestDataProvider.ANALYSIS_VARIABLE);
		variable.setAlias("");

		final Variable dbVariable = TestDataProvider.getTestVariable(org.generationcp.middleware.domain.ontology.VariableType.ANALYSIS);
		dbVariable.setHasUsage(true);

		dbVariable.setAlias("TEST");

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();
		final Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleTerm.getId(), true);
		Mockito.doReturn(dbVariable).when(this.ontologyVariableDataManager)
			.getVariable(variable.getProgramUuid(), dbVariable.getId(), true);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertEquals(1, bindingResult.getErrorCount());
		Assert.assertNotNull("Validator unable to catch change in alias value between user input and current db state", bindingResult.getFieldError("alias"));
	}

	/**
	 * Test for to check description length not exceed 1024 characters
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareException
	 */
	@Test
	public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setDescription(RandomStringUtils.randomAlphanumeric(1025));

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();
		final Term variableTerm = TestDataProvider.getVariableTerm();
		variableTerm.setId(variableTerm.getId() + 100);

		final Scale scale = TestDataProvider.getTestScale();
		final Variable variable = TestDataProvider.getTestVariable();
		variable.setHasUsage(true);
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		variableDetails.setName("ChangedName");

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(variableDetails.getProgramUuid(), variable.getId(),
				true);
		this.variableValidator.validate(variableDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for Name contain special character and start with digit
	 */
	@Test
	public void testWithSpecialCharacterAnsStartWithDigitVariableName() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setName("V@riable");

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for variables with the same Name
	 */
	@Test
	public void testForVariablesWithTheSameName() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		variableDetails.setId("11");
		variableDetails.setProgramUuid("uuid");
		variableDetails.setName("Variable_Name");
		variableDetails.setAlias("Variable_Name_Alias");

		final List<VariableOverridesDto> variableOverridesDtos = new ArrayList<>();
		variableOverridesDtos.add(new VariableOverridesDto());
		variableOverridesDtos.add(new VariableOverridesDto());
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final Variable variable = TestDataProvider.getTestVariable();
		variable.setMethod(new Method(methodTerm));
		variable.setProperty(new Property(propertyTerm));
		variable.setScale(scale);
	  	variable.setHasUsage(true);

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(variableOverridesDtos).when(this.ontologyVariableDataManager)
			.getVariableOverridesByAliasAndProgram(variableDetails.getName(), variableDetails.getProgramUuid());
		this.variableValidator.validate(variableDetails, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertEquals(1, bindingResult.getErrorCount());
		Assert.assertNotNull(bindingResult.getFieldError("name"));
	}

	/**
	 * Test for propertyId is required
	 */
	@Test
	public void testWithPropertyIdRequiredRequest() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setProperty(new PropertyDetails());
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.getProperty().setId("0");

		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("propertyId"));
	}

	/**
	 * Test for methodId is required
	 */
	@Test
	public void testWithMethodIdRequiredRequest() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setMethod(new MethodDetails());
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.getMethod().setId("0");
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		final ScaleDetails scaleSummary = new ScaleDetails();
		variable.setScale(scaleSummary);
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.getScale().setId("0");
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());

		this.variableValidator.validate(variable, bindingResult);
	}

	/**
	 * Test for scale data type is Numeric and expected range min is less than scale valid values min
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithScaleDataTypeNumericAndExpectedRangeOutOfScaleValidValues() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setExpectedMin("9");
		variable.setExpectedMax("21");
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setExpectedMin("15");
		variable.setExpectedMax("15");
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setExpectedMin("10.01");
		variable.setExpectedMax("15");
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setExpectedMin("15");
		variable.setExpectedMax("20.02");
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setExpectedMin("32");
		variable.setExpectedMax("18");
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		final Variable variableDetail = TestDataProvider.getTestVariable();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>(Collections.singletonList(variableDetail))).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

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

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setVariableTypes(null);
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("variableTypes"));
	}

	/**
	 * Test for invalid variable type
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithVariableTypeIsNotValid() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		variable.setVariableTypes(null);

		final VariableType variableType = new VariableType("0", "Variable Type 1", "Variable Type Description 1");

		variable.getVariableTypes().add(variableType);
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
		Assert.assertNotNull(bindingResult.getFieldError("variableTypes"));
	}

    /**
     * Test for variable type Analysis should not clubbed with other variable type.
     *
     * @throws MiddlewareException
     */
    @Test
    public void testAnalysisVariableShouldNotContainOtherVariableType() throws MiddlewareException {

        final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        final VariableDetails variable = TestDataProvider.getTestVariableDetails();
        variable.setId(null);
        final List<VariableType> variableTypes = variable.getVariableTypes();
        variableTypes.add(TestDataProvider.ANALYSIS_VARIABLE);
        variable.setVariableTypes(new HashSet<>(variableTypes));

        final Term methodTerm = TestDataProvider.getMethodTerm();
        final Term propertyTerm = TestDataProvider.getPropertyTerm();
        final Term scaleTerm = TestDataProvider.getScaleTerm();

        final Scale scale = TestDataProvider.getTestScale();

        final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

        Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
        Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
        Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
        Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
        Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

        this.variableValidator.validate(variable, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("variableTypes"));
    }

    @Test
    public void testPreviousVariableTypesShouldBePresentPositive() {
        final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        final VariableDetails variable = TestDataProvider.getTestVariableDetails();
        final List<VariableType> variableTypes = variable.getVariableTypes();
        // adding a new variable type
        variableTypes.add(TestDataProvider.STUDY_CONDITION_VARIABLE);
        variable.setVariableTypes(new HashSet<>(variableTypes));

        final Term methodTerm = TestDataProvider.getMethodTerm();
        final Term propertyTerm = TestDataProvider.getPropertyTerm();
        final Term scaleTerm = TestDataProvider.getScaleTerm();

        final Scale scale = TestDataProvider.getTestScale();
        final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();
        final Variable originalVariable = TestDataProvider.getTestVariable();

        originalVariable.setHasUsage(true);

        Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
        Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
        Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
        Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
        Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(originalVariable).when(this.ontologyVariableDataManager).getVariable(variable.getProgramUuid(), originalVariable.getId(), true);

        this.variableValidator.validate(variable, bindingResult);
        Assert.assertFalse("Validation should still pass even with new variable types as previous types are retained", bindingResult.hasErrors());
    }

    @Test
    public void testPreviousVariableTypesShouldBePresentNegative() {
        final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        final VariableDetails variable = TestDataProvider.getTestVariableDetails();

        final List<VariableType> variableTypes = new ArrayList<>();

        // changing variable types so that study condition is the only type
        variableTypes.add(TestDataProvider.STUDY_CONDITION_VARIABLE);
        variable.setVariableTypes(new HashSet<>(variableTypes));

        final Term methodTerm = TestDataProvider.getMethodTerm();
        final Term propertyTerm = TestDataProvider.getPropertyTerm();
        final Term scaleTerm = TestDataProvider.getScaleTerm();

        final Scale scale = TestDataProvider.getTestScale();
        final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();
        final Variable originalVariable = TestDataProvider.getTestVariable();

        originalVariable.setHasUsage(true);

        Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
        Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
        Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
        Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
        Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(originalVariable).when(this.ontologyVariableDataManager).getVariable(variable.getProgramUuid(), originalVariable.getId(), true);

        this.variableValidator.validate(variable, bindingResult);
        Assert.assertTrue("Validation should fail if previous type is no longer present", bindingResult.hasErrors());
        Assert.assertNotNull("Validation should fail if previous type is no longer present", bindingResult.getFieldError("variableTypes"));
    }

	/**
	 * Test for valid variable request
	 *
	 * @throws MiddlewareException
	 */
	@Test
	public void testWithValidRequest() throws MiddlewareException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		final VariableDetails variable = TestDataProvider.getTestVariableDetails();
		variable.setId(null);
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final Scale scale = TestDataProvider.getTestScale();

		final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

		this.variableValidator.validate(variable, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}

    /**
     * Test for valid variable request
     *
     * @throws MiddlewareException
     */
    @Test
    public void testVariableShouldNotSaveOrUpdateWithWithValidRequest() throws MiddlewareException {

        final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        final VariableDetails variable = TestDataProvider.getTestVariableDetails();
        variable.setId(null);
        final Term methodTerm = TestDataProvider.getMethodTerm();
        final Term propertyTerm = TestDataProvider.getPropertyTerm();
        final Term scaleTerm = TestDataProvider.getScaleTerm();

        final Scale scale = TestDataProvider.getTestScale();

        final VariableFilter variableFilter = TestDataProvider.getVariableFilterForVariableValidator();

        Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodTerm.getId());
        Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyTerm.getId());
        Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleTerm.getId());
        Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
        Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

        this.variableValidator.validate(variable, bindingResult);
        Assert.assertFalse(bindingResult.hasErrors());
    }

}
