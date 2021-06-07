package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameRequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmNameValidatorTest {

	final static Integer GERMPLASM_ID = new Random().nextInt();
	final static Integer NAME_ID = new Random().nextInt();


	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private GermplasmNameService germplasmNameService;

	@Mock
	private org.ibp.api.java.germplasm.GermplasmService germplasmService;

	@InjectMocks
	private GermplasmNameRequestValidator germplasmNameRequestValidator;

	private final String programUUID = RandomStringUtils.randomAlphabetic(10);

	@Before
	public void setup() {
		Mockito.when(this.germplasmService.filterGermplasmNameTypes(Mockito.any())).thenReturn(Collections.singletonList(new GermplasmNameTypeDTO()));

	}

	@Test
	public void testValidateCreateName_ThrowsException_WhenGermplasmIdIsInvalid() {
		try {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
			errors.reject("germplasm.invalid", "");
			final GermplasmNameRequestDto germplasmNameRequestDto = createNewGermplasmNameRequestDto();
			Mockito.doThrow(new ApiRequestValidationException(errors.getAllErrors())).when(germplasmValidator).validateGermplasmId(Mockito.any(BindingResult.class), Mockito.anyInt());
			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.invalid"));
		}
	}

	@Test
	public void testValidateCreateName_ThrowsException_WhenNameTypeCodeIsInvalid(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = createNewGermplasmNameRequestDto();
			Mockito.when(this.germplasmService.filterGermplasmNameTypes(Mockito.any())).thenReturn(null);
			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.invalid"));
		}
	}

	@Test
	public void testValidateCreateName_ThrowsException_WhenNameIsRequired(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = createNewGermplasmNameRequestDto();
			germplasmNameRequestDto.setName(RandomStringUtils.randomAlphabetic(0));

			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.required"));
		}
	}

	@Test
	public void testValidateCreateName_ThrowsException_WhenNameExceedMaxLength(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = createNewGermplasmNameRequestDto();
			germplasmNameRequestDto.setName(RandomStringUtils.randomAlphabetic(256));

			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.length"));
		}
	}

	@Test
	public void testValidateCreateName_ThrowsException_WhenDateIsRequired(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = createNewGermplasmNameRequestDto();
			germplasmNameRequestDto.setDate(null);

			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.date.required"));
		}
	}

	@Test
	public void testValidateCreateName_ThrowsException_WhenDateIsInvalid(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = createNewGermplasmNameRequestDto();
			germplasmNameRequestDto.setDate("20212201");

			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.date.invalid"));
		}
	}

	@Test
	public void testValidateCreateName_ThrowsException_WhenPreferredNameIsRequired(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = createNewGermplasmNameRequestDto();
			germplasmNameRequestDto.setPreferredName(null);

			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.preferred.required"));
		}
	}

	@Test
	public void testValidateDeleteName_ThrowsException_WhenNameIsNotBelongsToGermplasm(){
		try {
			final Name name = new Name(2);
			name.setGermplasm(this.mockGermplasm());
			name.setNstat(1);

			Mockito.when(this.germplasmNameService.getNameById(Mockito.any())).thenReturn(null);
			this.germplasmNameRequestValidator.validateNameDeletable(GermplasmNameValidatorTest.GERMPLASM_ID, GermplasmNameValidatorTest.NAME_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.invalid"));
		}
	}

	@Test
	public void testValidateDeleteName_ThrowsException_WhenPreferredNameCannotBeDeleted(){
		try {
			Name name = new Name();
			name.setGermplasm(this.mockGermplasm());
			name.setNid(1);
			name.setNstat(1);
			Mockito.when(this.germplasmNameService.getNameById(Mockito.any())).thenReturn(name);
			this.germplasmNameRequestValidator.validateNameDeletable(GermplasmNameValidatorTest.GERMPLASM_ID, 1);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.preferred.invalid"));
		}
	}

	@Test
	public void testValidateUpdateName_ThrowsException_WhenNameTypeCodeIsInvalid(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = new GermplasmNameRequestDto();
			germplasmNameRequestDto.setNameTypeCode(RandomStringUtils.randomAlphabetic(5));
			final Name name = new Name(GermplasmNameValidatorTest.NAME_ID);
			name.setGermplasm(this.mockGermplasm());
			name.setNstat(1);


			Mockito.when(this.germplasmNameService.getNameById(Mockito.any())).thenReturn(name);
			Mockito.when(this.germplasmService.filterGermplasmNameTypes(Mockito.any())).thenReturn(null);
			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, GermplasmNameValidatorTest.NAME_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.invalid"));
		}
	}

	@Test
	public void testValidateUpdateName_ThrowsException_WhenNameExceedMaxLength(){
		try {
			final GermplasmNameRequestDto germplasmNameRequestDto = new GermplasmNameRequestDto();
			germplasmNameRequestDto.setName(RandomStringUtils.randomAlphabetic(256));

			final Name name = new Name(GermplasmNameValidatorTest.NAME_ID);
			name.setGermplasm(this.mockGermplasm());
			name.setNstat(1);

			Mockito.when(this.germplasmNameService.getNameById(Mockito.any())).thenReturn(name);

			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, GermplasmNameValidatorTest.NAME_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.length"));
		}
	}

	@Test
	public void testValidateUpdateName_ThrowsException_WhenPreferredNameUpdatable(){
		try {

			final GermplasmNameRequestDto germplasmNameRequestDto = new GermplasmNameRequestDto();
			germplasmNameRequestDto.setPreferredName(Boolean.FALSE);

			final Name name = new Name(GermplasmNameValidatorTest.NAME_ID);
			name.setGermplasm(this.mockGermplasm());
			name.setNstat(1);

			Mockito.when(this.germplasmNameService.getNameById(GermplasmNameValidatorTest.NAME_ID)).thenReturn(name);

			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, GermplasmNameValidatorTest.NAME_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.preferred.invalid"));
		}
	}

	@Test
	public void testValidateUpdateName_ThrowsException_WhenDateIsInvalid(){
		try {

			final GermplasmNameRequestDto germplasmNameRequestDto = new GermplasmNameRequestDto();
			germplasmNameRequestDto.setDate("20212201");

			final Name name = new Name(GermplasmNameValidatorTest.NAME_ID);
			name.setGermplasm(this.mockGermplasm());
			name.setNstat(1);

			Mockito.when(this.germplasmNameService.getNameById(Mockito.any())).thenReturn(name);
			this.germplasmNameRequestValidator.validate(this.programUUID, germplasmNameRequestDto, GermplasmNameValidatorTest.GERMPLASM_ID, GermplasmNameValidatorTest.NAME_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.date.invalid"));
		}
	}

	private GermplasmNameRequestDto createNewGermplasmNameRequestDto() {
		GermplasmNameRequestDto germplasmNameRequestDto = new GermplasmNameRequestDto();
		germplasmNameRequestDto.setName(RandomStringUtils.randomAlphabetic(10));
		germplasmNameRequestDto.setPreferredName(false);
		germplasmNameRequestDto.setDate("20210101");
		germplasmNameRequestDto.setNameTypeCode(RandomStringUtils.randomAlphabetic(5));
		germplasmNameRequestDto.setLocationId(9001);
		return germplasmNameRequestDto;
	}

	private Germplasm mockGermplasm() {
		Germplasm germplasm = Mockito.mock(Germplasm.class);
		Mockito.when(germplasm.getGid()).thenReturn(GERMPLASM_ID);
		return germplasm;
	}
}
