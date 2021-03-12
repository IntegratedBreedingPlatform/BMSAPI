package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class GermplasmImportRequestValidator {

	public static final Integer NAME_MAX_LENGTH = 255;
	public static final Integer ATTRIBUTE_MAX_LENGTH = 255;

	private BindingResult errors;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private LocationService locationService;

	public BindingResult pruneGermplasmInvalidForImport(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		BaseValidator.checkNotEmpty(germplasmImportRequestDtoList, "germplasm.import.list.null");
		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmImportRequest.class.getName());

		if (!germplasmImportRequestDtoList.stream().filter(i -> Collections.frequency(germplasmImportRequestDtoList, i) > 1)
			.collect(Collectors.toSet()).isEmpty()) {
			errors.reject("germplasm.import.duplicated.objects"
				+ "", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<String> validBreedingMethodIds = this.getValidBreedingMethodDbIds(germplasmImportRequestDtoList);
		final List<String> validLocationAbbreviations = this.getValidLocationAbbreviations(germplasmImportRequestDtoList);

		final Map<GermplasmImportRequest, Integer> importRequestByIndexMap = IntStream.range(0, germplasmImportRequestDtoList.size())
			.boxed()
			.collect(Collectors.toMap(germplasmImportRequestDtoList::get, i -> i));
		germplasmImportRequestDtoList.removeIf( g -> {
			final Set<String> nameKeys = new HashSet<>();

			final Integer index = importRequestByIndexMap.get(g) + 1;
			if (StringUtils.isEmpty(g.getDefaultDisplayName())) {
				errors.reject("germplasm.create.null.name.types", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isEmpty(g.getAcquisitionDate())) {
				errors.reject("germplasm.create.acquisition.date.null", new String[] {index.toString()}, "");
				return true;
			}
			if (Util.tryParseDate(g.getAcquisitionDate(), Util.FRONTEND_DATE_FORMAT) == null) {
				errors.reject("germplasm.create.acquisition.date.invalid.format", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isNotEmpty(g.getBreedingMethodDbId()) && !validBreedingMethodIds.contains(g.getBreedingMethodDbId())) {
				errors.reject("germplasm.create.breeding.method.invalid", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isEmpty(g.getCountryOfOriginCode())) {
				errors.reject("germplasm.create.country.origin.null", new String[] {index.toString()}, "");
				return true;
			}

			if (!validLocationAbbreviations.contains(g.getCountryOfOriginCode())) {
				errors.reject("germplasm.create.country.origin.invalid", new String[] {index.toString()}, "");
				return true;
			}

			// Validations on names and synonyms
			if (isAnyCustomNameFieldInvalid(g, index))
				return true;
			if (g.getSynonyms().stream().map(Synonym::getType).anyMatch(Objects::isNull)) {
				errors.reject("germplasm.create.null.name.types", new String[] {index.toString()}, "");
				return true;
			}
			g.getSynonyms().stream().map(Synonym::getType).forEach(name -> nameKeys.add(name.toUpperCase()));
			if (g.getSynonyms().size() != nameKeys.size()) {
				errors.reject("germplasm.create.duplicated.name.types", new String[] {index.toString()}, "");
				return true;
			}
			if (areNameValuesInvalid(g.getSynonyms().stream().map(Synonym::getSynonym).collect(Collectors.toList()))) {
				errors.reject("germplasm.create.name.exceeded.length", new String[] {index.toString(), "synonyms"}, "");
				return true;
			}

			// Validations on attributes
			if (isAnyCustomAttributeFieldInvalid(g, index))
				return true;
			if (areAttributesInvalid(g.getAdditionalInfo())) {
				errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "additionalInfo"}, "");
				return true;
			}

			if (isAnyExternalReferenceInvalid(g, index)) {
				return true;
			}

			return false;
		});

		return this.errors;
	}

	private boolean isAnyExternalReferenceInvalid(final GermplasmImportRequest g, final Integer index) {
		if (g.getExternalReferences() != null) {
			g.getExternalReferences().stream().anyMatch(r -> {
				if (r == null) {
					errors.reject("germplasm.create.reference.null", new String[] {index.toString(), "externalReference"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > 2000) {
					errors.reject("germplasm.create.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > 255) {
					errors.reject("germplasm.create.reference.source.exceeded.length", new String[] {index.toString(), "referenceSource"},
						"");
					return true;
				}
				return false;
			});
		}
		return false;
	}

	private boolean isAnyCustomNameFieldInvalid(final GermplasmImportRequest g, final Integer index) {
		if (nameExceedsLength(g.getDefaultDisplayName())) {
			errors.reject("germplasm.create.name.exceeded.length", new String[] {index.toString(), "defaultDisplayName"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getAccessionNumber()) && nameExceedsLength(g.getAccessionNumber())) {
			errors.reject("germplasm.create.name.exceeded.length", new String[] {index.toString(), "accessionNumber"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getGenus()) && nameExceedsLength(g.getGenus())) {
			errors.reject("germplasm.create.name.exceeded.length", new String[] {index.toString(), "genus"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getPedigree()) && nameExceedsLength(g.getPedigree())) {
			errors.reject("germplasm.create.name.exceeded.length", new String[] {index.toString(), "pedigree"}, "");
			return true;
		}
		return false;
	}

	private boolean isAnyCustomAttributeFieldInvalid(final GermplasmImportRequest g, final Integer index) {
		if (!StringUtils.isEmpty(g.getCommonCropName()) && attributeExceedsLength(g.getCommonCropName())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "commonCropName"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getGermplasmOrigin()) && attributeExceedsLength(g.getGermplasmOrigin())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "germplasmOrigin"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getInstituteCode()) && attributeExceedsLength(g.getInstituteCode())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "instituteCode"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getInstituteName()) && attributeExceedsLength(g.getInstituteName())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "instituteName"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSeedSource()) && attributeExceedsLength(g.getSeedSource())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "seedSource"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSpecies()) && attributeExceedsLength(g.getSpecies())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "species"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSpeciesAuthority()) && attributeExceedsLength(g.getSpeciesAuthority())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "speciesAuthority"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSubtaxa()) && attributeExceedsLength(g.getSubtaxa())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "subtaxa"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSubtaxaAuthority()) && attributeExceedsLength(g.getSubtaxaAuthority())) {
			errors.reject("germplasm.create.attribute.exceeded.length", new String[] {index.toString(), "subtaxaAuthority"}, "");
			return true;
		}
		return false;
	}

	private List<String> getValidBreedingMethodDbIds(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		final List<Integer> breedingMethodIds =
			germplasmImportRequestDtoList.stream().filter(g -> StringUtils.isNotEmpty(g.getBreedingMethodDbId()))
				.map(g -> Integer.parseInt(g.getBreedingMethodDbId())).collect(Collectors.toList());
		final BreedingMethodSearchRequest searchRequest =
			new BreedingMethodSearchRequest(null, null, false);
		searchRequest.setMethodIds(breedingMethodIds);
		return
			this.breedingMethodService.getBreedingMethods(searchRequest, null).stream().map(m -> m.getMid().toString())
				.collect(Collectors.toList());

	}

	private List<String> getValidLocationAbbreviations(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		final Set<String> locationAbbrs =
			germplasmImportRequestDtoList.stream().filter(g -> StringUtils.isNotEmpty(g.getCountryOfOriginCode()))
				.map(g -> g.getCountryOfOriginCode().toUpperCase()).collect(Collectors.toSet());

		return
			this.locationService
				.getFilteredLocations(new LocationSearchRequest(null, null, null, new ArrayList<>(locationAbbrs), null, false),
					null)
				.stream().map(
				Location::getLabbr).collect(
				Collectors.toList());

	}

	private boolean areNameValuesInvalid(final Collection<String> values) {
		return values.stream().anyMatch(n -> {
			if (StringUtils.isEmpty(n)) {
				return true;
			}
			if (nameExceedsLength(n)) {
				return true;
			}
			return false;
		});
	}

	private boolean areAttributesInvalid(final Map<String, String> attributes) {
		if (attributes != null) {
			final Set<String> attributeKeys = new HashSet<>();

			if (attributes.keySet().stream().anyMatch(Objects::isNull)) {
				return true;
			}
			attributes.keySet().forEach(attr -> attributeKeys.add(attr.toUpperCase()));
			if (attributes.keySet().size() != attributeKeys.size()) {
				return true;
			}
			if (attributes.values().stream().anyMatch(n -> {
				if (StringUtils.isEmpty(n)) {
					return true;
				}
				if (attributeExceedsLength(n)) {
					return true;
				}
				return false;
			})) {
				return true;
			}
			return false;
		}
		return false;
	}

	private boolean attributeExceedsLength(final String attribute) {
		return attribute.length() > ATTRIBUTE_MAX_LENGTH;
	}

	private boolean nameExceedsLength(final String name) {
		return name.length() > NAME_MAX_LENGTH;
	}

}
