package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.germplasm.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmImportRequestDto;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GermplasmImportRequestDtoValidator {

	private BindingResult errors;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private BreedingMethodService breedingMethodService;

	public void validate(final String crop, final String programUUID, final GermplasmImportRequestDto germplasmImportRequestDto) {
		errors = new MapBindingResult(new HashMap<String, String>(), GermplasmImportRequestDto.class.getName());

		BaseValidator.checkNotNull(germplasmImportRequestDto, "germplasm.import.request.null");
		BaseValidator.checkNotEmpty(germplasmImportRequestDto.getGermplasmSet(), "germplasm.import.set.null");

		final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos = germplasmImportRequestDto.getGermplasmSet();

		final Set<Integer> clientIds = new HashSet<>();
		boolean invalid = germplasmDtos.stream().anyMatch(g -> {

			if (g == null) {
				errors.reject("germplasm.import.germplasm.null", "");
				return true;
			}

			if (g.getNames() == null || g.getNames().isEmpty()) {
				errors.reject("germplasm.import.names.null.or.empty", "");
				return true;
			}

			if (StringUtils.isEmpty(g.getPreferredName())) {
				errors.reject("germplasm.import.preferred.name.null", "");
				return true;
			}

			if (StringUtils.isEmpty(g.getReference()) && g.getReference().length() > 255) {
				errors.reject("germplasm.import.reference.length.error", "");
				return true;
			}

			if (g.getCreationDate() == null) {
				errors.reject("germplasm.import.creation.date.null", "");
				return true;
			}

			if (g.getClientId() == null) {
				errors.reject("germplasm.import.client.id.null", "");
				return true;
			}

			if (clientIds.contains(g.getClientId())) {
				errors.reject("germplasm.import.client.id.duplicated", "");
				return true;
			}

			clientIds.add(g.getClientId());

			if (StringUtils.isEmpty(g.getBreedingMethodAbbr())) {
				errors.reject("germplasm.import.breeding.method.mandatory", "");
				return true;
			}

			if (StringUtils.isEmpty(g.getLocationAbbr())) {
				errors.reject("germplasm.import.location.mandatory", "");
				return true;
			}

			if (StringUtil.isEmpty(g.getGuid()) && g.getGuid().length() > 36) {
				errors.reject("germplasm.import.guid.invalid.length", "");
				return true;
			}

			if (!g.getNames().containsKey(g.getPreferredName())) {
				errors.reject("germplasm.import.preferred.name.invalid", "");
				return true;
			}

			if (g.getNames().values().stream().anyMatch(n -> {
				if (StringUtils.isEmpty(n)) {
					errors.reject("germplasm.import.name.type.value.null.empty", "");
					return true;
				}
				if (n.length() > 255) {
					errors.reject("germplasm.import.name.type.value.invalid.length", "");
					return true;
				}
				return false;
			})) {
				return true;
			}

			if (g.getAttributes() != null && g.getAttributes().values().stream().anyMatch(n -> {
				if (StringUtils.isEmpty(n)) {
					errors.reject("germplasm.import.name.type.value.null.empty", "");
					return true;
				}
				if (n.length() > 255) {
					errors.reject("germplasm.import.name.type.value.invalid.length", "");
					return true;
				}
				return false;
			})) {
				return true;
			}

			return false;

		});

		if (invalid) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.validateGUIDNotExists(germplasmDtos);
		this.validateAllBreedingMethodAbbreviationsExists(crop, programUUID, germplasmDtos);
		this.validateAllLocationAbbreviationsExists(germplasmDtos);
		this.validateAllNameTypesExists(germplasmDtos);
		this.validateAllAttributesExists(germplasmDtos);

	}

	public void validateAllNameTypesExists(final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {
		final Set<String> nameTypes = new HashSet<>();
		germplasmDtos.forEach(g -> nameTypes.addAll(g.getNames().keySet()));
		final List<String> existingGermplasmNameTypes =
			this.germplasmService.getGermplasmNameTypesByCodes(nameTypes).stream().map(GermplasmNameTypeDTO::getCode).collect(
				Collectors.toList());
		if (existingGermplasmNameTypes.size() != nameTypes.size()) {
			nameTypes.remove(existingGermplasmNameTypes);
			errors.reject("germplasm.import.name.types.not.exist",
				new String[] {Util.buildErrorMessageFromList(new ArrayList<>(nameTypes), 3)}, "");
		}
	}

	public void validateAllBreedingMethodAbbreviationsExists(final String crop, final String programUUID,
		final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {
		final Set<String> breedingMethodsAbbrs =
			germplasmDtos.stream().map(GermplasmImportRequestDto.GermplasmDto::getBreedingMethodAbbr).collect(
				Collectors.toSet());
		final List<String> existingBreedingMethods =
			this.breedingMethodService.getBreedingMethods(crop, programUUID, breedingMethodsAbbrs, false).stream().map(
				BreedingMethodDTO::getCode).collect(Collectors.toList());
		if (breedingMethodsAbbrs.size() != existingBreedingMethods.size()) {
			breedingMethodsAbbrs.remove(existingBreedingMethods);
			errors.reject("germplasm.import.breeding.methods.not.exist",
				new String[] {Util.buildErrorMessageFromList(new ArrayList<>(breedingMethodsAbbrs), 3)}, "");
		}

	}

	public void validateAllLocationAbbreviationsExists(final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {

	}

	public void validateAllAttributesExists(final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {

	}

	public void validateGUIDNotExists(final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {

	}

}
