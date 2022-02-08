package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
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
	public static final String GERMPLASM_CREATE_NAME_EXCEEDED_LENGTH = "germplasm.create.name.exceeded.length";
	public static final String GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH = "germplasm.create.attribute.exceeded.length";

	protected BindingResult errors;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private GermplasmNameService germplasmNameService;

	public BindingResult pruneGermplasmInvalidForImport(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		BaseValidator.checkNotEmpty(germplasmImportRequestDtoList, "germplasm.import.list.null");
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmImportRequest.class.getName());

		if (!germplasmImportRequestDtoList.stream().filter(i -> Collections.frequency(germplasmImportRequestDtoList, i) > 1)
			.collect(Collectors.toSet()).isEmpty()) {
			this.errors.reject("germplasm.import.duplicated.objects"
				+ "", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Set<String> duplicatedPUIs = this.getDuplicatedGermplasmPUIs(germplasmImportRequestDtoList);
		final List<String> existingPUIs = this.getExistingGermplasmPUIs(germplasmImportRequestDtoList);
		final List<String> validBreedingMethodIds = this.getValidBreedingMethodDbIds(germplasmImportRequestDtoList);
		final List<String> validLocationAbbreviations = this.getValidLocationAbbreviations(germplasmImportRequestDtoList);

		final Map<GermplasmImportRequest, Integer> importRequestByIndexMap = IntStream.range(0, germplasmImportRequestDtoList.size())
			.boxed()
			.collect(Collectors.toMap(germplasmImportRequestDtoList::get, i -> i));
		germplasmImportRequestDtoList.removeIf( g -> {
			final Set<String> nameKeys = new HashSet<>();

			final Integer index = importRequestByIndexMap.get(g) + 1;

			if (g.isGermplasmPUIInList(duplicatedPUIs)) {
				this.errors.reject("germplasm.create.duplicated.pui", new String[] {index.toString()}, "");
				return true;
			}
			if (g.isGermplasmPUIInList(existingPUIs)) {
				this.errors.reject("germplasm.create.existing.pui", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isEmpty(g.getDefaultDisplayName())) {
				this.errors.reject("germplasm.create.null.name.types", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isEmpty(g.getAcquisitionDate())) {
				this.errors.reject("germplasm.create.acquisition.date.null", new String[] {index.toString()}, "");
				return true;
			}
			if (Util.tryParseDate(g.getAcquisitionDate(), Util.FRONTEND_DATE_FORMAT) == null) {
				this.errors.reject("germplasm.create.acquisition.date.invalid.format", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isNotEmpty(g.getBreedingMethodDbId()) && !validBreedingMethodIds.contains(g.getBreedingMethodDbId())) {
				this.errors.reject("germplasm.create.breeding.method.invalid", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isEmpty(g.getCountryOfOriginCode())) {
				this.errors.reject("germplasm.create.country.origin.null", new String[] {index.toString()}, "");
				return true;
			}

			if (!validLocationAbbreviations.contains(g.getCountryOfOriginCode())) {
				this.errors.reject("germplasm.create.country.origin.invalid", new String[] {index.toString()}, "");
				return true;
			}

			// Validations on names and synonyms
			if (this.isAnyCustomNameFieldInvalid(g, index))
				return true;
			if (g.getSynonyms().stream().map(Synonym::getType).anyMatch(Objects::isNull)) {
				this.errors.reject("germplasm.create.null.name.types", new String[] {index.toString()}, "");
				return true;
			}
			g.getSynonyms().stream().map(Synonym::getType).forEach(name -> nameKeys.add(name.toUpperCase()));
			if (g.getSynonyms().size() != nameKeys.size()) {
				this.errors.reject("germplasm.create.duplicated.name.types", new String[] {index.toString()}, "");
				return true;
			}
			if (this.areNameValuesInvalid(g.getSynonyms().stream().map(Synonym::getSynonym).collect(Collectors.toList()))) {
				this.errors.reject(GERMPLASM_CREATE_NAME_EXCEEDED_LENGTH, new String[] {index.toString(), "synonyms"}, "");
				return true;
			}

			// Validations on attributes
			if (this.isAnyCustomAttributeFieldInvalid(g, index))
				return true;
			if (this.areAttributesInvalid(g.getAdditionalInfo())) {
				this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "additionalInfo"}, "");
				return true;
			}

			return this.isAnyExternalReferenceInvalid(g, index);
		});

		return this.errors;
	}

	private boolean isAnyExternalReferenceInvalid(final GermplasmImportRequest g, final Integer index) {
		if (g.getExternalReferences() != null) {
			return g.getExternalReferences().stream().anyMatch(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					this.errors.reject("germplasm.create.reference.null", new String[] {index.toString(), "externalReference"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > 2000) {
					this.errors.reject("germplasm.create.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > 255) {
					this.errors.reject("germplasm.create.reference.source.exceeded.length", new String[] {index.toString(), "referenceSource"},
						"");
					return true;
				}
				return false;
			});
		}
		return false;
	}

	private boolean isAnyCustomNameFieldInvalid(final GermplasmImportRequest g, final Integer index) {
		if (this.nameExceedsLength(g.getDefaultDisplayName())) {
			this.errors.reject(GERMPLASM_CREATE_NAME_EXCEEDED_LENGTH, new String[] {index.toString(), "defaultDisplayName"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getAccessionNumber()) && this.nameExceedsLength(g.getAccessionNumber())) {
			this.errors.reject(GERMPLASM_CREATE_NAME_EXCEEDED_LENGTH, new String[] {index.toString(), "accessionNumber"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getGenus()) && this.nameExceedsLength(g.getGenus())) {
			this.errors.reject(GERMPLASM_CREATE_NAME_EXCEEDED_LENGTH, new String[] {index.toString(), "genus"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getPedigree()) && this.nameExceedsLength(g.getPedigree())) {
			this.errors.reject(GERMPLASM_CREATE_NAME_EXCEEDED_LENGTH, new String[] {index.toString(), "pedigree"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getGermplasmPUI()) && this.nameExceedsLength(g.getGermplasmPUI())) {
			this.errors.reject(GERMPLASM_CREATE_NAME_EXCEEDED_LENGTH, new String[] {index.toString(), "germplasmPUI"}, "");
			return true;
		}
		return false;
	}

	protected boolean isAnyCustomAttributeFieldInvalid(final GermplasmImportRequest g, final Integer index) {
		if (!StringUtils.isEmpty(g.getCommonCropName()) && this.attributeExceedsLength(g.getCommonCropName())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "commonCropName"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getGermplasmOrigin()) && this.attributeExceedsLength(g.getGermplasmOrigin())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "germplasmOrigin"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getInstituteCode()) && this.attributeExceedsLength(g.getInstituteCode())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "instituteCode"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getInstituteName()) && this.attributeExceedsLength(g.getInstituteName())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "instituteName"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSeedSource()) && this.attributeExceedsLength(g.getSeedSource())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "seedSource"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSpecies()) && this.attributeExceedsLength(g.getSpecies())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "species"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSpeciesAuthority()) && this.attributeExceedsLength(g.getSpeciesAuthority())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "speciesAuthority"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSubtaxa()) && this.attributeExceedsLength(g.getSubtaxa())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "subtaxa"}, "");
			return true;
		}
		if (!StringUtils.isEmpty(g.getSubtaxaAuthority()) && this.attributeExceedsLength(g.getSubtaxaAuthority())) {
			this.errors.reject(GERMPLASM_CREATE_ATTRIBUTE_EXCEEDED_LENGTH, new String[] {index.toString(), "subtaxaAuthority"}, "");
			return true;
		}
		return false;
	}

	protected List<String> getValidBreedingMethodDbIds(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
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

	protected List<String> getExistingGermplasmPUIs(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		final List<String> puisList = this.collectGermplasmPUIs(germplasmImportRequestDtoList);
		return this.germplasmNameService.getExistingGermplasmPUIs(puisList);
	}

	private List<String> collectGermplasmPUIs(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		return germplasmImportRequestDtoList.stream().map(GermplasmImportRequest::collectGermplasmPUIs).flatMap(List::stream).collect(Collectors.toList());
	}

	protected Set<String> getDuplicatedGermplasmPUIs(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		final List<String> puisList = this.collectGermplasmPUIs(germplasmImportRequestDtoList);
		return puisList.stream().filter(i -> Collections.frequency(puisList, i) > 1).collect(Collectors.toSet());
	}

	protected List<String> getValidLocationAbbreviations(final List<GermplasmImportRequest> germplasmImportRequestDtoList) {
		final Set<String> locationAbbrs =
			germplasmImportRequestDtoList.stream().filter(g -> StringUtils.isNotEmpty(g.getCountryOfOriginCode()))
				.map(g -> g.getCountryOfOriginCode().toUpperCase()).collect(Collectors.toSet());

		return
			this.locationService
				.searchLocations(new LocationSearchRequest(null, null, null, new ArrayList<>(locationAbbrs), null),
					null, null)
				.stream().map(
				LocationDTO::getAbbreviation).collect(
				Collectors.toList());

	}

	protected boolean areNameValuesInvalid(final Collection<String> values) {
		return values.stream().anyMatch(n -> {
			if (StringUtils.isEmpty(n)) {
				return true;
			}
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
			return attributes.values().stream().anyMatch(n -> StringUtils.isNotEmpty(n) && this.attributeExceedsLength(n));
		}
		return false;
	}

	protected boolean attributeExceedsLength(final String attribute) {
		return attribute.length() > ATTRIBUTE_MAX_LENGTH;
	}

	protected boolean nameExceedsLength(final String name) {
		return name.length() > NAME_MAX_LENGTH;
	}

}
