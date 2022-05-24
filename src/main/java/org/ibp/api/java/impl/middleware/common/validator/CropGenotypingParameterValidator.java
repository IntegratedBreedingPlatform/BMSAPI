package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.service.api.crop.CropGenotypingParameterService;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.Optional;

@Component
public class CropGenotypingParameterValidator {

	public static final int CROP_NAME_MAXLENGTH = 32;
	public static final int ENDPOINT_MAXLENGTH = 500;
	public static final int USERNAME_MAXLENGTH = 45;
	public static final int PROGRAMID_MAXLENGTH = 45;

	@Autowired
	private CropGenotypingParameterService cropGenotypingParameterService;

	public void validateCreation(final String pathCropName, final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), CropGenotypingParameterDTO.class.getName());
		this.validateCropName(pathCropName, cropGenotypingParameterDTO, errors);
		this.validateEndpoint(cropGenotypingParameterDTO, errors);
		this.validateTokenEndpoint(cropGenotypingParameterDTO, errors);
		this.validateCredentials(cropGenotypingParameterDTO, errors);
		this.validateProgramId(cropGenotypingParameterDTO, errors);
		this.validateCropGenotypingParameterAlreadyExists(cropGenotypingParameterDTO, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateEdition(final String cropPathName, final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), CropGenotypingParameterDTO.class.getName());
		this.validateCropName(cropPathName, cropGenotypingParameterDTO, errors);
		this.validateEndpoint(cropGenotypingParameterDTO, errors);
		this.validateTokenEndpoint(cropGenotypingParameterDTO, errors);
		this.validateCredentials(cropGenotypingParameterDTO, errors);
		this.validateProgramId(cropGenotypingParameterDTO, errors);
		this.validateCropGenotypingParameterId(cropGenotypingParameterDTO, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	protected void validateCropGenotypingParameterAlreadyExists(final CropGenotypingParameterDTO cropGenotypingParameterDTO,
		final BindingResult errors) {
		if (StringUtils.isNotBlank(cropGenotypingParameterDTO.getCropName())) {
			final Optional<CropGenotypingParameterDTO> cropGenotypingParameter =
				this.cropGenotypingParameterService.getCropGenotypingParameter(cropGenotypingParameterDTO.getCropName());
			if (cropGenotypingParameter.isPresent()) {
				errors.reject("crop.genotyping.parameter.record.already.exists", new String[] {cropGenotypingParameterDTO.getCropName()},
					"");
			}
		}
	}

	protected void validateCropGenotypingParameterId(final CropGenotypingParameterDTO cropGenotypingParameterDTO,
		final BindingResult errors) {
		final Optional<CropGenotypingParameterDTO> cropGenotypingParameter =
			this.cropGenotypingParameterService.getCropGenotypingParameterById(cropGenotypingParameterDTO.getGenotypingParameterId());
		if (!cropGenotypingParameter.isPresent()) {
			errors.reject("crop.genotyping.parameter.record.id.not.exists",
				new String[] {String.valueOf(cropGenotypingParameterDTO.getGenotypingParameterId())}, "");
		}
		if (cropGenotypingParameter.isPresent() && !cropGenotypingParameter.get().getCropName()
			.equalsIgnoreCase(cropGenotypingParameterDTO.getCropName())) {
			errors.reject("crop.genotyping.parameter.genotyping.parameter.crop.name.mismatch");
		}

	}

	protected void validateCropName(final String pathCropName, final CropGenotypingParameterDTO cropGenotypingParameterDTO,
		final BindingResult errors) {
		if (StringUtils.isNotBlank(pathCropName) && !pathCropName.equalsIgnoreCase(cropGenotypingParameterDTO.getCropName())) {
			errors.reject("crop.genotyping.parameter.crop.name.path.mismatch");
		}
		if (StringUtils.isBlank(cropGenotypingParameterDTO.getCropName())) {
			errors.reject("crop.genotyping.parameter.crop.name.is.required");
		}
		if (StringUtils.isNotBlank(cropGenotypingParameterDTO.getCropName())
			&& cropGenotypingParameterDTO.getCropName().length() > CROP_NAME_MAXLENGTH) {
			errors.reject("crop.genotyping.parameter.crop.name.exceeded.max.length", new Integer[] {CROP_NAME_MAXLENGTH}, "");
		}
	}

	protected void validateEndpoint(final CropGenotypingParameterDTO cropGenotypingParameterDTO, final BindingResult errors) {
		if (StringUtils.isBlank(cropGenotypingParameterDTO.getEndpoint())) {
			errors.reject("crop.genotyping.parameter.endpoint.is.required");
		}
		if (StringUtils.isNotBlank(cropGenotypingParameterDTO.getEndpoint())
			&& cropGenotypingParameterDTO.getEndpoint().length() > ENDPOINT_MAXLENGTH) {
			errors.reject("crop.genotyping.parameter.endpoint.exceeded.max.length", new Integer[] {ENDPOINT_MAXLENGTH}, "");
		}
	}

	protected void validateTokenEndpoint(final CropGenotypingParameterDTO cropGenotypingParameterDTO, final BindingResult errors) {
		if (StringUtils.isBlank(cropGenotypingParameterDTO.getTokenEndpoint())) {
			errors.reject("crop.genotyping.parameter.token.endpoint.is.required");
		}
		if (StringUtils.isNotBlank(cropGenotypingParameterDTO.getTokenEndpoint())
			&& cropGenotypingParameterDTO.getTokenEndpoint().length() > ENDPOINT_MAXLENGTH) {
			errors.reject("crop.genotyping.parameter.token.endpoint.exceeded.max.length", new Integer[] {ENDPOINT_MAXLENGTH}, "");
		}
	}

	protected void validateCredentials(final CropGenotypingParameterDTO cropGenotypingParameterDTO, final BindingResult errors) {
		if (StringUtils.isBlank(cropGenotypingParameterDTO.getUserName())) {
			errors.reject("crop.genotyping.parameter.username.is.required");
		}
		if (StringUtils.isNotBlank(cropGenotypingParameterDTO.getUserName())
			&& cropGenotypingParameterDTO.getUserName().length() > USERNAME_MAXLENGTH) {
			errors.reject("crop.genotyping.parameter.username.exceeded.max.length", new Integer[] {USERNAME_MAXLENGTH}, "");
		}
		if (StringUtils.isBlank(cropGenotypingParameterDTO.getPassword())) {
			errors.reject("crop.genotyping.parameter.password.is.required");
		}
	}

	protected void validateProgramId(final CropGenotypingParameterDTO cropGenotypingParameterDTO, final BindingResult errors) {

		if (StringUtils.isBlank(cropGenotypingParameterDTO.getProgramId())) {
			errors.reject("crop.genotyping.parameter.programid.is.required");
		}
		if (StringUtils.isNotBlank(cropGenotypingParameterDTO.getUserName())
			&& cropGenotypingParameterDTO.getUserName().length() > PROGRAMID_MAXLENGTH) {
			errors.reject("crop.genotyping.parameter.programid.exceeded.max.length", new Integer[] {PROGRAMID_MAXLENGTH}, "");
		}
	}

}
