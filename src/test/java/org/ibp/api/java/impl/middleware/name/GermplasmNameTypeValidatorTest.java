package org.ibp.api.java.impl.middleware.name;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.name.validator.GermplasmNameTypeValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmNameTypeValidatorTest {

	@Mock
	private GermplasmNameTypeService germplasmNameTypeService;

	@InjectMocks
	private GermplasmNameTypeValidator germplasmNameTypeValidator;

	@Test
	public void testValidate_ThrowsException_WhenCodeIsNull(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode(null);
		try {
		this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.empty"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenCodeIsBlank(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode("");
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.empty"));
		}
	}


	@Test
	public void testValidate_ThrowsException_WhenCodeIsInvalid(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH + 1));
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.length.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenCodeAlreadyExists(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		final Set<String> codes = new HashSet<>(Arrays.asList(germplasmNameTypeRequestDTO.getCode()));
		final GermplasmNameTypeDTO nameTypes = new GermplasmNameTypeDTO();
		nameTypes.setCode(germplasmNameTypeRequestDTO.getCode());
		when(this.germplasmNameTypeService.filterGermplasmNameTypes(Mockito.any())).thenReturn(Collections.singletonList(nameTypes));
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.code.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameIsNull(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setName(null);

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.empty"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameIsBlank(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setName("");

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.empty"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameIsInvalid(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setName(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH + 1));

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.length.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNameAlreadyExists(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		Mockito.when(this.germplasmNameTypeService.filterGermplasmNameTypesByName(germplasmNameTypeRequestDTO.getName())).thenReturn(Arrays.asList(new GermplasmNameTypeDTO()));
		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.name.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenDescriptionIsInvalid(){
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = buildGermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setDescription(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH + 1));

		try {
			this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.name.type.description.length.invalid"));
		}
	}

	private GermplasmNameTypeRequestDTO buildGermplasmNameTypeRequestDTO() {
		final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO = new GermplasmNameTypeRequestDTO();
		germplasmNameTypeRequestDTO.setCode(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		germplasmNameTypeRequestDTO.setName(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		germplasmNameTypeRequestDTO.setDescription(RandomStringUtils.randomAlphabetic(GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH));
		return germplasmNameTypeRequestDTO;
	}

}
