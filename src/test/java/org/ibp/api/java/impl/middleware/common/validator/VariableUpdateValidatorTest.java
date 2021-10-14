package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.study.CategoryDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class VariableUpdateValidatorTest {

	public static final String CROP = "maize";
	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	protected VariableService variableService;

	@Mock
	protected TermValidator termValidator;

	@InjectMocks
	private VariableUpdateValidator variableUpdateValidator;

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
		final CategoryDTO categoryDTO = new CategoryDTO();
		categoryDTO.setLabel(RandomStringUtils.randomAlphabetic(10));
		categoryDTO.setValue(RandomStringUtils.randomAlphabetic(10));
		variableDTO.getScale().getValidValues().setCategories(Arrays.asList(categoryDTO));

		try {
			this.variableUpdateValidator.validate(CROP, variableDTO);
		} catch (final ApiRequestValidationException exception) {
			fail("Should not throw an exception");
		}
	}

	@Test
	public void testValidationFail() {

		final VariableDTO variableDTO = new VariableDTO();
		try {
			this.variableUpdateValidator.validate(CROP, variableDTO);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException exception) {
			// Do nothing
		}
	}

	@Test
	public void testValidation_ValidateVariable_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(CROP, variableDTO, errors);

		Assert.assertEquals(3, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.id.should.be.numeric");
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.name.required");
	}

	@Test
	public void testValidation_ValidateVariable_MaxLengthExceeded() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH + 1));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(CROP, variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.variable.name.max.length.exceeded");
		Mockito.verify(this.termValidator).validate(Mockito.any(), Mockito.any());
	}

	@Test
	public void testValidation_ValidateVariable_PropertyScaleMethodMismatch() {

		final VariableDetails variableDetails = new VariableDetails();
		final PropertyDetails propertyDetails = new PropertyDetails();
		propertyDetails.setId("1");
		final MethodDetails methodDetails = new MethodDetails();
		methodDetails.setId("2");
		final ScaleDetails scaleDetails = new ScaleDetails();
		scaleDetails.setId("3");
		variableDetails.setProperty(propertyDetails);
		variableDetails.setMethod(methodDetails);
		variableDetails.setScale(scaleDetails);

		Mockito.when(this.ontologyVariableDataManager.areVariablesUsedInStudy(Mockito.anyList())).thenReturn(true);
		Mockito.when(this.variableService.getVariableById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(variableDetails);

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.getTrait().setTraitDbId("4");
		variableDTO.getMethod().setMethodDbId("5");
		variableDTO.getScale().setScaleDbId("6");
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(CROP, variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.cannot.update.trait.scale.method");
		Mockito.verify(this.termValidator).validate(Mockito.any(), Mockito.any());

	}

	@Test
	public void testValidation_ValidateVariable_StudyDbIdsMustBeNumeric() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.setStudyDbIds(Arrays.asList("1", "abc"));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateVariable(CROP, variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.study.id.must.be.numeric");
	}

	@Test
	public void testValidation_ValidateTrait_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateTrait(variableDTO, errors);

		Assert.assertEquals(3, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.trait.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.trait.id.should.be.numeric");
		this.assertError(errors.getAllErrors(), "observation.variable.update.trait.name.required");
	}

	@Test
	public void testValidation_ValidateTrait_MaxLengthExceeded() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getTrait().setTraitDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getTrait().setTraitName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH + 1));
		variableDTO.getTrait()
			.setTraitDescription(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_DEFINITION_MAX_LENGTH + 1));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateTrait(variableDTO, errors);

		Assert.assertEquals(2, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.trait.name.max.length.exceeded");
		this.assertError(errors.getAllErrors(), "observation.variable.update.trait.description.max.length.exceeded");
		Mockito.verify(this.termValidator).validate(Mockito.any(), Mockito.any());
	}

	@Test
	public void testValidation_ValidateMethod_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateMethod(variableDTO, errors);

		Assert.assertEquals(3, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.method.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.method.id.should.be.numeric");
		this.assertError(errors.getAllErrors(), "observation.variable.update.method.name.required");
	}

	@Test
	public void testValidation_ValidateMethod_MaxLengthExceeded() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getMethod().setMethodDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getMethod().setMethodName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH + 1));
		variableDTO.getMethod().setDescription(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_DEFINITION_MAX_LENGTH + 1));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateMethod(variableDTO, errors);

		Assert.assertEquals(2, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.method.name.max.length.exceeded");
		this.assertError(errors.getAllErrors(), "observation.variable.update.method.description.max.length.exceeded");
		Mockito.verify(this.termValidator).validate(Mockito.any(), Mockito.any());
	}

	@Test
	public void testValidation_ValidateScale_RequiredFields() {

		final VariableDTO variableDTO = new VariableDTO();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateScale(variableDTO, errors);

		Assert.assertEquals(3, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.id.required");
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.id.should.be.numeric");
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.name.required");
	}

	@Test
	public void testValidation_ValidateScale_MaxLengthExceeded() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getScale().setScaleName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH + 1));
		final CategoryDTO validValue1 = new CategoryDTO();
		validValue1.setValue(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH));
		validValue1.setLabel(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.CATEROGY_LABEL_MAX_LENGTH + 1));
		final CategoryDTO validValue2 = new CategoryDTO();
		validValue2.setValue(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH + 1));
		validValue2.setLabel(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.CATEROGY_LABEL_MAX_LENGTH));

		variableDTO.getScale().getValidValues().setCategories(Arrays.asList(validValue1, validValue2));
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateScale(variableDTO, errors);

		Assert.assertEquals(2, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.name.max.length.exceeded");
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.categories.label.length.exceeded");
		Mockito.verify(this.termValidator).validate(Mockito.any(), Mockito.any());
	}

	@Test
	public void testValidation_ValidateScale_ValidValues_MinIsGreaterThanMax() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getScale().setScaleName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.getScale().getValidValues().setMin(99);
		variableDTO.getScale().getValidValues().setMax(1);
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateScale(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.min.is.greater.than.max");
		Mockito.verify(this.termValidator).validate(Mockito.any(), Mockito.any());
	}

	@Test
	public void testValidation_ValidateScale_DataTypeNotSupported() {

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.getScale().setScaleDbId(RandomStringUtils.randomNumeric(5));
		variableDTO.getScale().setScaleName(RandomStringUtils.randomAlphabetic(VariableUpdateValidator.TERM_NAME_MAX_LENGTH));
		variableDTO.getScale().setDataType(RandomStringUtils.random(5));

		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.variableUpdateValidator.validateScale(variableDTO, errors);

		Assert.assertEquals(1, errors.getAllErrors().size());
		this.assertError(errors.getAllErrors(), "observation.variable.update.scale.datatype.not.supported");
		Mockito.verify(this.termValidator).validate(Mockito.any(), Mockito.any());
	}

	private void assertError(final List<ObjectError> objectErrorList, final String errorCode) {
		Assert.assertTrue(objectErrorList.stream().anyMatch(o -> Arrays.stream(o.getCodes()).anyMatch(code -> code.equals(errorCode))));
	}

}
