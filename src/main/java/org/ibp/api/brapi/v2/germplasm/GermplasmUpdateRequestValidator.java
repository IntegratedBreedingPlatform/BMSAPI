package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class GermplasmUpdateRequestValidator {

	private BindingResult errors;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private LocationService locationService;

	public void validate(final GermplasmUpdateRequest germplasmUpdateRequest) {
		BaseValidator.checkNotNull(germplasmUpdateRequest, "germplasm.update.request.null");
		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmUpdateRequest.class.getName());

		final Set<String> nameKeys = new HashSet<>();

		if (!StringUtils.isEmpty(germplasmUpdateRequest.getAcquisitionDate())
			&& Util.tryParseDate(germplasmUpdateRequest.getAcquisitionDate(), Util.FRONTEND_DATE_FORMAT) == null) {
			this.errors.reject("germplasm.update.acquisition.date.invalid.format");
		}
		if (!StringUtils.isEmpty(germplasmUpdateRequest.getBreedingMethodDbId()) && !this.isValidBreedingMethodId(germplasmUpdateRequest)) {
			this.errors.reject("germplasm.update.breeding.method.invalid");
		}
		if (!StringUtils.isEmpty(germplasmUpdateRequest.getCountryOfOriginCode()) && !this.isValidLocationAbbreviation(germplasmUpdateRequest)) {
			this.errors.reject("germplasm.update.country.origin.invalid");
		}

		// Validations on names and synonyms
		this.validateCustomNameFields(germplasmUpdateRequest);
		if (germplasmUpdateRequest.getSynonyms().stream().map(Synonym::getType).anyMatch(String::isEmpty)) {
			this.errors.reject("germplasm.update.null.name.types");
		}
		if (germplasmUpdateRequest.getSynonyms().stream().map(Synonym::getSynonym).anyMatch(String::isEmpty)) {
			this.errors.reject("germplasm.update.null.synonym");
		}
		germplasmUpdateRequest.getSynonyms().stream().map(Synonym::getType).forEach(name -> nameKeys.add(name.toUpperCase()));
		if (germplasmUpdateRequest.getSynonyms().size() != nameKeys.size()) {
			this.errors.reject("germplasm.update.duplicated.name.types");
		}
		if (this.areNameValuesInvalid(germplasmUpdateRequest.getSynonyms().stream().map(Synonym::getSynonym).collect(Collectors.toList()))) {
			this.errors.reject("germplasm.update.name.exceeded.length", new String[] {"synonyms"}, "");
		}

		// Validations on attributes
		this.validateCustomAttributeFields(germplasmUpdateRequest);
		if (this.areAttributesInvalid(germplasmUpdateRequest.getAdditionalInfo())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"additionalInfo"}, "");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateCustomNameFields(final GermplasmUpdateRequest g) {
		if (!StringUtils.isEmpty(g.getDefaultDisplayName()) && this.nameExceedsLength(g.getDefaultDisplayName())) {
			this.errors.reject("germplasm.update.name.exceeded.length", new String[] {"defaultDisplayName"}, "");
		}
		if (!StringUtils.isEmpty(g.getAccessionNumber()) && this.nameExceedsLength(g.getAccessionNumber())) {
			this.errors.reject("germplasm.update.name.exceeded.length", new String[] {"accessionNumber"}, "");
		}
		if (!StringUtils.isEmpty(g.getGenus()) && this.nameExceedsLength(g.getGenus())) {
			this.errors.reject("germplasm.update.name.exceeded.length", new String[] {"genus"}, "");
		}
		if (!StringUtils.isEmpty(g.getPedigree()) && this.nameExceedsLength(g.getPedigree())) {
			this.errors.reject("germplasm.update.name.exceeded.length", new String[] {"pedigree"}, "");
		}
	}

	private void validateCustomAttributeFields(final GermplasmUpdateRequest g) {
		if (!StringUtils.isEmpty(g.getCommonCropName()) && this.attributeExceedsLength(g.getCommonCropName())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"commonCropName"}, "");
		}
		if (!StringUtils.isEmpty(g.getGermplasmOrigin()) && this.attributeExceedsLength(g.getGermplasmOrigin())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"germplasmOrigin"}, "");
		}
		if (!StringUtils.isEmpty(g.getInstituteCode()) && this.attributeExceedsLength(g.getInstituteCode())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"instituteCode"}, "");
		}
		if (!StringUtils.isEmpty(g.getInstituteName()) && this.attributeExceedsLength(g.getInstituteName())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"instituteName"}, "");
		}
		if (!StringUtils.isEmpty(g.getSeedSource()) && this.attributeExceedsLength(g.getSeedSource())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"seedSource"}, "");
		}
		if (!StringUtils.isEmpty(g.getSpecies()) && this.attributeExceedsLength(g.getSpecies())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"species"}, "");
		}
		if (!StringUtils.isEmpty(g.getSpeciesAuthority()) && this.attributeExceedsLength(g.getSpeciesAuthority())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"speciesAuthority"}, "");
		}
		if (!StringUtils.isEmpty(g.getSubtaxa()) && this.attributeExceedsLength(g.getSubtaxa())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"subtaxa"}, "");
		}
		if (!StringUtils.isEmpty(g.getSubtaxaAuthority()) && this.attributeExceedsLength(g.getSubtaxaAuthority())) {
			this.errors.reject("germplasm.update.attribute.exceeded.length", new String[] {"subtaxaAuthority"}, "");
		}
	}

	private boolean isValidBreedingMethodId(final GermplasmUpdateRequest germplasmUpdateRequest) {
		if (NumberUtils.isDigits(germplasmUpdateRequest.getBreedingMethodDbId())) {
			return this.breedingMethodService.getBreedingMethod(Integer.parseInt(germplasmUpdateRequest.getBreedingMethodDbId())).isPresent();
		}
		return false;
	}

	private boolean isValidLocationAbbreviation(final GermplasmUpdateRequest germplasmUpdateRequest) {
		return
			this.locationService
				.countFilteredLocations(
					new LocationSearchRequest(null, null, null, Collections.singletonList(germplasmUpdateRequest.getCountryOfOriginCode()),
						null,
						false)) > 0;

	}


	protected boolean areNameValuesInvalid(final Collection<String> values) {
		return values.stream().anyMatch(n -> {
			return this.nameExceedsLength(n);
		});
	}

	protected boolean areAttributesInvalid(final Map<String, String> attributes) {
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
				if (this.attributeExceedsLength(n)) {
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

	protected boolean attributeExceedsLength(final String attribute) {
		return attribute.length() > GermplasmImportRequestValidator.ATTRIBUTE_MAX_LENGTH;
	}

	protected boolean nameExceedsLength(final String name) {
		return name.length() > GermplasmImportRequestValidator.NAME_MAX_LENGTH;
	}

}
