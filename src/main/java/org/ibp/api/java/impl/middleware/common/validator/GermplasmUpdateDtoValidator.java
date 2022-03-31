package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmServiceImpl;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH;

@Component
public class GermplasmUpdateDtoValidator {

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmMiddlewareService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	public void validate(final String programUUID, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmUpdateDTO.class.getName());

		this.validateEmptyList(errors, germplasmUpdateDTOList);
		this.validateAttributeAndNameCodes(errors, programUUID, germplasmUpdateDTOList);
		this.validateAttributeAndNameValues(errors, germplasmUpdateDTOList);
		this.validateGermplasmIdAndGermplasmUUID(errors, germplasmUpdateDTOList);
		this.validateLocationAbbreviation(errors, germplasmUpdateDTOList);
		this.validateBreedingMethod(errors, germplasmUpdateDTOList);
		this.validateCreationDate(errors, germplasmUpdateDTOList);
		this.validateProgenitorsBothMustBeSpecified(errors, germplasmUpdateDTOList);
		this.validateProgenitorsGids(errors, germplasmUpdateDTOList);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	/**
	 * TODO IBP-5324 Review and consolidate approach with GermplasmImportRequestDtoValidator#validateBeforeSaving
	 */
	boolean validateAttributeAndNameValues(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		return germplasmUpdateDTOList.stream().anyMatch(g -> {
			if (this.areNameValuesInvalid(g.getNames().values(), errors)) {
				return true;
			}

			if (this.areAttributesInvalid(g.getAttributes(), errors)) {
				return true;
			}
			return false;
		});
	}

	private boolean areAttributesInvalid(final Map<String, String> attributes, final BindingResult errors) {
		if (attributes != null) {
			return attributes.values().stream().anyMatch(n -> {
				if (StringUtils.isNotEmpty(n) && n.length() > AttributeValidator.GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH) {
					errors.reject("germplasm.import.attribute.value.invalid.length", "");
					return true;
				}
				return false;
			});
		}
		return false;
	}

	private boolean areNameValuesInvalid(final Collection<String> values, final BindingResult errors) {
		return values.stream().anyMatch(n -> {
			/* FIXME germplasm import updates receives empty name columns and filter them out
			if (StringUtils.isEmpty(n)) {
				errors.reject("germplasm.import.name.type.value.null.empty", "");
				return true;
			}
			 */
			if (n.length() > NAME_MAX_LENGTH) {
				errors.reject("germplasm.import.name.type.value.invalid.length", "");
				return true;
			}
			return false;
		});
	}

	protected void validateEmptyList(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		if (germplasmUpdateDTOList == null || germplasmUpdateDTOList.isEmpty()) {
			errors.reject("germplasm.update.empty.list", "");
		}
	}

	protected void validateAttributeAndNameCodes(final BindingResult errors, final String programUUID,
		final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> nameCodes = new HashSet<>();
		germplasmUpdateDTOList
			.forEach(g -> nameCodes.addAll(g.getNames().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList())));
		nameCodes.addAll(
			germplasmUpdateDTOList.stream()
				.map(o -> StringUtils.isNotEmpty(o.getPreferredNameType()) ? o.getPreferredNameType().toUpperCase() : null)
				.filter(Objects::nonNull).collect(
				Collectors.toSet()));
		final Set<String> existingNamesCodes =
			this.germplasmService.filterGermplasmNameTypes(nameCodes).stream().map(nameType -> nameType.getCode().toUpperCase()).collect(
				Collectors.toSet());

