package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.service.api.crop.CropGenotypingParameterService;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CropGenotypingParameterValidatorTest {

	@Mock
	private CropGenotypingParameterService cropGenotypingParameterService;

	@Mock
	private BindingResult errors;

	@InjectMocks
	private CropGenotypingParameterValidator cropGenotypingParameterValidator;

	@Test
	public void testValidateCreation_OK() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		try {
			this.cropGenotypingParameterValidator.validateCreation(cropGenotypingParameterDTO.getCropName(), cropGenotypingParameterDTO);
		} catch (final ApiRequestValidationException exception) {
			Assert.fail("Should not throw an exception");
		}

	}

	@Test
	public void testValidateCreation_Fail() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		cropGenotypingParameterDTO.setCropName(null);
		try {
			this.cropGenotypingParameterValidator.validateCreation(cropGenotypingParameterDTO.getCropName(), cropGenotypingParameterDTO);
			Assert.fail("Should throw an exception");
		} catch (final ApiRequestValidationException exception) {

		}

	}

	@Test
	public void testValidateEdition_OK() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		when(this.cropGenotypingParameterService.getCropGenotypingParameterById(
			cropGenotypingParameterDTO.getGenotypingParameterId())).thenReturn(
			Optional.of(cropGenotypingParameterDTO));
		try {
			this.cropGenotypingParameterValidator.validateEdition(cropGenotypingParameterDTO.getCropName(), cropGenotypingParameterDTO);
		} catch (final ApiRequestValidationException exception) {
			Assert.fail("Should not throw an exception");
		}

	}

	@Test
	public void testValidateEdition_Fail() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		cropGenotypingParameterDTO.setCropName("");
		try {
			this.cropGenotypingParameterValidator.validateEdition(cropGenotypingParameterDTO.getCropName(), cropGenotypingParameterDTO);
			Assert.fail("Should not throw an exception");
		} catch (final ApiRequestValidationException exception) {

		}

	}

	@Test
	public void testValidateCropGenotypingParameterAlreadyExists() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		when(this.cropGenotypingParameterService.getCropGenotypingParameter(cropGenotypingParameterDTO.getCropName())).thenReturn(
			Optional.of(new CropGenotypingParameterDTO()));

		this.cropGenotypingParameterValidator.validateCropGenotypingParameterAlreadyExists(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.record.already.exists",
			new String[] {cropGenotypingParameterDTO.getCropName()}, "");

	}

	@Test
	public void testvalidateCropGenotypingParameterId_IDIsRequired() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		cropGenotypingParameterDTO.setGenotypingParameterId(0);
		this.cropGenotypingParameterValidator.validateCropGenotypingParameterId(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.record.id.is.required",
			new String[] {}, "");
	}

	@Test
	public void testvalidateCropGenotypingParameterId_RecordDoesNotExist() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		cropGenotypingParameterDTO.setGenotypingParameterId(1);
		when(this.cropGenotypingParameterService.getCropGenotypingParameterById(
			cropGenotypingParameterDTO.getGenotypingParameterId())).thenReturn(
			Optional.empty());
		this.cropGenotypingParameterValidator.validateCropGenotypingParameterId(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.record.id.not.exists",
			new String[] {String.valueOf(cropGenotypingParameterDTO.getGenotypingParameterId())}, "");
	}

	@Test
	public void testvalidateCropGenotypingParameterId_CropNameDoesNotMatch() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = this.getCropGenotypingParameterDTO();
		final CropGenotypingParameterDTO cropGenotypingParameterDTOWithDifferentCrop = this.getCropGenotypingParameterDTO();
		cropGenotypingParameterDTOWithDifferentCrop.setCropName("some crop");
		cropGenotypingParameterDTO.setGenotypingParameterId(1);
		when(this.cropGenotypingParameterService.getCropGenotypingParameterById(
			cropGenotypingParameterDTO.getGenotypingParameterId())).thenReturn(
			Optional.of(cropGenotypingParameterDTOWithDifferentCrop));
		this.cropGenotypingParameterValidator.validateCropGenotypingParameterId(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.genotyping.parameter.crop.name.mismatch");

	}

	@Test
	public void testValidateCropName() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = new CropGenotypingParameterDTO();

		this.cropGenotypingParameterValidator.validateCropName(cropGenotypingParameterDTO.getCropName(), cropGenotypingParameterDTO,
			this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.crop.name.is.required");

		cropGenotypingParameterDTO.setCropName("maize");
		this.cropGenotypingParameterValidator.validateCropName("wheat", cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.crop.name.path.mismatch");

		cropGenotypingParameterDTO.setCropName(RandomStringUtils.random(CropGenotypingParameterValidator.CROP_NAME_MAXLENGTH + 1));
		this.cropGenotypingParameterValidator.validateCropName(cropGenotypingParameterDTO.getCropName(), cropGenotypingParameterDTO,
			this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.crop.name.exceeded.max.length",
			new Integer[] {CropGenotypingParameterValidator.CROP_NAME_MAXLENGTH}, "");

	}

	@Test
	public void testValidateEndpoint() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = new CropGenotypingParameterDTO();
		this.cropGenotypingParameterValidator.validateEndpoint(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.endpoint.is.required");

		cropGenotypingParameterDTO.setEndpoint(RandomStringUtils.random(CropGenotypingParameterValidator.ENDPOINT_MAXLENGTH + 1));
		this.cropGenotypingParameterValidator.validateEndpoint(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.endpoint.exceeded.max.length",
			new Integer[] {CropGenotypingParameterValidator.ENDPOINT_MAXLENGTH}, "");
	}

	@Test
	public void testValidateTokenEndpoint() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = new CropGenotypingParameterDTO();
		this.cropGenotypingParameterValidator.validateTokenEndpoint(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.token.endpoint.is.required");

		cropGenotypingParameterDTO.setTokenEndpoint(RandomStringUtils.random(CropGenotypingParameterValidator.ENDPOINT_MAXLENGTH + 1));
		this.cropGenotypingParameterValidator.validateTokenEndpoint(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.token.endpoint.exceeded.max.length",
			new Integer[] {CropGenotypingParameterValidator.ENDPOINT_MAXLENGTH}, "");
	}

	@Test
	public void testValidateCredentials() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = new CropGenotypingParameterDTO();
		this.cropGenotypingParameterValidator.validateCredentials(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.username.is.required");
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.password.is.required");

		cropGenotypingParameterDTO.setUserName(RandomStringUtils.random(CropGenotypingParameterValidator.USERNAME_MAXLENGTH + 1));
		this.cropGenotypingParameterValidator.validateCredentials(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.username.exceeded.max.length",
			new Integer[] {CropGenotypingParameterValidator.USERNAME_MAXLENGTH}, "");
	}

	@Test
	public void testValidateProgramId() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = new CropGenotypingParameterDTO();
		this.cropGenotypingParameterValidator.validateProgramId(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.programid.is.required");

		cropGenotypingParameterDTO.setUserName(RandomStringUtils.random(CropGenotypingParameterValidator.PROGRAMID_MAXLENGTH + 1));
		this.cropGenotypingParameterValidator.validateProgramId(cropGenotypingParameterDTO, this.errors);
		Mockito.verify(this.errors).reject("crop.genotyping.parameter.programid.exceeded.max.length",
			new Integer[] {CropGenotypingParameterValidator.PROGRAMID_MAXLENGTH}, "");
	}

	private CropGenotypingParameterDTO getCropGenotypingParameterDTO() {
		final CropGenotypingParameterDTO cropGenotypingParameterDTO = new CropGenotypingParameterDTO();
		cropGenotypingParameterDTO.setGenotypingParameterId(1);
		cropGenotypingParameterDTO.setCropName(RandomStringUtils.randomAlphanumeric(10));
		cropGenotypingParameterDTO.setEndpoint(RandomStringUtils.randomAlphanumeric(10));
		cropGenotypingParameterDTO.setTokenEndpoint(RandomStringUtils.randomAlphanumeric(10));
		cropGenotypingParameterDTO.setUserName(RandomStringUtils.randomAlphanumeric(10));
		cropGenotypingParameterDTO.setPassword(RandomStringUtils.randomAlphanumeric(10));
		cropGenotypingParameterDTO.setProgramId(RandomStringUtils.randomAlphanumeric(10));
		return cropGenotypingParameterDTO;
	}

}
