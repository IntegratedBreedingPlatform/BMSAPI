package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.importation.ExtendedGermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GermplasmImportRequestDtoValidator {

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));
	private static Integer STOCK_ID_MAX_LENGTH = 35;

	private BindingResult errors;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmServiceMw;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private LotService lotService;

	public void validateBeforeSaving(final String programUUID, final List<GermplasmImportRequestDto> germplasmImportRequestDto) {
		errors = new MapBindingResult(new HashMap<String, String>(), GermplasmImportRequestDto.class.getName());

		BaseValidator.checkNotEmpty(germplasmImportRequestDto, "germplasm.import.list.null");

		final Set<Integer> clientIds = new HashSet<>();
		boolean invalid = germplasmImportRequestDto.stream().anyMatch(g -> {

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

			if (!DateUtil.isValidDate(g.getCreationDate())) {
				errors.reject("germplasm.import.creation.date.invalid", "");
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

			if (!StringUtil.isEmpty(g.getGermplasmUUID()) && g.getGermplasmUUID().length() > 36) {
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

		this.validateGUIDNotExists(germplasmImportRequestDto);
		this.validateNotDuplicatedGUID(germplasmImportRequestDto);
		this.validateAllBreedingMethodAbbreviationsExists(programUUID, germplasmImportRequestDto);
		this.validateAllLocationAbbreviationsExists(programUUID, germplasmImportRequestDto);
		this.validateAllNameTypesExists(germplasmImportRequestDto);
		this.validateAllAttributesExists(germplasmImportRequestDto);

	}

	public void validateImportLoadedData(final String programUUID,
		final List<ExtendedGermplasmImportRequestDto> germplasmImportRequestDto) {
		errors = new MapBindingResult(new HashMap<String, String>(), ExtendedGermplasmImportRequestDto.class.getName());

		BaseValidator.checkNotEmpty(germplasmImportRequestDto, "germplasm.import.list.null");

		boolean invalid = germplasmImportRequestDto.stream().anyMatch(g -> {

			final Set<String> nameKeys = new HashSet<>();
			final Set<String> attributeKeys = new HashSet<>();

			if (g == null) {
				errors.reject("germplasm.import.germplasm.null", "");
				return true;
			}

			if (!StringUtils.isEmpty(g.getReference()) && g.getReference().length() > 255) {
				errors.reject("germplasm.import.reference.length.error", "");
				return true;
			}

			if (StringUtils.isNotEmpty(g.getCreationDate()) && !DateUtil.isValidDate(g.getCreationDate())) {
				errors.reject("germplasm.import.creation.date.invalid", "");
				return true;
			}

			if (!StringUtil.isEmpty(g.getGermplasmUUID()) && g.getGermplasmUUID().length() > 36) {
				errors.reject("germplasm.import.guid.invalid.length", "");
				return true;
			}

			if (g.getNames() != null) {
				g.getNames().keySet().forEach(name -> nameKeys.add(name.toUpperCase()));
				if (g.getNames().keySet().size() != nameKeys.size()) {
					errors.reject("germplasm.import.duplicated.name.types", new String[] {g.getClientId().toString()}, "");
					return true;
				}

				if (StringUtils.isNotEmpty(g.getPreferredName()) && !nameKeys.contains(g.getPreferredName().toUpperCase())) {
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

			}

			if (g.getAttributes() != null) {
				g.getAttributes().keySet().forEach(attr -> attributeKeys.add(attr.toUpperCase()));
				if (g.getAttributes().keySet().size() != attributeKeys.size()) {
					errors.reject("germplasm.import.duplicated.attributes", new String[] {g.getClientId().toString()}, "");
					return true;
				}

				if (g.getAttributes().values().stream().anyMatch(n -> {
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
			}

			if (g.getAmount() != null && g.getAmount() <= 0) {
				errors.reject("germplasm.import.inventory.amount.invalid", "");
				return true;
			}

			return false;

		});

		if (invalid) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.validateNotDuplicatedGUID(germplasmImportRequestDto);
		this.validateAllBreedingMethodAbbreviationsExists(programUUID, germplasmImportRequestDto);
		this.validateAllLocationAbbreviationsExists(programUUID, germplasmImportRequestDto);
		this.validateAllStorageLocationAbbreviationsExists(programUUID, germplasmImportRequestDto);
		this.validateAllNameTypesExists(germplasmImportRequestDto);
		this.validateAllAttributesExists(germplasmImportRequestDto);
		this.validateStockIds(germplasmImportRequestDto);
		this.validateUnits(germplasmImportRequestDto);
	}

	private void validateAllNameTypesExists(final List<? extends GermplasmImportRequestDto> germplasmDtos) {
		final Set<String> nameTypes = new HashSet<>();
		germplasmDtos.forEach(g -> {
			if (g.getNames() != null && !g.getNames().isEmpty())
				nameTypes.addAll(g.getNames().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList()));
		});
		if (!nameTypes.isEmpty()) {
			final List<String> existingGermplasmNameTypes =
				this.germplasmService.filterGermplasmNameTypes(nameTypes).stream().map(GermplasmNameTypeDTO::getCode).collect(
					Collectors.toList());
			if (existingGermplasmNameTypes.size() != nameTypes.size()) {
				nameTypes.removeAll(existingGermplasmNameTypes);
				errors.reject("germplasm.import.name.types.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(nameTypes), 3)}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

	private void validateAllBreedingMethodAbbreviationsExists(final String programUUID,
		final List<? extends GermplasmImportRequestDto> germplasmDtos) {
		final Set<String> breedingMethodsAbbrs =
			germplasmDtos.stream().filter(Objects::nonNull).map(g -> g.getBreedingMethodAbbr().toUpperCase()).collect(
				Collectors.toSet());
		if (!breedingMethodsAbbrs.isEmpty()) {
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

	}

	private void validateAllLocationAbbreviationsExists(final String programUUID,
		final List<? extends GermplasmImportRequestDto> germplasmDtos) {
		final Set<String> locationAbbrs =
			germplasmDtos.stream().filter(Objects::nonNull).map(g -> g.getLocationAbbr().toUpperCase()).collect(Collectors.toSet());
		if (!locationAbbrs.isEmpty()) {
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
	}

	private void validateAllStorageLocationAbbreviationsExists(final String programUUID,
		final List<ExtendedGermplasmImportRequestDto> germplasmDtos) {
		final List<String> locationAbbreviations =
			germplasmDtos.stream().filter(g -> StringUtils.isNotEmpty(g.getStorageLocationAbbr()))
				.map(ExtendedGermplasmImportRequestDto::getStorageLocationAbbr).distinct().collect(Collectors.toList());
		if (!locationAbbreviations.isEmpty()) {
			final List<Location> existingLocations =
				locationDataManager.getFilteredLocations(programUUID, STORAGE_LOCATION_TYPE, null, locationAbbreviations, false);
			if (existingLocations.size() != locationAbbreviations.size()) {
				final List<String> existingAbbreviations = existingLocations.stream().map(Location::getLabbr).collect(Collectors.toList());
				final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
				invalidAbbreviations.removeAll(existingAbbreviations);
				errors
					.reject("germplasm.import.storage.location.abbreviations.not.exist",
						new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateAllAttributesExists(final List<? extends GermplasmImportRequestDto> germplasmDtos) {
		final Set<String> attributes = new HashSet<>();
		germplasmDtos.stream().filter(germ -> germ.getAttributes() != null).collect(Collectors.toList())
			.forEach(g -> attributes.addAll(g.getAttributes().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList())));
		if (!attributes.isEmpty()) {
			final List<String> existingGermplasmAttributes =
				this.germplasmService.filterGermplasmAttributes(attributes).stream().map(AttributeDTO::getCode).collect(
					Collectors.toList());
			final Set<String> repeatedAttributes =
				existingGermplasmAttributes.stream().filter(i -> Collections.frequency(existingGermplasmAttributes, i) > 1)
					.collect(Collectors.toSet());
			if (!repeatedAttributes.isEmpty()) {
				errors.reject("germplasm.import.attributes.duplicated.found",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(repeatedAttributes), 3)}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			if (existingGermplasmAttributes.size() != attributes.size()) {
				attributes.removeAll(existingGermplasmAttributes);
				errors.reject("germplasm.import.attributes.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(attributes), 3)}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

	private void validateNotDuplicatedGUID(final List<? extends GermplasmImportRequestDto> germplasmDtos) {
		final List<String> guidsList =
			germplasmDtos.stream().filter(g -> !StringUtils.isEmpty(g.getGermplasmUUID())).map(GermplasmImportRequestDto::getGermplasmUUID)
				.collect(
					Collectors.toList());
		if (!guidsList.stream().filter(i -> Collections.frequency(guidsList, i) > 1)
			.collect(Collectors.toSet()).isEmpty()) {
			errors.reject("germplasm.import.duplicated.guids", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateGUIDNotExists(final List<? extends GermplasmImportRequestDto> germplasmDtos) {
		final List<String> guidsList =
			germplasmDtos.stream().filter(g -> !StringUtils.isEmpty(g.getGermplasmUUID())).map(GermplasmImportRequestDto::getGermplasmUUID)
				.collect(
					Collectors.toList());
		final List<Germplasm> germplasmDTOS = this.germplasmServiceMw.getGermplasmByGUIDs(guidsList);
		if (!germplasmDTOS.isEmpty()) {
			errors.reject("germplasm.import.existent.guids",
				new String[] {
					Util.buildErrorMessageFromList(germplasmDTOS.stream().map(Germplasm::getGermplasmUUID).collect(Collectors.toList()),
						3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateStockIds(final List<ExtendedGermplasmImportRequestDto> germplasmDtos) {
		final List<String> stockIds = germplasmDtos.stream().filter(g -> StringUtils.isNotEmpty(g.getStockId())).map(g -> g.getStockId())
			.collect(Collectors.toList());

		if (!stockIds.isEmpty()) {
			final List<String> uniqueNotNullStockIds = stockIds.stream().distinct().collect(Collectors.toList());
			if (stockIds.size() != uniqueNotNullStockIds.size()) {
				errors.reject("lot.input.list.stock.ids.duplicated", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			if (uniqueNotNullStockIds.stream().filter(c -> c.length() > STOCK_ID_MAX_LENGTH).count() > 0) {
				errors.reject("lot.stock.id.length.higher.than.maximum", new String[] {String.valueOf(STOCK_ID_MAX_LENGTH)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			final List<LotDto> existingLotDtos = this.lotService.getLotsByStockIds(uniqueNotNullStockIds);
			if (!existingLotDtos.isEmpty()) {
				final List<String> existingStockIds = existingLotDtos.stream().map(LotDto::getStockId).collect(Collectors.toList());
				errors.reject("lot.input.list.stock.ids.invalid", new String[] {Util.buildErrorMessageFromList(existingStockIds, 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateUnits(final List<ExtendedGermplasmImportRequestDto> germplasmDtos) {
		final List<String> units = germplasmDtos.stream().filter(g -> StringUtils.isNotEmpty(g.getUnit())).map(g -> g.getUnit()).distinct()
			.collect(Collectors.toList());
		if (!units.isEmpty()) {
			this.inventoryCommonValidator.validateUnitNames(new ArrayList<>(units), errors);
		}
	}

}
