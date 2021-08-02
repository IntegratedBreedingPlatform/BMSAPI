package org.ibp.api.java.impl.middleware.name;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.pojos.Name;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.name.validator.GermplasmNameTypeValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmNameTypeValidatorTest {

	final static Integer NAME_TYPE_ID = new Random().nextInt();

	@Mock
	private GermplasmNameTypeService germplasmNameTypeService;

	@Mock
	private GermplasmNameService germplasmNameService;

	@InjectMocks
	private GermplasmNameTypeValidator germplasmNameTypeValidator;

	@Test
	public void testValidate_ThrowsException_WhenCodeIsNull() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode(null);
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.empty"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenCodeIsBlank() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode("");
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.empty"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenCodeIsInvalid() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH + 1));
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.length.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenCodeAlreadyExists() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		final Set<String> codes = new HashSet<>(Arrays.asList(germplasmNameTypeRequestDTO.getCode()));
		final GermplasmNameTypeDTO nameTypes = new GermplasmNameTypeDTO();
		nameTypes.setCode(germplasmNameTypeRequestDTO.getCode());
		when(this.germplasmNameTypeService.filterGermplasmNameTypes(Mockito.any())).thenReturn(Collections.singletonList(nameTypes));
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameIsNull() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setName(null);

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.empty"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameIsBlank() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setName("");

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.empty"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameIsInvalid() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setName(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH + 1));

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.length.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameAlreadyExists() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		Mockito.when(this.germplasmNameTypeService.filterGermplasmNameTypesByName(germplasmNameTypeRequestDTO.getName()))
			.thenReturn(Arrays.asList(new GermplasmNameTypeDTO()));
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenDescriptionIsInvalid() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = this.buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO
			.setDescription(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH + 1));

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.description.length.invalid"));
		}
	}

	@Test
	public void ValidateDeletable_ThrowsException_WhenNameTypeIdIsInvalid() {
		Mockito.when(this.germplasmNameTypeService.getNameTypeById(GermplasmNameTypeValidatorTest.NAME_TYPE_ID))
			.thenReturn(Optional.empty());
		try {
			this.germplasmNameTypeValidator.validateCanBeDeleted(GermplasmNameTypeValidatorTest.NAME_TYPE_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.invalid"));
		}
	}

	@Test
	public void ValidateDeletable_ThrowsException_WhenNameTypeIsUsedInGermplasm() {
		final GermplasmNameTypeDTO germplasmNameTypeDTO = this.buildGermplasmNameTypeDTO();

		Mockito.when(this.germplasmNameTypeService.getNameTypeById(GermplasmNameTypeValidatorTest.NAME_TYPE_ID))
			.thenReturn(Optional.of(germplasmNameTypeDTO));

		Mockito.when(this.germplasmNameService.isNameTypeUsedAsGermplasmName(GermplasmNameTypeValidatorTest.NAME_TYPE_ID))
			.thenReturn(false);

		try {
			this.germplasmNameTypeValidator.validateCanBeDeleted(GermplasmNameTypeValidatorTest.NAME_TYPE_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.is.in.used"));
		}
	}

	@Test
	public void ValidateDeletable_ThrowsException_WhenNameTypeIsUsedInList() {
		final GermplasmNameTypeDTO germplasmNameTypeDTO = this.buildGermplasmNameTypeDTO();

		Mockito.when(this.germplasmNameTypeService.getNameTypeById(GermplasmNameTypeValidatorTest.NAME_TYPE_ID))
			.thenReturn(Optional.of(germplasmNameTypeDTO));

		Mockito.when(this.germplasmNameTypeService.isNameTypeUsedInListDataProp(Mockito.any()))
			.thenReturn(true);
		try {
			this.germplasmNameTypeValidator.validateCanBeDeleted(GermplasmNameTypeValidatorTest.NAME_TYPE_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.asociated.to.list"));
		}
	}

	@Test
	public void ValidateDeletable_ThrowsException_WhenNameTypeIdIsUsedBySystem() {
		final GermplasmNameTypeDTO germplasmNameTypeDTO = this.buildGermplasmNameTypeDTO();
		germplasmNameTypeDTO.setCode(GermplasmNameTypeValidator.SYSTEM_NAME_TYPES.get(0));

		Mockito.when(this.germplasmNameTypeService.getNameTypeById(GermplasmNameTypeValidatorTest.NAME_TYPE_ID))
			.thenReturn(Optional.of(germplasmNameTypeDTO));

		try {
			this.germplasmNameTypeValidator.validateCanBeDeleted(GermplasmNameTypeValidatorTest.NAME_TYPE_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.used.for.the.system"));
		}
	}

	private GermplasmNameTypeRequestDTO buildGermplasmNameTypeRequestDTO() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = new GermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		germplasmNameTypeRequestDTO.setName(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		germplasmNameTypeRequestDTO.setDescription(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		return germplasmNameTypeRequestDTO;
	}

	private GermplasmNameTypeDTO buildGermplasmNameTypeDTO() {
		final GermplasmNameTypeDTO germplasmNameTypeDTO = new GermplasmNameTypeDTO();
		germplasmNameTypeDTO.setCode(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		germplasmNameTypeDTO.setName(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		germplasmNameTypeDTO.setDescription(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		return germplasmNameTypeDTO;
	}

}
