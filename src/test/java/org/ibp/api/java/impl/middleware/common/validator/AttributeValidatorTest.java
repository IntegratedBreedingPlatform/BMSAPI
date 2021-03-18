
package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AttributeValidatorTest {

	private static final String ATTRIBUTE_CODE = "STAT_ACC";
	private static final String PASSPORT_ATTRIBUTE_TYPE = "PASSPORT";
	private static final Integer GID = 1;
	private static final Integer ATTRIBUTE_ID = 1;
	private static final String GERMPLASM_ATTRIBUTE_VALUE = "value";
	private static final String GERMPLASM_ATTRIBUTE_DATE = "20210316";

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private GermplasmAttributeService germplasmAttributeService;

	private BindingResult errors;
	private AttributeValidator attributeValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.attributeValidator = new AttributeValidator();
		this.attributeValidator.setGermplasmDataManager(this.germplasmDataManager);
		this.attributeValidator.setGermplasmService(this.germplasmService);
		this.attributeValidator.setGermplasmAttributeService(this.germplasmAttributeService);
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testValidateAttributeId_WhenAttributeIdIsInvalid() throws MiddlewareQueryException {
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		Mockito.doReturn(null).when(this.germplasmDataManager).getAttributeById(attributeById);

		this.attributeValidator.validateAttributeIds(this.errors, Lists.newArrayList(String.valueOf(attributeById)));
		Assert.assertEquals("attribute.invalid", this.errors.getAllErrors().get(0).getCode());
	}

	@Test
	public void testForValidProgramId() throws MiddlewareQueryException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		final Attribute attribute = new Attribute();
		attribute.setAid(attributeById);
		final List<Attribute> attributeLists = Lists.newArrayList(attribute);
		Mockito.doReturn(attributeLists).when(this.germplasmDataManager).getAttributeByIds(Lists.newArrayList(attributeById));

		this.attributeValidator.validateAttributeIds(bindingResult, Lists.newArrayList(String.valueOf(attributeById)));

		Assert.assertFalse(bindingResult.hasErrors());
	}

	@Test
	public void testValidateAttributeType_ValidAttributeTypeValue() {
		this.attributeValidator.validateAttributeType(this.errors, PASSPORT_ATTRIBUTE_TYPE);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateAttributeType_ThrowsException_WhenAttributeTypeValueIsInvalid() {
		try{
			this.attributeValidator.validateAttributeType(this.errors, "FAIL");
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.type.invalid", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttributeCode_WhenAttributeCodeValueIsValid() {
		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setCode(ATTRIBUTE_CODE);
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		Mockito.when(this.germplasmAttributeService.filterGermplasmAttributes(Collections.singleton(germplasmAttributeRequestDto.getAttributeCode()),
			PASSPORT_ATTRIBUTE_TYPE)).thenReturn(Collections.singletonList(attributeDTO));
		this.attributeValidator.validateAttributeCode(this.errors, germplasmAttributeRequestDto);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateAttributeCode_ThrowException_WhenAttributeCodeValueIsInvalid() {
		try{
			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			Mockito.when(this.germplasmAttributeService.filterGermplasmAttributes(Collections.singleton(germplasmAttributeRequestDto.getAttributeCode()),
				PASSPORT_ATTRIBUTE_TYPE)).thenReturn(Collections.emptyList());
			this.attributeValidator.validateAttributeCode(this.errors, germplasmAttributeRequestDto);
			Assert.fail("should throw an exception");
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.code.invalid", this.errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateGermplasmAttributeShouldNotExist_WhenGermplamAttributeIsNotExisting() {
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.emptyList());
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		this.attributeValidator.validateGermplasmAttributeShouldNotExist(this.errors, GID, germplasmAttributeRequestDto);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttributeShouldNotExist_ThrowsException_WhenGermplamAttributeIsExisting() {
		try {
			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));
			this.attributeValidator.validateGermplasmAttributeShouldNotExist(this.errors, GID, germplasmAttributeRequestDto);
			Assert.fail("should throw an exception");
		} catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.code.invalid.existing", this.errors.getAllErrors().get(0).getCode());
		}

	}

	@Test
	public void testValidateGermplasmAttributeExists_WhenGermplasmAttributeIsExisting() {
		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));
		this.attributeValidator.validateGermplasmAttributeExists(this.errors, GID, ATTRIBUTE_ID);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttributeExists_ThrowsException_WhenGermplasmAttributeIsNotExisting() {
		try {
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null))
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
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));

		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		this.attributeValidator.validateGermplasmAttributeForUpdate(this.errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateGermplasmAttribute_ThrowsException_WhenAttributeIdIsNotExisting() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try{
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			germplasmAttributeDto.setId(2);
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
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
			germplasmAttributeDto.setAttributeCode("ATTRIBUTE");
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));

			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			this.attributeValidator.validateGermplasmAttributeForUpdate(errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
			Assert.fail("should throw an exception");
		}catch(final ApiRequestValidationException e) {
			Assert.assertEquals("attribute.code.update.invalid", errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttribute_ForUpdateScenario_WhenGermplasmAttributeIsValid() {

		//Validate for update success
		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));

		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setCode(ATTRIBUTE_CODE);
		Mockito.when(this.germplasmAttributeService.filterGermplasmAttributes(Collections.singleton(germplasmAttributeRequestDto.getAttributeCode()),
			PASSPORT_ATTRIBUTE_TYPE)).thenReturn(Collections.singletonList(attributeDTO));
		this.attributeValidator.validateAttribute(this.errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
		Assert.assertFalse(this.errors.hasErrors());
	}

	@Test
	public void testValidateAttribute_ForNonExistentGermplasmAttributeScenario_WhenGermplasmAttributeIsValid() {
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setCode(ATTRIBUTE_CODE);
		Mockito.when(this.germplasmAttributeService.filterGermplasmAttributes(Collections.singleton(germplasmAttributeRequestDto.getAttributeCode()),
			PASSPORT_ATTRIBUTE_TYPE)).thenReturn(Collections.singletonList(attributeDTO));
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.emptyList());
		this.attributeValidator.validateAttribute(this.errors, GID, germplasmAttributeRequestDto, null);
		Assert.assertFalse(this.errors.hasErrors());
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
			final String invalidValue = RandomStringUtils.randomAlphabetic(256);
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


	private GermplasmAttributeRequestDto createGermplasmAttributeRequestDto() {
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = new GermplasmAttributeRequestDto();
		germplasmAttributeRequestDto.setAttributeCode(ATTRIBUTE_CODE);
		germplasmAttributeRequestDto.setAttributeType(PASSPORT_ATTRIBUTE_TYPE);
		germplasmAttributeRequestDto.setValue(GERMPLASM_ATTRIBUTE_VALUE);
		germplasmAttributeRequestDto.setDate(GERMPLASM_ATTRIBUTE_DATE);
		return germplasmAttributeRequestDto;
	}

	private GermplasmAttributeDto createGermplasmAttributeDto() {
		final GermplasmAttributeDto germplasmAttributeDto = new GermplasmAttributeDto();
		germplasmAttributeDto.setAttributeCode(ATTRIBUTE_CODE);
		germplasmAttributeDto.setAttributeType(PASSPORT_ATTRIBUTE_TYPE);
		germplasmAttributeDto.setId(ATTRIBUTE_ID);
		return germplasmAttributeDto;
	}
}
