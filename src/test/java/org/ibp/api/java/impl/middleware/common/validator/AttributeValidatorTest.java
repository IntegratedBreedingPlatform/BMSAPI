
package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AttributeValidatorTest {

	private static final String VARIABLE_NAME = "STAT_ACC";
	private static final Integer GID = 1;
	private static final Integer ATTRIBUTE_ID = 1;
	private static final Integer VARIABLE_ID = 1;

	private static final String GERMPLASM_ATTRIBUTE_VALUE = "value";
	private static final String GERMPLASM_ATTRIBUTE_DATE = "20210316";

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private GermplasmAttributeService germplasmAttributeService;

	private BindingResult errors;

	@InjectMocks
	private AttributeValidator attributeValidator;

	@Before
	public void beforeEachTest() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testValidateAttributeType_ValidAttributeTypeValue() {
		this.attributeValidator.validateAttributeType(this.errors, VariableType.GERMPLASM_ATTRIBUTE.getId());
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateAttributeType_ThrowsException_WhenAttributeTypeValueIsInvalid() {
		try {
			this.attributeValidator.validateAttributeType(this.errors, VariableType.TRAIT.getId());
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.variable.type.invalid", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttributeId_WhenAttributeIdIsInvalid() throws MiddlewareQueryException {
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		Mockito.doReturn(Lists.newArrayList()).when(this.ontologyVariableDataManager).getWithFilter(Mockito.any());

		this.attributeValidator.validateAttributeIds(this.errors, Lists.newArrayList(String.valueOf(attributeById)));
		Assert.assertEquals("attribute.invalid", this.errors.getAllErrors().get(0).getCode());
	}

	@Test
	public void testForValidProgramId() throws MiddlewareQueryException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		final Variable variable = new Variable();
		variable.setId(attributeById);
		variable.getVariableTypes().add(VariableType.GERMPLASM_ATTRIBUTE);
		final List<Variable> attributeLists = Lists.newArrayList(variable);
		Mockito.doReturn(attributeLists).when(this.ontologyVariableDataManager).getWithFilter(Mockito.any());

		this.attributeValidator.validateAttributeIds(bindingResult, Lists.newArrayList(String.valueOf(attributeById)));

		Assert.assertFalse(bindingResult.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttributeShouldNotExist_WhenGermplamAttributeIsNotExisting() {
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, VariableType.GERMPLASM_ATTRIBUTE.getId(), null))
			.thenReturn(Collections.emptyList());
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		this.attributeValidator.validateGermplasmAttributeShouldNotExist(this.errors, GID, germplasmAttributeRequestDto);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttributeShouldNotExist_ThrowsException_WhenGermplamAttributeIsExisting() {
		try {
			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			germplasmAttributeRequestDto.setVariableId(VARIABLE_ID);
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			germplasmAttributeDto.setVariableName("Var");
			germplasmAttributeDto.setVariableId(VARIABLE_ID);
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null, null))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));
			this.attributeValidator.validateGermplasmAttributeShouldNotExist(this.errors, GID, germplasmAttributeRequestDto);
			Assert.fail("should throw an exception");
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.name.invalid.existing", this.errors.getAllErrors().get(0).getCode());
		}

	}

	@Test
	public void testValidateGermplasmAttributeExists_WhenGermplasmAttributeIsExisting() {
		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null, null))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));
		this.attributeValidator.validateGermplasmAttributeExists(this.errors, GID, ATTRIBUTE_ID);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttributeExists_ThrowsException_WhenGermplasmAttributeIsNotExisting() {
		try {
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null, null))
				.thenReturn(Collections.emptyList());
			this.attributeValidator.validateGermplasmAttributeExists(this.errors, GID, ATTRIBUTE_ID);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.id.invalid.not.existing", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateGermplasmAttributeForUpdate_WhenGermplasmAttributeIsValid() {
		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		germplasmAttributeDto.setId(ATTRIBUTE_ID);
		germplasmAttributeDto.setVariableId(VARIABLE_ID);
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null, null))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));

		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		germplasmAttributeRequestDto.setVariableId(VARIABLE_ID);
		this.attributeValidator.validateGermplasmAttributeForUpdate(this.errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttribute_ThrowsException_WhenAttributeIdIsNotExisting() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try{
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			germplasmAttributeDto.setId(2);
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, VariableType.GERMPLASM_ATTRIBUTE.getId(), null))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));

			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			this.attributeValidator.validateGermplasmAttributeForUpdate(errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
			Assert.fail("should throw an exception");
		}catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.id.invalid.not.existing", errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateGermplasmAttribute_ThrowsException_WhenAttributeCodeIsInvalid() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try{
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			germplasmAttributeDto.setVariableName("ATTRIBUTE");
			germplasmAttributeDto.setId(ATTRIBUTE_ID);
			germplasmAttributeDto.setVariableId(2);
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null,null))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));

			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			germplasmAttributeRequestDto.setVariableId(VARIABLE_ID);
			this.attributeValidator.validateGermplasmAttributeForUpdate(errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
			Assert.fail("should throw an exception");
		}catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.variable.id.invalid.not.existing", errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttribute_ForUpdateScenario_WhenGermplasmAttributeIsValid() {

		//Validate for update success
		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		germplasmAttributeDto.setVariableId(VARIABLE_ID);
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, VariableType.GERMPLASM_ATTRIBUTE.getId(), null))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));

		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		germplasmAttributeRequestDto.setVariableId(VARIABLE_ID);

		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null, null))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));

		final Variable variable = new Variable();
		variable.setId(VARIABLE_ID);
		variable.addVariableType(VariableType.GERMPLASM_PASSPORT);

		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(null, VARIABLE_ID, false);

		try (final MockedStatic<VariableValueUtil> variableValueUtilMockedStatic = Mockito.mockStatic(VariableValueUtil.class)) {
			variableValueUtilMockedStatic.when(() -> VariableValueUtil.isValidAttributeValue(Mockito.any(), Mockito.any()))
				.thenReturn(true);
			this.attributeValidator.validateAttribute(this.errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
			Assert.assertFalse(this.errors.hasErrors());
		}
	}

	@Test
	public void testValidateGermplasmAttributeValue_WhenValueIsValid() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.attributeValidator.validateGermplasmAttributeValue(errors, GERMPLASM_ATTRIBUTE_VALUE);
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttributeValue_ThrowsException_WhenValueIsInvalid() {
		try {
			final String invalidValue = RandomStringUtils.randomAlphabetic(AttributeValidator.GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH + 1);
			this.attributeValidator.validateGermplasmAttributeValue(this.errors, invalidValue);
			Assert.fail("should throw an exception");
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.value.invalid.length", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateGermplasmAttributeDate_WhenDateIsValid() {
		this.attributeValidator.validateGermplasmAttributeDate(this.errors, GERMPLASM_ATTRIBUTE_DATE);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttributeDate_ThrowException_WhenDateIsValid() {
		try{
			this.attributeValidator.validateGermplasmAttributeDate(this.errors, "2021-03-16");
			Assert.fail("should throw an exception");
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.date.invalid.format", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttributeVariable_ThrowException_WhenVariableIsNull() {
		try {
			this.attributeValidator.validateAttributeVariable(this.errors, null);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.variable.invalid", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttributeVariable_ThrowException_WhenVariableTypeIsInvalid() {
		try {
			final Variable variable = new Variable();
			variable.addVariableType(VariableType.TRAIT);
			this.attributeValidator.validateAttributeVariable(this.errors, variable);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.variable.type.invalid", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateVariableDataTypeValue_ThrowsException_WhenValueIsInvalid() {
		try {
			try (final MockedStatic<VariableValueUtil> variableValueUtilMockedStatic = Mockito.mockStatic(VariableValueUtil.class)) {
				variableValueUtilMockedStatic.when(() -> VariableValueUtil.isValidAttributeValue(Mockito.any(), Mockito.any()))
					.thenReturn(false);
				this.attributeValidator.validateVariableDataTypeValue(this.errors, new Variable(), RandomStringUtils.randomAlphabetic(20));
			}
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("invalid.variable.value", this.errors.getAllErrors().get(0).getCode());
		}

	}

	private GermplasmAttributeRequestDto createGermplasmAttributeRequestDto() {
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = new GermplasmAttributeRequestDto();
		germplasmAttributeRequestDto.setValue(GERMPLASM_ATTRIBUTE_VALUE);
		germplasmAttributeRequestDto.setDate(GERMPLASM_ATTRIBUTE_DATE);
		return germplasmAttributeRequestDto;
	}

	private GermplasmAttributeDto createGermplasmAttributeDto() {
		final GermplasmAttributeDto germplasmAttributeDto = new GermplasmAttributeDto();
		germplasmAttributeDto.setVariableName(VARIABLE_NAME);
		germplasmAttributeDto.setId(ATTRIBUTE_ID);
		return germplasmAttributeDto;
	}
}
