package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.study.ScaleCategoryDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VariableDtoValidatorTest {

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private VariableServiceBrapi variableServiceBrapi;

	@Mock
	private TermValidator termValidator;

	@Mock
	private StudyServiceBrapi studyServiceBrapi;

	@InjectMocks
	private VariableDtoValidator variableDtoValidator;

	@Before
	public void setUp() {
		when(this.variableServiceBrapi.getVariables(ArgumentMatchers.any(VariableSearchRequestDTO.class),
			ArgumentMatchers.eq(null), ArgumentMatchers.eq(VariableTypeGroup.TRAIT)))
			.thenReturn(Collections.singletonList(new VariableDTO()));
	}

	@Test
	public void testValidateForUpdate_Success() {

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
			this.variableDtoValidator.validateForUpdate(variableDTO.getObservationVariableDbId(), variableDTO);
		} catch (final ApiRequestValidationException exception) {
			fail("Should not throw an exception");
		}
	}

	@Test
	public void testValidateForUpdate_Fail() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		try {
			this.variableDtoValidator.validateForUpdate(variableDTO.getObservationVariableDbId(), variableDTO);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException exception) {
			// Do nothing
		}
	}

	@Test
	public void testValidateForCreate_Success() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getTrait().setTraitDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getMethod().setMethodDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomNumeric(5));
		try {
			this.variableDtoValidator.validateForCreate(Arrays.asList(variableDTO));
		} catch (final ApiRequestValidationException exception) {
			fail("Should not throw an exception");
		}
	}

	@Test
	public void testValidateForCreate_Fail() {

		final VariableDTO variableDTO = new VariableDTO();
		try {
			this.variableDtoValidator.validateForCreate(Arrays.asList(variableDTO));
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException exception) {
			// Do nothing
		}
	}

	@Test
	public void testValidateObservationVariableDbId_RequiredVariableDbId() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateObservationVariableDbId(variableDTO.getObservationVariableDbId(), variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.variable.id.required");
	}

	@Test
	public void testValidateObservationVariableDbId_VariableDbIdNotEqual() {

		final VariableDTO variableDTO = new VariableDTO();
		final String observationVariableDbId = RandomStringUtils.randomNumeric(5);
		variableDTO.setObservationVariableDbId(observationVariableDbId);
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());

		this.variableDtoValidator.validateObservationVariableDbId(RandomStringUtils.randomNumeric(5), variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.variable.id.and.path.variable.id.not.equal");

	}

	@Test
	public void testValidateObservationVariableDbId_NonNumericVariableDbId() {

		final VariableDTO variableDTO = new VariableDTO();
		final String observationVariableDbId = RandomStringUtils.randomAlphabetic(5);
		variableDTO.setObservationVariableDbId(observationVariableDbId);
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateObservationVariableDbId(observationVariableDbId, variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.variable.id.should.be.numeric");
	}

	@Test
	public void testValidateVariable_MissingVariableName() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateObservationVariableName(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.variable.name.required");
	}

	@Test
	public void testValidateObservationVariableDbId_InvalidObservationVariableDbId() {
		when(this.variableServiceBrapi.getVariables(ArgumentMatchers.any(VariableSearchRequestDTO.class),
			ArgumentMatchers.eq(null), ArgumentMatchers.eq(VariableTypeGroup.TRAIT)))
			.thenReturn(new ArrayList<>());

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(5));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateObservationVariableDbId(variableDTO.getObservationVariableDbId(), variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.variable.id.invalid");
	}

	@Test
	public void testValidateVariable_MaxLengthExceeded() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableDtoValidator.TERM_NAME_MAX_LENGTH + 1));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateObservationVariableName(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.variable.name.max.length.exceeded");
	}

	@Test
	public void testCheckVariableIsUsedInStudy_PropertyScaleMethodMismatch() {

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

		when(this.ontologyVariableDataManager.areVariablesUsedInStudy(Mockito.anyList())).thenReturn(true);
		when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean()))
			.thenReturn(variable);

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableDtoValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.getTrait().setTraitDbId("4");
		variableDTO.getMethod().setMethodDbId("5");
		variableDTO.getScale().setScaleDbId("6");
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.checkVariableIsUsedInStudy(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.cannot.update.trait.scale.method");
	}

	@Test
	public void testValidateStudyDbIds_InvalidStudyDbId() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableDtoValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.setStudyDbIds(Arrays.asList("1", "abc"));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateStudyDbIds(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.study.id.invalid");
	}

	@Test
	public void testValidateTrait_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateTrait(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.trait.id.required");
	}

	@Test
	public void testValidateTrait_NonNumericTraitDbId() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getTrait().setTraitDbId(RandomStringUtils.randomAlphabetic(5));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateTrait(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.trait.id.should.be.numeric");
	}

	@Test
	public void testValidateMethod_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateMethod(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.method.id.required");
	}

	@Test
	public void testValidateMethod_NonNumericMethodDbId() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getMethod().setMethodDbId(RandomStringUtils.randomAlphabetic(5));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateMethod(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.method.id.should.be.numeric");
	}

	@Test
	public void testValidateScale_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateScale(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.scale.id.required");
	}

	@Test
	public void testValidateScale_NonNumericScaleDbId() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomAlphabetic(5));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateScale(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.scale.id.should.be.numeric");
	}

	@Test
	public void testCheckDuplicatePropertyScaleMethodCombination_VariableWithPropertyScaleMethodCombinationAlreadyExists() {
		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getTrait().setTraitDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getMethod().setMethodDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomNumeric(5));

		final Variable variable = new Variable();
		variable.setId(1);
		when(this.ontologyVariableDataManager.getWithFilter(Mockito.any())).thenReturn(Arrays.asList(variable));

		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.checkDuplicatePropertyScaleMethodCombination(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.trait.scale.method.combination.already.exists");
	}

	@Test
	public void testValidateContextOfUse_InvalidContextOfUse() {
		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getContextOfUse().add("Invalid ContextOfUse");

		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.validateContextOfUse(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.invalid.context.of.use");
	}

	@Test
	public void testCheckForExistingObservationVariableName_VariableNameAlreadyUsed() {
		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(5));
		variableDTO.getTrait().setTraitDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getMethod().setMethodDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomNumeric(5));

		final Variable variable = new Variable();
		variable.setId(1);
		variable.setName(variableDTO.getObservationVariableName());
		when(this.ontologyVariableDataManager.getWithFilter(Mockito.any())).thenReturn(Arrays.asList(variable));

		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableDtoValidator.checkForExistingObservationVariableName(Arrays.asList(variableDTO), errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.variable.names.already.exist");
	}

	private void assertError(final List<ObjectError> objectErrorList, final String errorCode) {
		Assert.assertTrue(objectErrorList.stream().anyMatch(o -> Arrays.stream(o.getCodes()).anyMatch(code -> code.equals(errorCode))));
	}

}
