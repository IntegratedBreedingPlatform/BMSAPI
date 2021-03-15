package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
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
public class GermplasmUpdateRequestValidator extends GermplasmImportRequestValidator {

	public static final Integer NAME_MAX_LENGTH = 255;
	public static final Integer ATTRIBUTE_MAX_LENGTH = 255;

	private BindingResult errors;

	public void validate(final GermplasmUpdateRequest germplasmUpdateRequest) {
		BaseValidator.checkNotNull(germplasmUpdateRequest, "germplasm.update.request.null");
		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmUpdateRequest.class.getName());

		final List<String> validBreedingMethodIds = this.getValidBreedingMethodDbIds(Collections.singletonList(germplasmUpdateRequest));
		final List<String> validLocationAbbreviations = this.getValidLocationAbbreviations(Collections.singletonList(germplasmUpdateRequest));


		final Set<String> nameKeys = new HashSet<>();

		if (!StringUtils.isEmpty(germplasmUpdateRequest.getAcquisitionDate())
			&& Util.tryParseDate(germplasmUpdateRequest.getAcquisitionDate(), Util.FRONTEND_DATE_FORMAT) == null) {
			this.errors.reject("germplasm.update.acquisition.date.invalid.format");
		}
		if (!StringUtils.isEmpty(germplasmUpdateRequest.getBreedingMethodDbId()) && !validBreedingMethodIds
			.contains(germplasmUpdateRequest.getBreedingMethodDbId())) {
			this.errors.reject("germplasm.update.breeding.method.invalid");
		}
		if (!StringUtils.isEmpty(germplasmUpdateRequest.getCountryOfOriginCode()) && !validLocationAbbreviations
			.contains(germplasmUpdateRequest.getCountryOfOriginCode())) {
			this.errors.reject("germplasm.update.country.origin.invalid");
		}

		// Validations on names and synonyms
		this.validateCustomNameFields(germplasmUpdateRequest);
		if (germplasmUpdateRequest.getSynonyms().stream().map(Synonym::getType).anyMatch(Objects::isNull)) {
			this.errors.reject("germplasm.update.null.name.types");
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

	private void validateCustomNameFields(final GermplasmImportRequest g) {
		if (this.nameExceedsLength(g.getDefaultDisplayName())) {
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

}
