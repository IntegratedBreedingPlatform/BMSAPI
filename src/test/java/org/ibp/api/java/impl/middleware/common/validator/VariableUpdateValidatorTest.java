package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.study.ScaleCategoryDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class VariableUpdateValidatorTest {

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private VariableServiceBrapi variableServiceBrapi;

	@InjectMocks
	private VariableUpdateValidator variableUpdateValidator;

	@Before
	public void setUp() {
		Mockito.when(this.variableServiceBrapi.getObservationVariables(ArgumentMatchers.any(VariableSearchRequestDTO.class), ArgumentMatchers.eq(null)))
				.thenReturn(Collections.singletonList(new VariableDTO()));
	}

	@Test
	public void testValidationSuccess() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getTrait().setTraitDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getTrait().setTraitName(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getTrait().setTraitDescription(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getMethod().setMethodDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getMethod().setMethodName(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getMethod().setDescription(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getScale().setScaleName(RandomStringUtils.randomAlphabetic(5));
		final ScaleCategoryDTO scaleCategoryDTO = new ScaleCategoryDTO();
		scaleCategoryDTO.setLabel(RandomStringUtils.randomAlphabetic(10));
		scaleCategoryDTO.setValue(RandomStringUtils.randomAlphabetic(10));
		variableDTO.getScale().getValidValues().setCategories(Arrays.asList(scaleCategoryDTO));

		try {
			this.variableUpdateValidator.validate(variableDTO);
		} catch (final ApiRequestValidationException exception) {
			fail("Should not throw an exception");
		}
	}

	@Test
	public void testValidationFail() {

		final VariableDTO variableDTO = new VariableDTO();
		try {
			this.variableUpdateValidator.validate(variableDTO);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException exception) {
			// Do nothing
		}
	}

	@Test
	public void testValidation_ValidateVariable_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(variableDTO, errors);

		Assert.assertEquals(3, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.id.should.be.numeric");
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.name.required");
	}

	@Test
	public void testValidation_ValidateVariable_InvalidObservationVariablDbId() {
		Mockito.when(this.variableServiceBrapi.getObservationVariables(ArgumentMatchers.any(VariableSearchRequestDTO.class), ArgumentMatchers.eq(null)))
				.thenReturn(new ArrayList<>());

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(5));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.id.invalid");
	}


	@Test
	public void testValidation_ValidateVariable_MaxLengthExceeded() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH + 1));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.name.max.length.exceeded");
	}

	@Test
	public void testValidation_ValidateVariable_PropertyScaleMethodMismatch() {

		final Variable variable = new Variable();
		final Property property = new Property();
		property.setId(1);
		final Method method = new Method();
		method.setId(2);
		final Scale scale = new Scale();
		scale.setId(3);
		variable.setProperty(property);
		variable.setMethod(method);
		variable.setScale(scale);

		Mockito.when(this.ontologyVariableDataManager.areVariablesUsedInStudy(Mockito.anyList())).thenReturn(true);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(variable);

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.getTrait().setTraitDbId("4");
		variableDTO.getMethod().setMethodDbId("5");
		variableDTO.getScale().setScaleDbId("6");
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.cannot.update.trait.scale.method");
	}

	@Test
	public void testValidation_ValidateVariable_StudyDbIdsMustBeNumeric() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.setStudyDbIds(Arrays.asList("1", "abc"));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.study.id.must.be.numeric");
	}

	@Test
	public void testValidation_ValidateTrait_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateTrait(variableDTO, errors);

		Assert.assertEquals(2, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.trait.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.trait.id.should.be.numeric");
	}

	@Test
	public void testValidation_ValidateMethod_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateMethod(variableDTO, errors);

		Assert.assertEquals(2, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.method.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.method.id.should.be.numeric");
	}

	@Test
	public void testValidation_ValidateScale_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateScale(variableDTO, errors);

		Assert.assertEquals(2, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.id.should.be.numeric");
	}

	private void assertError(final List<ObjectError> objectErrorList, final String errorCode) {
		Assert.assertTrue(objectErrorList.stream().anyMatch(o -> Arrays.stream(o.getCodes()).anyMatch(code -> code.equals(errorCode))));
	}

}
