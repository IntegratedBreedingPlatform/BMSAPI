
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

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private GermplasmAttributeService germplasmAttributeService;

	private AttributeValidator attributeValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.attributeValidator = new AttributeValidator();
		this.attributeValidator.setGermplasmDataManager(this.germplasmDataManager);
		this.attributeValidator.setGermplasmService(this.germplasmService);
		this.attributeValidator.setGermplasmAttributeService(this.germplasmAttributeService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testForInvalidAttributeId() throws MiddlewareQueryException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		Mockito.doReturn(null).when(this.germplasmDataManager).getAttributeById(attributeById);

		this.attributeValidator.validateAttributeIds(bindingResult, Lists.newArrayList(String.valueOf(attributeById)));
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForValidProgramId() throws MiddlewareQueryException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		final Attribute attribute = new Attribute();
		attribute.setAid(attributeById);
		List<Attribute> attributeLists = Lists.newArrayList(attribute);
		Mockito.doReturn(attributeLists).when(this.germplasmDataManager).getAttributeByIds(Lists.newArrayList(attributeById));

		this.attributeValidator.validateAttributeIds(bindingResult, Lists.newArrayList(String.valueOf(attributeById)));

		Assert.assertFalse(bindingResult.hasErrors());
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateAttributeType() {
		// Validate for happy path scenario
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.attributeValidator.validateAttributeType(errors, PASSPORT_ATTRIBUTE_TYPE);
		Assert.assertFalse(errors.hasErrors());

		// Validate for scenario with error
		this.attributeValidator.validateAttributeType(errors, "FAIL");
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateAttributeCode() {
		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setCode(ATTRIBUTE_CODE);

		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();

		Mockito.when(this.germplasmService.filterGermplasmAttributes(Collections.singleton(germplasmAttributeRequestDto.getAttributeCode()),
			PASSPORT_ATTRIBUTE_TYPE)).thenReturn(Collections.singletonList(attributeDTO));
		// Validate for happy path scenario
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.attributeValidator.validateAttributeCode(errors, germplasmAttributeRequestDto);
		Assert.assertFalse(errors.hasErrors());

		Mockito.when(this.germplasmService.filterGermplasmAttributes(Collections.singleton(germplasmAttributeRequestDto.getAttributeCode()),
			PASSPORT_ATTRIBUTE_TYPE)).thenReturn(Collections.emptyList());

		// Validate for scenario with error
		this.attributeValidator.validateAttributeCode(errors, germplasmAttributeRequestDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateGermplasmAttributeShouldNotExist() {
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.emptyList());
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();

		// Validate for happy path scenario
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.attributeValidator.validateGermplasmAttributeShouldNotExist(errors, GID, germplasmAttributeRequestDto);

		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));
		this.attributeValidator.validateGermplasmAttributeShouldNotExist(errors, GID, germplasmAttributeRequestDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateGermplasmAttributeExist() {
		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));

		// Validate for happy path scenario
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.attributeValidator.validateGermplasmAttributeExisting(errors, GID, ATTRIBUTE_ID);

		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, null))
			.thenReturn(Collections.emptyList());
		this.attributeValidator.validateGermplasmAttributeExisting(errors, GID, ATTRIBUTE_ID);
	}

	@Test
	public void testValidateGermplasmAttributeForUpdateSuccess() {
		try{
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));

			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			this.attributeValidator.validateGermplasmAttributeForUpdate(errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
		}catch(ApiRequestValidationException e) {
			Assert.fail("should not throw an error");
		}
	}

	@Test
	public void testValidateGermplasmAttributeForInvalidAttributeId() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try{
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			germplasmAttributeDto.setId(2);
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));

			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			this.attributeValidator.validateGermplasmAttributeForUpdate(errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
			Assert.fail("should throw an error");
		}catch(ApiRequestValidationException e) {
			Assert.assertEquals("attribute.id.invalid.not.existing", errors.getAllErrors().get(0).getCode());
		}
	}
	@Test
	public void testValidateGermplasmAttributeForInvalidAttributeCode() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try{
			final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
			germplasmAttributeDto.setAttributeCode("ATTRIBUTE");
			Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
				.thenReturn(Collections.singletonList(germplasmAttributeDto));

			final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
			this.attributeValidator.validateGermplasmAttributeForUpdate(errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);
			Assert.fail("should throw an error");
		}catch(ApiRequestValidationException e) {
			Assert.assertEquals("attribute.code.update.invalid", errors.getAllErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAttribute() {

		//Validate for update success

		final GermplasmAttributeDto germplasmAttributeDto = this.createGermplasmAttributeDto();
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.singletonList(germplasmAttributeDto));

		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = this.createGermplasmAttributeRequestDto();
		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setCode(ATTRIBUTE_CODE);
		Mockito.when(this.germplasmService.filterGermplasmAttributes(Collections.singleton(germplasmAttributeRequestDto.getAttributeCode()),
			PASSPORT_ATTRIBUTE_TYPE)).thenReturn(Collections.singletonList(attributeDTO));
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.attributeValidator.validateAttribute(errors, GID, germplasmAttributeRequestDto, ATTRIBUTE_ID);

		//Validate for non-existing
		Mockito.when(this.germplasmAttributeService.getGermplasmAttributeDtos(GID, PASSPORT_ATTRIBUTE_TYPE))
			.thenReturn(Collections.emptyList());
		this.attributeValidator.validateAttribute(errors, GID, germplasmAttributeRequestDto, null);
	}


	public GermplasmAttributeRequestDto createGermplasmAttributeRequestDto() {
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = new GermplasmAttributeRequestDto();
		germplasmAttributeRequestDto.setAttributeCode(ATTRIBUTE_CODE);
		germplasmAttributeRequestDto.setAttributeType(PASSPORT_ATTRIBUTE_TYPE);
		return germplasmAttributeRequestDto;
	}

	public GermplasmAttributeDto createGermplasmAttributeDto() {
		final GermplasmAttributeDto germplasmAttributeDto = new GermplasmAttributeDto();
		germplasmAttributeDto.setAttributeCode(ATTRIBUTE_CODE);
		germplasmAttributeDto.setAttributeType(PASSPORT_ATTRIBUTE_TYPE);
		germplasmAttributeDto.setId(ATTRIBUTE_ID);
		return germplasmAttributeDto;
	}
}
