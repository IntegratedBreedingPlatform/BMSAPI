package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmInventoryImportDTO;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GermplasmImportRequestDtoValidator {

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));
	static final Integer STOCK_ID_MAX_LENGTH = 35;
	static final Integer GUID_MAX_LENGTH = 36;
	static final Integer REFERENCE_MAX_LENGTH = 255;
	static final Integer NAME_MAX_LENGTH = 255;
	static final Integer ATTRIBUTE_MAX_LENGTH = 255;

	private BindingResult errors;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmServiceMw;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private LotService lotService;

	public void validateBeforeSaving(final String programUUID, final GermplasmImportRequestDto germplasmImportRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmImportRequestDto.class.getName());

		BaseValidator.checkNotNull(germplasmImportRequestDto, "germplasm.import.request.null");
		BaseValidator.checkNotEmpty(germplasmImportRequestDto.getGermplasmList(), "germplasm.import.list.null");

		if (germplasmImportRequestDto.getConnectUsing() == null) {
			this.errors.reject("germplasm.import.connect.using.null"
				+ "", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Set<Integer> clientIds = new HashSet<>();
		final List<GermplasmImportDTO> germplasmImportDTOList = germplasmImportRequestDto.getGermplasmList();

		final boolean invalid = germplasmImportDTOList.stream().anyMatch(g -> {

			final Set<String> nameKeys = new HashSet<>();

			if (g == null) {
				this.errors.reject("germplasm.import.germplasm.null", "");
				return true;
			}

			if (g.getNames() == null || g.getNames().isEmpty()) {
				this.errors.reject("germplasm.import.names.null.or.empty", "");
				return true;
			}

			if (StringUtils.isEmpty(g.getPreferredName())) {
				this.errors.reject("germplasm.import.preferred.name.null", "");
				return true;
			}

			if (!StringUtils.isEmpty(g.getReference()) && g.getReference().length() > REFERENCE_MAX_LENGTH) {
				this.errors.reject("germplasm.import.reference.length.error", "");
				return true;
			}

			if (g.getCreationDate() == null) {
				this.errors.reject("germplasm.import.creation.date.null", "");
				return true;
			}

			if (!DateUtil.isValidDate(g.getCreationDate())) {
				this.errors.reject("germplasm.import.creation.date.invalid", "");
				return true;
			}

			if (g.getClientId() == null) {
				this.errors.reject("germplasm.import.client.id.null", "");
				return true;
			}

			if (clientIds.contains(g.getClientId())) {
				this.errors.reject("germplasm.import.client.id.duplicated", "");
				return true;
			}

			clientIds.add(g.getClientId());

			if (StringUtils.isEmpty(g.getBreedingMethodAbbr())) {
				this.errors.reject("germplasm.import.breeding.method.mandatory", "");
				return true;
			}

			if (StringUtils.isEmpty(g.getLocationAbbr())) {
				this.errors.reject("germplasm.import.location.mandatory", "");
				return true;
			}

			if (!StringUtil.isEmpty(g.getGermplasmUUID()) && g.getGermplasmUUID().length() > GUID_MAX_LENGTH) {
				this.errors.reject("germplasm.import.guid.invalid.length", "");
				return true;
			}

			if (!StringUtil.isEmpty(g.getGermplasmUUID()) && g.getGermplasmUUID().equals("0")) {
				this.errors.reject("germplasm.import.guid.invalid.zero", "");
				return true;
			}

			if (g.getNames().keySet().stream().anyMatch(Objects::isNull)) {
				this.errors.reject("germplasm.import.null.name.types", new String[] {g.getClientId().toString()}, "");
				return true;
			}

			g.getNames().keySet().forEach(name -> nameKeys.add(name.toUpperCase()));
			if (g.getNames().keySet().size() != nameKeys.size()) {
				this.errors.reject("germplasm.import.duplicated.name.types", new String[] {g.getClientId().toString()}, "");
				return true;
			}

			if (!nameKeys.contains(g.getPreferredName().toUpperCase())) {
				this.errors.reject("germplasm.import.preferred.name.invalid", "");
				return true;
			}

			if (this.areNameValuesInvalid(g.getNames().values())) {
				return true;
			}

			if (this.areAttributesInvalid(g.getClientId(), g.getAttributes())) {
				return true;
			}

			if (germplasmImportRequestDto.getConnectUsing() != GermplasmImportRequestDto.PedigreeConnectionType.NONE) {
				if ((StringUtils.isNotEmpty(g.getProgenitor1()) && StringUtils.isEmpty(g.getProgenitor2())) || (
					StringUtils.isNotEmpty(g.getProgenitor2()) && StringUtils.isEmpty(g.getProgenitor1()))) {
					this.errors.reject("germplasm.import.invalid.progenitors.combination", "");
					return true;
				}
			} else {
				if ((StringUtils.isNotEmpty(g.getProgenitor1()) || StringUtils.isNotEmpty(g.getProgenitor2()))) {
					this.errors.reject("germplasm.import.progenitors.must.be.empty", "");
					return true;
				}
			}

			if (germplasmImportRequestDto.getConnectUsing() == GermplasmImportRequestDto.PedigreeConnectionType.GID) {
				if ((StringUtils.isNotEmpty(g.getProgenitor1()) && !StringUtils.isNumeric(g.getProgenitor1())) || (
					StringUtils.isNotEmpty(g.getProgenitor2()) && !StringUtils.isNumeric(g.getProgenitor2()))) {
					this.errors.reject("germplasm.import.progenitor.must.be.numeric.when.connecting.by.gid", "");
					return true;
				}
			}

			return false;

		});

		if (invalid) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!germplasmImportRequestDto.isSkipIfExists()) {
			this.validateGUIDNotExists(germplasmImportDTOList);
		}
		this.validateNotDuplicatedGUID(germplasmImportDTOList);
		this.validateAllBreedingMethodAbbreviationsExistsAndNotAcceptMutations(programUUID, germplasmImportDTOList);
		this.validateAllLocationAbbreviationsExists(programUUID, germplasmImportDTOList);
		this.validateAllNameTypesExists(germplasmImportDTOList);
		this.validateAllAttributesExists(germplasmImportDTOList);

	}

	public void validateImportLoadedData(final String programUUID,
		final List<GermplasmInventoryImportDTO> germplasmInventoryImportDTOList) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmInventoryImportDTO.class.getName());

		BaseValidator.checkNotEmpty(germplasmInventoryImportDTOList, "germplasm.import.list.null");

		final Set<Integer> clientIds = new HashSet<>();
		final boolean invalid = germplasmInventoryImportDTOList.stream().anyMatch(g -> {

			final Set<String> nameKeys = new HashSet<>();

			if (g == null) {
				this.errors.reject("germplasm.import.germplasm.null", "");
				return true;
			}

			if (g.getClientId() != null) {
				if (clientIds.contains(g.getClientId())) {
					this.errors.reject("germplasm.import.client.id.duplicated", "");
					return true;
				}

				clientIds.add(g.getClientId());
			}

			if (!StringUtils.isEmpty(g.getReference()) && g.getReference().length() > REFERENCE_MAX_LENGTH) {
				this.errors.reject("germplasm.import.reference.length.error", "");
				return true;
			}

			if (StringUtils.isNotEmpty(g.getCreationDate()) && !DateUtil.isValidDate(g.getCreationDate())) {
				this.errors.reject("germplasm.import.creation.date.invalid", "");
				return true;
			}

			if (!StringUtil.isEmpty(g.getGermplasmUUID()) && g.getGermplasmUUID().length() > GUID_MAX_LENGTH) {
				this.errors.reject("germplasm.import.guid.invalid.length", "");
				return true;
			}

			if (!StringUtil.isEmpty(g.getGermplasmUUID()) && g.getGermplasmUUID().equals("0")) {
				this.errors.reject("germplasm.import.guid.invalid.zero", "");
				return true;
			}

			if (g.getNames() != null) {
				g.getNames().keySet().forEach(name -> nameKeys.add(name.toUpperCase()));
				if (g.getNames().keySet().size() != nameKeys.size()) {
					this.errors.reject("germplasm.import.duplicated.name.types",
						new String[] {(g.getClientId() != null) ? String.valueOf(g.getClientId()) : "unknown"}, "");
					return true;
				}

				if (this.areNameValuesInvalid(g.getNames().values())) {
					return true;
				}

			}

			if (StringUtils.isNotEmpty(g.getPreferredName()) && (g.getNames() == null || !nameKeys
				.contains(g.getPreferredName().toUpperCase()))) {
				this.errors.reject("germplasm.import.preferred.name.invalid", "");
				return true;
			}

			if (this.areAttributesInvalid(g.getClientId(), g.getAttributes())) {
				return true;
			}

			if ((StringUtils.isNotEmpty(g.getProgenitor1()) && StringUtils.isEmpty(g.getProgenitor2())) || (
				StringUtils.isNotEmpty(g.getProgenitor2()) && StringUtils.isEmpty(g.getProgenitor1()))) {
				this.errors.reject("germplasm.import.invalid.progenitors.combination", "");
				return true;
			}

			if (g.getAmount() != null && g.getAmount() <= 0) {
				this.errors.reject("germplasm.import.inventory.amount.invalid", "");
				return true;
			}

			return false;

		});

		if (invalid) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateNotDuplicatedGUID(germplasmInventoryImportDTOList);
		this.validateAllBreedingMethodAbbreviationsExistsAndNotAcceptMutations(programUUID, germplasmInventoryImportDTOList);
		this.validateAllLocationAbbreviationsExists(programUUID, germplasmInventoryImportDTOList);
		this.validateAllStorageLocationAbbreviationsExists(programUUID, germplasmInventoryImportDTOList);
		this.validateAllNameTypesExists(germplasmInventoryImportDTOList);
		this.validateAllAttributesExists(germplasmInventoryImportDTOList);
		this.validateStockIds(germplasmInventoryImportDTOList);
		this.validateUnits(germplasmInventoryImportDTOList);
	}

	private void validateAllNameTypesExists(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> nameTypes = new HashSet<>();
		germplasmImportDTOList.forEach(g -> {
			if (g.getNames() != null && !g.getNames().isEmpty())
				nameTypes.addAll(g.getNames().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList()));
		});
		if (!nameTypes.isEmpty()) {
			final List<String> existingGermplasmNameTypes =
				this.germplasmService.filterGermplasmNameTypes(nameTypes).stream().map(GermplasmNameTypeDTO::getCode).collect(
					Collectors.toList());
			if (existingGermplasmNameTypes.size() != nameTypes.size()) {
				nameTypes.removeAll(existingGermplasmNameTypes);
			this.errors.reject("germplasm.import.name.types.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(nameTypes), 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateAllBreedingMethodAbbreviationsExistsAndNotAcceptMutations(final String programUUID,
		final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> breedingMethodsAbbrs =
			germplasmImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getBreedingMethodAbbr()))
				.map(g -> g.getBreedingMethodAbbr().toUpperCase()).collect(
				Collectors.toSet());
		if (!breedingMethodsAbbrs.isEmpty()) {
			final BreedingMethodSearchRequest searchRequest =
				new BreedingMethodSearchRequest(programUUID, new ArrayList<>(breedingMethodsAbbrs), false);
			final List<BreedingMethodDTO> existingBreedingMethods =
				this.breedingMethodService.getBreedingMethods(searchRequest, null);
			if (breedingMethodsAbbrs.size() != existingBreedingMethods.size()) {
				final List<String> existingBreedingMethodsCodes =
					existingBreedingMethods.stream().map(
						BreedingMethodDTO::getCode).collect(Collectors.toList());
				breedingMethodsAbbrs.removeAll(existingBreedingMethodsCodes);
				this.errors.reject("germplasm.import.breeding.methods.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(breedingMethodsAbbrs), 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			final Map<String, BreedingMethodDTO> methodsMapByAbbreviation =
				existingBreedingMethods.stream().collect(Collectors.toMap(bm -> bm.getCode().toUpperCase(), bm -> bm));
			if (germplasmImportDTOList.stream().anyMatch(
				g -> StringUtils.isNotEmpty(g.getProgenitor1()) && StringUtils.isNotEmpty(g.getProgenitor2()) && StringUtils
					.isNotEmpty(g.getBreedingMethodAbbr()) && Integer.valueOf(1)
					.equals(methodsMapByAbbreviation
						.get(g.getBreedingMethodAbbr().toUpperCase()).getNumberOfProgenitors()))) {
				this.errors.reject("germplasm.import.mutation.not.supported.when.saving.progenitors", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateAllLocationAbbreviationsExists(final String programUUID,
		final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> locationAbbrs = germplasmImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getLocationAbbr()))
			.map(g -> g.getLocationAbbr().toUpperCase()).collect(Collectors.toSet());
		if (!locationAbbrs.isEmpty()) {
			final List<String> existingLocations =
				this.locationService
					.getFilteredLocations(new LocationSearchRequest(programUUID, null, null, new ArrayList<>(locationAbbrs), null, false),
						null)
					.stream().map(
					Location::getLabbr).collect(
					Collectors.toList());
			if (locationAbbrs.size() != existingLocations.size()) {
				locationAbbrs.removeAll(existingLocations);
			this.errors.reject("germplasm.import.location.abbreviations.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(locationAbbrs), 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateAllStorageLocationAbbreviationsExists(final String programUUID,
		final List<GermplasmInventoryImportDTO> germplasmInventoryImportDTOList) {
		final List<String> locationAbbreviations =
			germplasmInventoryImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getStorageLocationAbbr()))
				.map(g -> g.getStorageLocationAbbr().toUpperCase()).distinct().collect(Collectors.toList());
		if (!locationAbbreviations.isEmpty()) {
			final List<Location> existingLocations =
				this.locationService.getFilteredLocations(
					new LocationSearchRequest(programUUID, STORAGE_LOCATION_TYPE, null, new ArrayList<>(locationAbbreviations), null,
						false), null);
			if (existingLocations.size() != locationAbbreviations.size()) {
				final List<String> existingAbbreviations = existingLocations.stream().map(Location::getLabbr).collect(Collectors.toList());
				final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
				invalidAbbreviations.removeAll(existingAbbreviations);
				this.errors
					.reject("germplasm.import.storage.location.abbreviations.not.exist",
						new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateAllAttributesExists(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> attributes = new HashSet<>();
		germplasmImportDTOList.stream().filter(germ -> germ.getAttributes() != null).collect(Collectors.toList())
			.forEach(g -> attributes.addAll(g.getAttributes().keySet().stream().map(n -> n.toUpperCase()).collect(Collectors.toList())));
		if (!attributes.isEmpty()) {
			final List<String> existingGermplasmAttributes =
				this.germplasmAttributeService.filterGermplasmAttributes(attributes, null).stream().map(AttributeDTO::getCode).collect(
					Collectors.toList());
			final Set<String> repeatedAttributes =
				existingGermplasmAttributes.stream().filter(i -> Collections.frequency(existingGermplasmAttributes, i) > 1)
					.collect(Collectors.toSet());
			if (!repeatedAttributes.isEmpty()) {
				this.errors.reject("germplasm.import.attributes.duplicated.found",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(repeatedAttributes), 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
			if (existingGermplasmAttributes.size() != attributes.size()) {
				attributes.removeAll(existingGermplasmAttributes);
				this.errors.reject("germplasm.import.attributes.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(attributes), 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateNotDuplicatedGUID(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final List<String> guidsList =
			germplasmImportDTOList.stream().filter(g -> !StringUtils.isEmpty(g.getGermplasmUUID()))
				.map(GermplasmImportDTO::getGermplasmUUID)
				.collect(
					Collectors.toList());
		if (!guidsList.stream().filter(i -> Collections.frequency(guidsList, i) > 1)
			.collect(Collectors.toSet()).isEmpty()) {
			this.errors.reject("germplasm.import.duplicated.guids", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateGUIDNotExists(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final List<String> guidsList =
			germplasmImportDTOList.stream().filter(g -> !StringUtils.isEmpty(g.getGermplasmUUID()))
				.map(GermplasmImportDTO::getGermplasmUUID)
				.collect(
					Collectors.toList());
		final List<Germplasm> germplasmDTOS = this.germplasmServiceMw.getGermplasmByGUIDs(guidsList);
		if (!germplasmDTOS.isEmpty()) {
			this.errors.reject("germplasm.import.existent.guids",
				new String[] {
					Util.buildErrorMessageFromList(germplasmDTOS.stream().map(Germplasm::getGermplasmUUID).collect(Collectors.toList()),
						3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateStockIds(final List<GermplasmInventoryImportDTO> germplasmInventoryImportDTOList) {
		final List<String> stockIds =
			germplasmInventoryImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getStockId())).map(g -> g.getStockId())
				.collect(Collectors.toList());

		if (!stockIds.isEmpty()) {
			final List<String> uniqueNotNullStockIds = stockIds.stream().distinct().collect(Collectors.toList());
			if (stockIds.size() != uniqueNotNullStockIds.size()) {
				this.errors.reject("lot.input.list.stock.ids.duplicated", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			if (uniqueNotNullStockIds.stream().filter(c -> c.length() > STOCK_ID_MAX_LENGTH).count() > 0) {
				this.errors.reject("lot.stock.id.length.higher.than.maximum", new String[] {String.valueOf(STOCK_ID_MAX_LENGTH)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			final List<LotDto> existingLotDtos = this.lotService.getLotsByStockIds(uniqueNotNullStockIds);
			if (!existingLotDtos.isEmpty()) {
				final List<String> existingStockIds = existingLotDtos.stream().map(LotDto::getStockId).collect(Collectors.toList());
				this.errors
					.reject("lot.input.list.stock.ids.invalid", new String[] {Util.buildErrorMessageFromList(existingStockIds, 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateUnits(final List<GermplasmInventoryImportDTO> germplasmInventoryImportDTOList) {
		final List<String> units =
			germplasmInventoryImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getUnit())).map(g -> g.getUnit()).distinct()
				.collect(Collectors.toList());
		if (!units.isEmpty()) {
			this.inventoryCommonValidator.validateUnitNames(new ArrayList<>(units), this.errors);
		}
	}

	private boolean areNameValuesInvalid(final Collection<String> values) {
		return values.stream().anyMatch(n -> {
			if (StringUtils.isEmpty(n)) {
				this.errors.reject("germplasm.import.name.type.value.null.empty", "");
				return true;
			}
			if (n.length() > NAME_MAX_LENGTH) {
				this.errors.reject("germplasm.import.name.type.value.invalid.length", "");
				return true;
			}
			return false;
		});
	}

	private boolean areAttributesInvalid(final Integer id, final Map<String, String> attributes) {
		if (attributes != null) {
			final Set<String> attributeKeys = new HashSet<>();

			if (attributes.keySet().stream().anyMatch(Objects::isNull)) {
				this.errors.reject("germplasm.import.null.attributes", new String[] {(id != null) ? String.valueOf(id) : "Unknown"}, "");
				return true;
			}
			attributes.keySet().forEach(attr -> attributeKeys.add(attr.toUpperCase()));
			if (attributes.keySet().size() != attributeKeys.size()) {
				this.errors
					.reject("germplasm.import.duplicated.attributes", new String[] {(id != null) ? String.valueOf(id) : "Unknown"}, "");
				return true;
			}
			if (attributes.values().stream().anyMatch(n -> {
				if (StringUtils.isEmpty(n)) {
					this.errors.reject("germplasm.import.attribute.value.null.empty", "");
					return true;
				}
				if (n.length() > ATTRIBUTE_MAX_LENGTH) {
					this.errors.reject("germplasm.import.attribute.value.invalid.length", "");
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

}
