package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmImportRequestDto;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
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
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmServiceMw;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private LocationDataManager locationDataManager;

	public void validate(final String programUUID, final GermplasmImportRequestDto germplasmImportRequestDto) {
		errors = new MapBindingResult(new HashMap<String, String>(), GermplasmImportRequestDto.class.getName());

		BaseValidator.checkNotNull(germplasmImportRequestDto, "germplasm.import.request.null");
		BaseValidator.checkNotEmpty(germplasmImportRequestDto.getGermplasmList(), "germplasm.import.set.null");

		final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos = germplasmImportRequestDto.getGermplasmList();

		final Set<Integer> clientIds = new HashSet<>();
		boolean invalid = germplasmDtos.stream().anyMatch(g -> {

			final Set<String> nameKeys = new HashSet<>();
			final Set<String> attributeKeys = new HashSet<>();

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

			if (!StringUtils.isEmpty(g.getReference()) && g.getReference().length() > 255) {
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

			if (!StringUtil.isEmpty(g.getGuid()) && g.getGuid().length() > 36) {
				errors.reject("germplasm.import.guid.invalid.length", "");
				return true;
			}

			g.getNames().keySet().forEach(name -> nameKeys.add(name.toUpperCase()));
			if (g.getNames().keySet().size() != nameKeys.size()) {
				errors.reject("germplasm.import.duplicated.name.types", new String[] {g.getClientId().toString()}, "");
				return true;
			}

			if (!nameKeys.contains(g.getPreferredName().toUpperCase())) {
				errors.reject("germplasm.import.preferred.name.invalid", "");
				return true;
			}

			if (g.getAttributes() != null) {
				g.getAttributes().keySet().forEach(attr -> attributeKeys.add(attr.toUpperCase()));
				if (g.getAttributes().keySet().size() != attributeKeys.size()) {
					errors.reject("germplasm.import.duplicated.attributes", new String[] {g.getClientId().toString()}, "");
					return true;
				}
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
					errors.reject("germplasm.import.attribute.value.null.empty", "");
					return true;
				}
				if (n.length() > 255) {
					errors.reject("germplasm.import.attribute.value.invalid.length", "");
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
		this.validateAllBreedingMethodAbbreviationsExists(programUUID, germplasmDtos);
		this.validateAllLocationAbbreviationsExists(programUUID, germplasmDtos);
		this.validateAllNameTypesExists(germplasmDtos);
		this.validateAllAttributesExists(germplasmDtos);

	}

	private void validateAllNameTypesExists(final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {
		final Set<String> nameTypes = new HashSet<>();
		germplasmDtos.forEach(g -> nameTypes.addAll(g.getNames().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList())));
		final List<String> existingGermplasmNameTypes =
			this.germplasmService.getGermplasmNameTypesByCodes(nameTypes).stream().map(GermplasmNameTypeDTO::getCode).collect(
				Collectors.toList());
		if (existingGermplasmNameTypes.size() != nameTypes.size()) {
			nameTypes.removeAll(existingGermplasmNameTypes);
			errors.reject("germplasm.import.name.types.not.exist",
				new String[] {Util.buildErrorMessageFromList(new ArrayList<>(nameTypes), 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateAllBreedingMethodAbbreviationsExists(final String programUUID,
		final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {
		final Set<String> breedingMethodsAbbrs =
			germplasmDtos.stream().map(g -> g.getBreedingMethodAbbr().toUpperCase()).collect(
				Collectors.toSet());
		final List<String> existingBreedingMethods =
			this.breedingMethodService.getBreedingMethods(programUUID, breedingMethodsAbbrs, false).stream().map(
				BreedingMethodDTO::getCode).collect(Collectors.toList());
		if (breedingMethodsAbbrs.size() != existingBreedingMethods.size()) {
			breedingMethodsAbbrs.removeAll(existingBreedingMethods);
			errors.reject("germplasm.import.breeding.methods.not.exist",
				new String[] {Util.buildErrorMessageFromList(new ArrayList<>(breedingMethodsAbbrs), 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	private void validateAllLocationAbbreviationsExists(final String programUUID,
		final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {
		final Set<String> locationAbbrs =
			germplasmDtos.stream().map(g -> g.getLocationAbbr().toUpperCase()).collect(Collectors.toSet());
		final List<String> existingLocations =
			this.locationDataManager.getFilteredLocations(programUUID, null, null, new ArrayList<>(locationAbbrs), false).stream().map(
				Location::getLabbr).collect(
				Collectors.toList());
		if (locationAbbrs.size() != existingLocations.size()) {
			locationAbbrs.removeAll(existingLocations);
			errors.reject("germplasm.import.location.abbreviations.not.exist",
				new String[] {Util.buildErrorMessageFromList(new ArrayList<>(locationAbbrs), 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateAllAttributesExists(final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {
		final Set<String> attributes = new HashSet<>();
		germplasmDtos.stream().filter(germ -> germ.getAttributes() != null).collect(Collectors.toList())
			.forEach(g -> attributes.addAll(g.getAttributes().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList())));
		if (!attributes.isEmpty()) {
			final List<String> existingGermplasmAttributes =
				this.germplasmService.getGermplasmAttributesByCodes(attributes).stream().map(AttributeDTO::getCode).collect(
					Collectors.toList());
			if (existingGermplasmAttributes.size() != attributes.size()) {
				attributes.removeAll(existingGermplasmAttributes);
				errors.reject("germplasm.import.attributes.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(attributes), 3)}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

	private void validateGUIDNotExists(final List<GermplasmImportRequestDto.GermplasmDto> germplasmDtos) {
		final List<String> guidsList =
			germplasmDtos.stream().filter(g -> !StringUtils.isEmpty(g.getGuid())).map(GermplasmImportRequestDto.GermplasmDto::getGuid)
				.collect(
					Collectors.toList());
		if (!guidsList.stream().filter(i -> Collections.frequency(guidsList, i) > 1)
			.collect(Collectors.toSet()).isEmpty()) {
			errors.reject("germplasm.import.duplicated.guids", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		final List<Germplasm> germplasmDTOS = this.germplasmServiceMw.getGermplasmByGUIDs(guidsList);
		if (!germplasmDTOS.isEmpty()) {
			errors.reject("germplasm.import.existent.guids",
				new String[] {
					Util.buildErrorMessageFromList(germplasmDTOS.stream().map(Germplasm::getGermplasmUUID).collect(Collectors.toList()),
						3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
