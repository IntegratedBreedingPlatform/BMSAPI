package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.UDTableType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttributeValidator {

	private static List<String> ALLOWED_ATTRIBUTE_TYPES = Arrays.asList(UDTableType.ATRIBUTS_ATTRIBUTE.getType(),
		UDTableType.ATRIBUTS_PASSPORT.getType());

	private static Integer GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH = 255;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private GermplasmService germplasmService;

	public void validateAttributeIds(final BindingResult errors, final List<String> attributeIds) {
		if (attributeIds == null || attributeIds.isEmpty()) {
			return;
		}

		final List<Integer> ids =
			attributeIds.stream().map(attributeId -> Integer.valueOf(attributeId)).distinct().collect(Collectors.toList());

		final List<Attribute> attributes = this.germplasmDataManager.getAttributeByIds(ids);
		if (attributes.size() != ids.size()) {
			errors.reject("attribute.invalid", "");
		}
	}

	public void validateAttributeType(final BindingResult errors, final String attributeType) {
		if (StringUtils.isEmpty(attributeType)) {
			return;
		}

		if (!AttributeValidator.ALLOWED_ATTRIBUTE_TYPES.contains(attributeType.toUpperCase())) {
			errors.reject("attribute.type.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateAttributeCode(final BindingResult errors, final GermplasmAttributeRequestDto dto) {
		final List<AttributeDTO> attributeDTOS = this.germplasmService
			.filterGermplasmAttributes(Collections.singleton(dto.getAttributeCode()), dto.getAttributeType());
		if(attributeDTOS.isEmpty()) {
			errors.reject("attribute.code.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeShouldNotExist(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto) {
		final List<GermplasmAttributeDto> germplasmAttributeDtos = this.germplasmAttributeService.getGermplasmAttributeDtos(gid,
			dto.getAttributeType());

		final List<GermplasmAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getAttributeCode().equalsIgnoreCase(dto.getAttributeCode())).collect(Collectors.toList());
		if (!existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.code.invalid.existing", new String[] {dto.getAttributeCode()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateGermplasmAttributeExisting(final BindingResult errors, final Integer gid, final Integer attributeId) {
		final List<GermplasmAttributeDto> germplasmAttributeDtos = this.germplasmAttributeService.getGermplasmAttributeDtos(gid,
			null);
		List<GermplasmAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new String[] {attributeId.toString()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeForUpdate(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto,
		final Integer attributeId) {
		final List<GermplasmAttributeDto> germplasmAttributeDtos = this.germplasmAttributeService.getGermplasmAttributeDtos(gid,
			dto.getAttributeType());


		// Filter by germplasm attribute id
		List<GermplasmAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new String[] {attributeId.toString()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		// Filter by germplasm attribute code
		existingGermplasmAttributes = existingGermplasmAttributes.stream()
			.filter(existing -> existing.getAttributeCode().equalsIgnoreCase(dto.getAttributeCode())).collect(Collectors.toList());
		if (existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.code.update.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateAttribute(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto,
		final Integer attributeId) {
		this.validateAttributeCode(errors, dto);
		if(attributeId == null) {
			this.validateGermplasmAttributeShouldNotExist(errors, gid, dto);
		} else {
			this.validateGermplasmAttributeForUpdate(errors, gid, dto, attributeId);
		}
		this.validateGermplasmAttributeValue(errors, dto.getValue());
		this.validateGermplasmAttributeDate(errors, dto.getDate());
	}

	void validateGermplasmAttributeValue(final BindingResult errors, final String value) {
		if (value != null && value.length() > GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH) {
			errors.reject("attribute.value.invalid.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeDate(final BindingResult errors, final String date) {
		if (!DateUtil.isValidDate(date)) {
			errors.reject("attribute.date.invalid.format", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}


	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setGermplasmService(final GermplasmService germplasmService) {
		this.germplasmService = germplasmService;
	}

	void setGermplasmAttributeService(final  GermplasmAttributeService germplasmAttributeService) {
		this.germplasmAttributeService = germplasmAttributeService;
	}
}
