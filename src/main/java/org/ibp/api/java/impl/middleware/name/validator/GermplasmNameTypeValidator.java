package org.ibp.api.java.impl.middleware.name.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.constant.SystemNameTypes;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class GermplasmNameTypeValidator {

	public static final Integer CODE_OR_NAME_MAX_LENGTH = 50;
	public static final Integer DESCRIPTION_MAX_LENGTH = 255;

	private BindingResult errors;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private GermplasmNameService germplasmNameService;

	public void validateNameTypeCreation(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameTypeRequestDTO.class.getName());
		this.validateNameTypeCode(germplasmNameTypeRequestDTO);
		this.validateIfCodeAlreadyExists(germplasmNameTypeRequestDTO.getCode(), null);
		this.validateNameTypeName(germplasmNameTypeRequestDTO);
		this.validateIfNameAlreadyExists(germplasmNameTypeRequestDTO.getName(), null);
		this.validateNameTypeDescription(germplasmNameTypeRequestDTO);
	}

	public void validateNameTypeDeletion(final Integer nameTypeId) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameTypeRequestDTO.class.getName());
		final GermplasmNameTypeDTO germplasmNameTypeDTO = this.validateExistingNameType(nameTypeId);
		this.validateNameTypeBelongsToSystem(germplasmNameTypeDTO.getCode());
		this.validateNameTypeBelongsToGermplasm(nameTypeId);
		this.validateNameTypeBelongsToStudy(nameTypeId);
	}

	public void validateNameTypeModification(final Integer nameTypeId, final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameTypeRequestDTO.class.getName());

		final GermplasmNameTypeDTO germplasmNameTypeDTO = this.validateExistingNameType(nameTypeId);

		final boolean used = this.isNameTypeInUse(germplasmNameTypeDTO);

		if (used) {
			if (germplasmNameTypeRequestDTO.getCode() != null && !germplasmNameTypeRequestDTO.getCode()
				.equalsIgnoreCase(germplasmNameTypeDTO.getCode())) {
				this.errors.reject("germplasm.name.type.code.can.not.be.modified", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
			if (germplasmNameTypeRequestDTO.getName() != null && !germplasmNameTypeRequestDTO.getName()
				.equalsIgnoreCase(germplasmNameTypeDTO.getName())) {
				this.errors.reject("germplasm.name.type.name.can.not.be.modified", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		} else {
			if (germplasmNameTypeRequestDTO.getCode() != null) {
				this.validateNameTypeCode(germplasmNameTypeRequestDTO);
				this.validateIfCodeAlreadyExists(germplasmNameTypeRequestDTO.getCode(), nameTypeId);
			}

			if (germplasmNameTypeRequestDTO.getName() != null) {
				this.validateNameTypeName(germplasmNameTypeRequestDTO);
				this.validateIfNameAlreadyExists(germplasmNameTypeRequestDTO.getName(), nameTypeId);
			}
		}

		this.validateNameTypeDescription(germplasmNameTypeRequestDTO);
	}

	private void validateNameTypeDescription(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		if (StringUtils.isNotBlank(germplasmNameTypeRequestDTO.getDescription()) && //
			germplasmNameTypeRequestDTO.getDescription().length() > GermplasmNameTypeValidator.DESCRIPTION_MAX_LENGTH) {
			this.errors.reject("germplasm.name.type.description.length.invalid",
				new String[] {GermplasmNameTypeValidator.DESCRIPTION_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNameTypeName(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		if (StringUtils.isBlank(germplasmNameTypeRequestDTO.getName())) {
			this.errors.reject("germplasm.name.type.name.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (germplasmNameTypeRequestDTO.getName().length() > GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH) {
			this.errors.reject("germplasm.name.type.name.length.invalid",
				new String[] {GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNameTypeCode(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		if (StringUtils.isBlank(germplasmNameTypeRequestDTO.getCode())) {
			this.errors.reject("germplasm.name.type.code.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());

		}

		if (germplasmNameTypeRequestDTO.getCode().length() > GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH) {
			this.errors.reject("germplasm.name.type.code.length.invalid",
				new String[] {GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final String codeTrimmed = StringUtils.deleteWhitespace(germplasmNameTypeRequestDTO.getCode());
		if (germplasmNameTypeRequestDTO.getCode().length() != codeTrimmed.length()) {
			this.errors.reject("germplasm.name.type.code.whitespace", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	protected void validateIfCodeAlreadyExists(final String code, final Integer nameTypeId) {
		final Set<String> codes = new HashSet<>(Arrays.asList(code));
		final List<GermplasmNameTypeDTO> nameTypeDTOS = this.germplasmNameTypeService.filterGermplasmNameTypes(codes);
		if (!nameTypeDTOS.isEmpty() && (nameTypeId == null || !nameTypeDTOS.get(0).getId().equals(nameTypeId))) {
			this.errors.reject("germplasm.name.type.code.invalid", new String[] {code}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	protected void validateIfNameAlreadyExists(final String name, final Integer nameTypeId) {
		final List<GermplasmNameTypeDTO> nameTypeDTOS = this.germplasmNameTypeService.filterGermplasmNameTypesByName(name);
		if (!nameTypeDTOS.isEmpty() && (nameTypeId == null || !nameTypeDTOS.get(0).getId().equals(nameTypeId))) {
			this.errors.reject("germplasm.name.type.name.invalid", new String[] {name}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private GermplasmNameTypeDTO validateExistingNameType(final Integer nameTypeId) {
		final Optional<GermplasmNameTypeDTO> germplasmNameTypeDTO = this.germplasmNameTypeService.getNameTypeById(nameTypeId);

		if (!germplasmNameTypeDTO.isPresent()) {
			this.errors.reject("germplasm.name.type.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		return germplasmNameTypeDTO.get();
	}

	public void validate(final Integer nameTypeId) {
		final Optional<GermplasmNameTypeDTO> germplasmNameTypeDTO = this.germplasmNameTypeService.getNameTypeById(nameTypeId);

		if (!germplasmNameTypeDTO.isPresent()) {
			this.errors.reject("germplasm.name.type.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private boolean isNameTypeInUse(final GermplasmNameTypeDTO germplasmNameTypeDTO) {
		final boolean isSystem = SystemNameTypes.getTypes().contains(germplasmNameTypeDTO.getCode());
		final boolean isNameTypeUsedInGermplasmName = this.germplasmNameService.isNameTypeUsedAsGermplasmName(germplasmNameTypeDTO.getId());
		return isSystem || isNameTypeUsedInGermplasmName;
	}

	private void validateNameTypeBelongsToSystem(final String code) {
		if (SystemNameTypes.getTypes().contains(code)) {
			this.errors.reject("germplasm.name.type.used.for.the.system", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNameTypeBelongsToGermplasm(final Integer nameTypeId) {
		final boolean isNameTypeUsedInGermplasmName = this.germplasmNameService.isNameTypeUsedAsGermplasmName(nameTypeId);
		if (isNameTypeUsedInGermplasmName) {
			this.errors.reject("germplasm.name.type.is.in.use", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNameTypeBelongsToStudy(final Integer nameTypeId) {
		final boolean isNameTypeUsedInGermplasmName = this.germplasmNameService.isNameTypeUsedInStudies(nameTypeId);
		if (isNameTypeUsedInGermplasmName) {
			this.errors.reject("germplasm.name.type.used.in.projectprop", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
