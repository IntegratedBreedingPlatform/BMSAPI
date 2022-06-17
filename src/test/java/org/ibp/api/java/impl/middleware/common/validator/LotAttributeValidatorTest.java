package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.shared.AttributeRequestDto;
import org.generationcp.middleware.domain.shared.AttributeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.inventory.LotAttributeService;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
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

public class LotAttributeValidatorTest {
	private static final Integer LOT_ID = 1;
	private static final String LOT_ATTRIBUTE_DATE = "20210316";
	private static final String LOT_ATTRIBUTE_VALUE = "value";
	private static final int VARIABLE_ID= 1001;
	private static final String VARIABLE_NAME = "INVENTORY_VAR";
	private static final Integer ATTRIBUTE_ID = 1;

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private LotAttributeService lotAttributeService;

	@Mock
	private LotService lotService;

	private BindingResult errors;

	@InjectMocks
	private LotAttributeValidator lotAttributeValidator;

	@Before
	public void beforeEachTest() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		Mockito.when(this.lotService.searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.eq(null)))
			.thenReturn(Collections.singletonList(new ExtendedLotDto()));

		final Variable variable = new Variable();
		variable.setId(VARIABLE_ID);
		variable.addVariableType(VariableType.INVENTORY_ATTRIBUTE);

		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(null, VARIABLE_ID, false);
	}

	@Test
	public void testValidateAttribute_ThrowsException_WhenLotIdIsInvalid() {
		try {
			Mockito.when(this.lotService.searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.eq(null)))
				.thenReturn(null);
			this.lotAttributeValidator.validateLot(this.errors, LOT_ID);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("lot.invalid", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttributeVariable_ThrowException_WhenVariableIsNull() {
		try {
			this.lotAttributeValidator.validateAttributeVariable(this.errors, null,
				Collections.singletonList(VariableType.INVENTORY_ATTRIBUTE));
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
			this.lotAttributeValidator.validateAttributeVariable(this.errors, variable,
				Collections.singletonList(VariableType.INVENTORY_ATTRIBUTE));
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.variable.type.invalid", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttributeDate_WhenDateIsValid() {
		this.lotAttributeValidator.validateAttributeDate(this.errors, LOT_ATTRIBUTE_DATE);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateAttributeDate_ThrowException_WhenDateIsInvalid() {
		try{
			this.lotAttributeValidator.validateAttributeDate(this.errors, "2021-03-16");
			Assert.fail("should throw an exception");
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.date.invalid.format", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttributeValue_WhenValueIsValid() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.lotAttributeValidator.validateAttributeValue(errors, LOT_ATTRIBUTE_VALUE);
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void testValidateAttributeValue_ThrowsException_WhenValueIsInvalid() {
		try {
			final String invalidValue = RandomStringUtils.randomAlphabetic(
				AttributeValidator.ATTRIBUTE_VALUE_MAX_LENGTH + 1);
			this.lotAttributeValidator.validateAttributeValue(this.errors, invalidValue);
			Assert.fail("should throw an exception");
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.value.invalid.length", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateLotAttributeShouldNotExist_WhenLotAttributeIsNotExisting() {
		final AttributeRequestDto attributeRequestDto = this.createAttributeRequestDto();
		try (final MockedStatic<VariableValueUtil> variableValueUtilMockedStatic = Mockito.mockStatic(VariableValueUtil.class)) {
			variableValueUtilMockedStatic.when(() -> VariableValueUtil.isValidAttributeValue(Mockito.any(), Mockito.any()))
				.thenReturn(true);
			this.lotAttributeValidator.validateAttribute(this.errors, LOT_ID, attributeRequestDto, null);
			Assert.assertFalse(this.errors.hasErrors());
		}
	}

	@Test
	public void testValidateLotAttributeShouldNotExist_ThrowsException_WhenLotAttributeIsExisting() {
		try {
			final AttributeRequestDto attributeRequestDto = this.createAttributeRequestDto();
			final AttributeDto attributeDto = new AttributeDto();
			attributeDto.setVariableId(VARIABLE_ID);
			Mockito.when(this.lotAttributeService.getLotAttributeDtos(LOT_ID, null))
				.thenReturn(Collections.singletonList(attributeDto));

			try (final MockedStatic<VariableValueUtil> variableValueUtilMockedStatic = Mockito.mockStatic(VariableValueUtil.class)) {
				variableValueUtilMockedStatic.when(() -> VariableValueUtil.isValidAttributeValue(Mockito.any(), Mockito.any()))
					.thenReturn(true);
				this.lotAttributeValidator.validateAttribute(this.errors, LOT_ID, attributeRequestDto, null);
				Assert.fail("should throw an exception");
			}
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.name.invalid.existing", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateLotAttributeForUpdate_WhenLotAttributeIsValid() {
		final AttributeDto attributeDto = this.createAttributeDto();
		attributeDto.setVariableId(VARIABLE_ID);
		Mockito.when(this.lotAttributeService.getLotAttributeDtos(LOT_ID, null))
			.thenReturn(Collections.singletonList(attributeDto));

		final AttributeRequestDto attributeRequestDto = this.createAttributeRequestDto();
		attributeRequestDto.setVariableId(VARIABLE_ID);
		this.lotAttributeValidator.validateLotAttributeForUpdate(this.errors, LOT_ID, attributeRequestDto, ATTRIBUTE_ID);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateLotAttribute_ThrowsException_WhenAttributeIdIsNotExisting() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try{
			final AttributeDto attributeDto = this.createAttributeDto();
			attributeDto.setId(2);
			Mockito.when(this.lotAttributeService.getLotAttributeDtos(LOT_ID, null))
				.thenReturn(Collections.singletonList(attributeDto));

			final AttributeRequestDto attributeRequestDto = this.createAttributeRequestDto();
			this.lotAttributeValidator.validateLotAttributeForUpdate(errors, LOT_ID, attributeRequestDto, ATTRIBUTE_ID);
			Assert.fail("should throw an exception");
		}catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.id.invalid.not.existing", errors.getAllErrors().get(0).getCode());
		}
	}


	@Test
	public void testValidateLotAttribute_ThrowsException_WhenAttributeCodeIsInvalid() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try{
			final AttributeDto attributeDto = this.createAttributeDto();
			attributeDto.setVariableName("ATTRIBUTE");
			attributeDto.setVariableId(2);
			Mockito.when(this.lotAttributeService.getLotAttributeDtos(LOT_ID, null))
				.thenReturn(Collections.singletonList(attributeDto));

			final AttributeRequestDto attributeRequestDto = this.createAttributeRequestDto();
			this.lotAttributeValidator.validateLotAttributeForUpdate(errors, LOT_ID, attributeRequestDto, ATTRIBUTE_ID);
			Assert.fail("should throw an exception");
		}catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.variable.id.invalid.not.existing", errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateLotAttributeExists_WhenLotAttributeIsExisting() {
		final AttributeDto attributeDto = this.createAttributeDto();
		Mockito.when(this.lotAttributeService.getLotAttributeDtos(LOT_ID, null))
			.thenReturn(Collections.singletonList(attributeDto));
		this.lotAttributeValidator.validateLotAttributeExists(this.errors, LOT_ID, ATTRIBUTE_ID);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateLotAttributeExists_ThrowsException_WhenLotAttributeIsNotExisting() {
		try {
			Mockito.when(this.lotAttributeService.getLotAttributeDtos(LOT_ID, null))
				.thenReturn(Collections.emptyList());
			this.lotAttributeValidator.validateLotAttributeExists(this.errors, LOT_ID, ATTRIBUTE_ID);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.id.invalid.not.existing", this.errors.getAllErrors().get(0).getCode());
		}
	}

	private AttributeRequestDto createAttributeRequestDto() {
		final AttributeRequestDto attributeRequestDto = new AttributeRequestDto();
		attributeRequestDto.setValue(LOT_ATTRIBUTE_VALUE);
		attributeRequestDto.setDate(LOT_ATTRIBUTE_DATE);
		attributeRequestDto.setVariableId(VARIABLE_ID);
		return attributeRequestDto;
	}

	private AttributeDto createAttributeDto() {
		final AttributeDto attributeDto = new AttributeDto();
		attributeDto.setVariableName(VARIABLE_NAME);
		attributeDto.setId(ATTRIBUTE_ID);
		return attributeDto;
	}
}