		final Set<String> attributesCodes = new HashSet<>();
		germplasmUpdateDTOList.stream().filter(germ -> germ.getAttributes() != null).collect(Collectors.toList())
			.forEach(
				g -> attributesCodes.addAll(g.getAttributes().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList())));

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.setProgramUuid(programUUID);
		VariableType.getAttributeVariableTypes().forEach(variableFilter::addVariableType);
		attributesCodes.forEach(variableFilter::addName);

		final List<Variable> existingAttributeVariables =
			this.ontologyVariableDataManager.getWithFilter(variableFilter);

		final List<String> existingVariablesNamesAndAlias = new ArrayList<>();
		existingAttributeVariables.forEach(v -> {
				existingVariablesNamesAndAlias.add(v.getName().toUpperCase());
				if (StringUtils.isNotEmpty(v.getAlias())) {
					existingVariablesNamesAndAlias.add(v.getAlias().toUpperCase());
				}
			}
		);

		if (!nameCodes.isEmpty() && !nameCodes.equals(existingNamesCodes)) {
			errors.reject("germplasm.update.invalid.name.code", new String[] {
				String.join(",", nameCodes.stream().filter((name) -> !existingNamesCodes.contains(name)).collect(
					Collectors.toList()))}, "");
		}

		if (existingAttributeVariables.size() != attributesCodes.size()) {
			//Check if same variable was used by name or alias
			existingAttributeVariables.forEach(v -> {
				if (attributesCodes.contains(v.getName().toUpperCase()) && StringUtils.isNotEmpty(v.getAlias()) && attributesCodes
					.contains(v.getAlias().toUpperCase())) {
					errors.reject("germplasm.import.two.columns.referring.to.same.variable",
						new String[] {v.getName(), v.getAlias()}, "");
					return;
				}
			});

			attributesCodes.removeAll(existingVariablesNamesAndAlias);
			if (!attributesCodes.isEmpty()) {
				errors.reject("germplasm.update.invalid.attribute.code",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(attributesCodes), 3)}, "");
			}
		}

		final List<String> ambiguousCodes =
			new ArrayList<>(CollectionUtils.intersection(existingNamesCodes, existingVariablesNamesAndAlias));
		if (!ambiguousCodes.isEmpty()) {
			errors.reject("germplasm.update.code.is.defined.as.name.and.attribute", new String[] {String.join(",", ambiguousCodes)}, "");
		}
	}

	protected void validateGermplasmIdAndGermplasmUUID(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		// Find rows (GermplasmUpdateDTO) with blank GID and UUID.
		final Optional<GermplasmUpdateDTO> germplasmWithNoIdentifier =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isEmpty(dto.getGermplasmUUID()) && dto.getGid() == null).findAny();
		if (germplasmWithNoIdentifier.isPresent()) {
			errors.reject("germplasm.update.missing.gid.and.uuid", "");
		}

		final Set<Integer> gids =
			germplasmUpdateDTOList.stream().map(dto -> dto.getGid()).filter(Objects::nonNull).collect(Collectors.toSet());
		final Set<String> germplasmUUIDs =
			germplasmUpdateDTOList.stream().map(dto -> StringUtils.isNotEmpty(dto.getGermplasmUUID()) ? dto.getGermplasmUUID() : null)
				.filter(
					Objects::nonNull).collect(Collectors.toSet());

		final List<Germplasm> germplasmByGIDs = this.germplasmMiddlewareService.getGermplasmByGIDs(new ArrayList<>(gids));
		final List<Germplasm> germplasmByUUIDs = this.germplasmMiddlewareService.getGermplasmByGUIDs(new ArrayList<>(germplasmUUIDs));

		final Set<Integer> existingGids = germplasmByGIDs.stream().map(Germplasm::getGid).collect(Collectors.toSet());
		if (!gids.equals(existingGids)) {
			errors.reject("germplasm.update.invalid.gid", new String[] {
				String.join(",", gids.stream().filter((gid) -> !existingGids.contains(gid)).map(o -> String.valueOf(o)).collect(
					Collectors.toSet()))}, "");
		}
		final Set<String> existingUUIDs = germplasmByUUIDs.stream().map(Germplasm::getGermplasmUUID).collect(Collectors.toSet());
		if (!germplasmUUIDs.equals(germplasmByUUIDs.stream().map(Germplasm::getGermplasmUUID).collect(Collectors.toSet()))) {
			errors.reject("germplasm.update.invalid.uuid", new String[] {
				String.join(",", germplasmUUIDs.stream().filter((uuid) -> !existingUUIDs.contains(uuid)).collect(
					Collectors.toList()))}, "");
		}

	}

	protected void validateLocationAbbreviation(final BindingResult errors,	final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> locationAbbrs =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isNotEmpty(dto.getLocationAbbreviation()))
				.map(dto -> dto.getLocationAbbreviation().toUpperCase()).collect(Collectors.toSet());

		final List<String> abbreviations =
			this.locationService
				.searchLocations(new LocationSearchRequest(null, null, new ArrayList<>(locationAbbrs), null), null, null)
				.stream()
				.map(LocationDTO::getAbbreviation).collect(
				Collectors.toList());

		locationAbbrs.removeAll(abbreviations);

		if (!locationAbbrs.isEmpty()) {
			errors.reject("germplasm.update.invalid.location.abbreviation", new String[] {String.join(",", locationAbbrs)}, "");
		}

	}

	protected void validateBreedingMethod(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final List<String> breedingMethodsAbbrs =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isNotEmpty(dto.getBreedingMethodAbbr()))
				.map(GermplasmUpdateDTO::getBreedingMethodAbbr).collect(Collectors.toList());

		final BreedingMethodSearchRequest breedingMethodSearchRequest = new BreedingMethodSearchRequest();
		breedingMethodSearchRequest.setMethodAbbreviations(breedingMethodsAbbrs);
		final List<String> abbreviations =
			this.breedingMethodService.searchBreedingMethods(breedingMethodSearchRequest, null, null)
				.stream()
				.map(BreedingMethodDTO::getCode).collect(
				Collectors.toList());

		breedingMethodsAbbrs.removeAll(abbreviations);

		if (!breedingMethodsAbbrs.isEmpty()) {
			errors.reject("germplasm.update.invalid.breeding.method", new String[] {String.join(",", breedingMethodsAbbrs)}, "");
		}

	}

	protected void validateCreationDate(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Optional<GermplasmUpdateDTO> optionalGermplasmUpdateDTOWithInvalidDate =
			germplasmUpdateDTOList.stream()
				.filter(o -> StringUtils.isNotEmpty(o.getCreationDate()) && !DateUtil.isValidDate(o.getCreationDate())).findAny();

		if (optionalGermplasmUpdateDTOWithInvalidDate.isPresent()) {
			errors.reject("germplasm.update.invalid.creation.date", "");
		}

	}

	protected void validateProgenitorsGids(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		// Get the gids from progenitors.
		final Set<Integer> progenitorGids =
			germplasmUpdateDTOList.stream().map(dto -> dto.getProgenitors().values()).flatMap(Collection::stream)
				.filter(value -> value != null && value != 0).collect(Collectors.toSet());
		final List<Germplasm> germplasmByGIDs = this.germplasmMiddlewareService.getGermplasmByGIDs(new ArrayList<>(progenitorGids));
		final Set<Integer> existingGids = germplasmByGIDs.stream().map(Germplasm::getGid).collect(Collectors.toSet());
		if (!progenitorGids.equals(existingGids)) {
			errors.reject("germplasm.update.invalid.progenitors.gids", new String[] {
				String.join(",",
					progenitorGids.stream().filter((gid) -> !existingGids.contains(gid)).map(o -> String.valueOf(o)).collect(
						Collectors.toSet()))}, "");
		}

	}

	protected void validateProgenitorsBothMustBeSpecified(final BindingResult errors,
		final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		final long
			progenitorsWithEmptyValuesCount =
			germplasmUpdateDTOList.stream().filter(dto -> {
				final Integer progenitor1 = dto.getProgenitors().getOrDefault(GermplasmServiceImpl.PROGENITOR_1, null);
				final Integer progenitor2 = dto.getProgenitors().getOrDefault(GermplasmServiceImpl.PROGENITOR_2, null);
				return (progenitor1 != null && progenitor2 == null) || (progenitor1 == null
					&& progenitor2 != null);
			}).count();
		if (progenitorsWithEmptyValuesCount > 0) {
			errors.reject("germplasm.update.invalid.progenitors", "");
		}

	}

}
