package org.ibp.api.java.impl.middleware.name.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class GermplasmNameTypeValidator {

	public static final Integer CODE_OR_NAME_MAX_LENGTH = 50;
	public static final Integer DESCRIPTION_MAX_LENGTH = 255;
	public static final List<String> SYSTEM_NAME_TYPES = Collections.unmodifiableList(
		Arrays.asList("LNAME", "CODE1", "CODE2", "CODE3", "PEDIGREE", "CROSS NAME", "CROSSING NAME", "DERIVATIVE NAME", "SELHISFIX"));

	private BindingResult errors;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private GermplasmNameService germplasmNameService;

	public void validate(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO, final Integer nameTypeId) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameTypeRequestDTO.class.getName());

		if (nameTypeId != null) {
			this.validateExistingNameType(nameTypeId);

			if (StringUtils.isNotBlank(germplasmNameTypeRequestDTO.getCode())) {
				this.validateNameTypeCode(germplasmNameTypeRequestDTO);
				this.validateIfCodeAlreadyExists(germplasmNameTypeRequestDTO.getCode(), nameTypeId);

			}

			if (StringUtils.isNotBlank(germplasmNameTypeRequestDTO.getName())) {
				this.validateNameTypeName(germplasmNameTypeRequestDTO);
				this.validateIfNameAlreadyExists(germplasmNameTypeRequestDTO.getName(), nameTypeId);
			}

		} else {

			this.validateNameTypeCode(germplasmNameTypeRequestDTO);
			this.validateIfCodeAlreadyExists(germplasmNameTypeRequestDTO.getCode(), nameTypeId);
			this.validateNameTypeName(germplasmNameTypeRequestDTO);
			this.validateIfNameAlreadyExists(germplasmNameTypeRequestDTO.getName(), nameTypeId);
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

	private void validateExistingNameType(final Integer nameTypeId) {
		final Optional<GermplasmNameTypeDTO> germplasmNameTypeDTO = this.germplasmNameTypeService.getNameTypeById(nameTypeId);

		if (!germplasmNameTypeDTO.isPresent()) {
			this.errors.reject("germplasm.name.type.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateNameTypeBelongsToSystem(germplasmNameTypeDTO.get().getCode());
		this.validateNameTypeBelongsToGermplasm(nameTypeId);
		this.validateNameTypeBelongsToGermplasmList(germplasmNameTypeDTO.get().getName());
	}

	private void validateNameTypeBelongsToSystem(final String code) {
		if (GermplasmNameTypeValidator.SYSTEM_NAME_TYPES.contains(code)) {
			this.errors.reject("germplasm.name.type.used.for.the.system", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNameTypeBelongsToGermplasm(final Integer nameTypeId) {
		final boolean isNameTypeUsedInGermplasmName = this.germplasmNameService.isNameTypeUsedAsGermplasmName(nameTypeId);
		if (isNameTypeUsedInGermplasmName) {
			this.errors.reject("germplasm.name.type.is.in.used", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNameTypeBelongsToGermplasmList(final String name) {
		final boolean isNameTypeAssociatedToList =
			this.germplasmNameTypeService.isNameTypeUsedInListDataProp(name);

		if (isNameTypeAssociatedToList) {
			this.errors.reject("germplasm.name.type.asociated.to.list", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateCanBeDeleted(final Integer nameTypeId) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameTypeRequestDTO.class.getName());
		this.validateExistingNameType(nameTypeId);
	}
}
