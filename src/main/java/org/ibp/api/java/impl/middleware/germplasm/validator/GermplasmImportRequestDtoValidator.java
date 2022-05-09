package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmInventoryImportDTO;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
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

	private static final Set<Integer> STORAGE_LOCATION_TYPE = Collections.singleton(1500);
	static final Integer STOCK_ID_MAX_LENGTH = 35;
	static final Integer REFERENCE_MAX_LENGTH = 255;
	public static final Integer NAME_MAX_LENGTH = 5000;
	private static final List<VariableType> ATTRIBUTE_TYPES =
		Arrays.asList(VariableType.GERMPLASM_ATTRIBUTE, VariableType.GERMPLASM_PASSPORT);

	private BindingResult errors;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private GermplasmNameService germplasmNameService;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private LotService lotService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	public void validateBeforeSaving(final String programUUID, final GermplasmImportRequestDto germplasmImportRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmImportRequestDto.class.getName());

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

			if (!StringUtil.isEmpty(g.getGermplasmPUI()) && g.getGermplasmPUI().length() > NAME_MAX_LENGTH) {
				this.errors.reject("germplasm.import.pui.invalid.length", "");
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

			if (germplasmImportRequestDto.getConnectUsing() == GermplasmImportRequestDto.PedigreeConnectionType.GID
				&& ((StringUtils.isNotEmpty(g.getProgenitor1()) && !StringUtils.isNumeric(g.getProgenitor1())) || (
				StringUtils.isNotEmpty(g.getProgenitor2()) && !StringUtils.isNumeric(g.getProgenitor2())))) {
				this.errors.reject("germplasm.import.progenitor.must.be.numeric.when.connecting.by.gid", "");
				return true;
			}

			return false;

		});

		if (invalid) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!germplasmImportRequestDto.isSkipIfExists()) {
			this.validatePUINotExists(germplasmImportDTOList);
		}
		this.validateNotDuplicatedPUI(germplasmImportDTOList);
		this.validateAllBreedingMethodAbbreviationsExistsAndNotAcceptMutations(germplasmImportDTOList);
		this.validateAllLocationAbbreviationsExists(germplasmImportDTOList);
		this.validateAllNameTypesExists(germplasmImportDTOList);
		this.validateAllAttributesExists(programUUID, germplasmImportDTOList);

	}

	public void validateImportLoadedData(final String programUUID,
		final List<GermplasmInventoryImportDTO> germplasmInventoryImportDTOList) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmInventoryImportDTO.class.getName());

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

			if (!StringUtil.isEmpty(g.getGermplasmPUI()) && g.getGermplasmPUI().length() > NAME_MAX_LENGTH) {
				this.errors.reject("germplasm.import.pui.invalid.length", "");
				return true;
			}

			if (!StringUtil.isEmpty(g.getGermplasmPUI()) && g.getGermplasmPUI().equals("0")) {
				this.errors.reject("germplasm.import.pui.invalid.zero", "");
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

		this.validateNotDuplicatedPUI(germplasmInventoryImportDTOList);
		this.validateAllBreedingMethodAbbreviationsExistsAndNotAcceptMutations(germplasmInventoryImportDTOList);
		this.validateAllLocationAbbreviationsExists(germplasmInventoryImportDTOList);
		this.validateAllStorageLocationAbbreviationsExists(germplasmInventoryImportDTOList);
		this.validateAllNameTypesExists(germplasmInventoryImportDTOList);
		this.validateAllAttributesExists(programUUID, germplasmInventoryImportDTOList);
		this.validateStockIds(germplasmInventoryImportDTOList);
		this.validateUnits(germplasmInventoryImportDTOList);
	}

	private void validateAllNameTypesExists(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> nameTypes = new HashSet<>();
		germplasmImportDTOList.forEach(g -> {
			if (g.getNames() != null && !g.getNames().isEmpty())
				nameTypes.addAll(g.getNames().keySet().stream().map(String::toUpperCase).collect(Collectors.toList()));
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

	private void validateAllBreedingMethodAbbreviationsExistsAndNotAcceptMutations(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> breedingMethodsAbbrs =
			germplasmImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getBreedingMethodAbbr()))
				.map(g -> g.getBreedingMethodAbbr().toUpperCase()).collect(
				Collectors.toSet());
		if (!breedingMethodsAbbrs.isEmpty()) {
			final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest();
			searchRequest.setMethodAbbreviations(new ArrayList<>(breedingMethodsAbbrs));
			final List<BreedingMethodDTO> existingBreedingMethods =
				this.breedingMethodService.searchBreedingMethods(searchRequest, null, null);
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

	private void validateAllLocationAbbreviationsExists(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> locationAbbrs = germplasmImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getLocationAbbr()))
			.map(g -> g.getLocationAbbr().toUpperCase()).collect(Collectors.toSet());
		if (!locationAbbrs.isEmpty()) {
			final List<String> existingLocations =
				this.locationService
					.searchLocations(new LocationSearchRequest(null, null, new ArrayList<>(locationAbbrs), null),
						null, null)
					.stream().map(
					LocationDTO::getAbbreviation).collect(
					Collectors.toList());
			if (locationAbbrs.size() != existingLocations.size()) {
				locationAbbrs.removeAll(existingLocations);
				this.errors.reject("germplasm.import.location.abbreviations.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(locationAbbrs), 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateAllStorageLocationAbbreviationsExists(final List<GermplasmInventoryImportDTO> germplasmInventoryImportDTOList) {
		final List<String> locationAbbreviations =
			germplasmInventoryImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getStorageLocationAbbr()))
				.map(g -> g.getStorageLocationAbbr().toUpperCase()).distinct().collect(Collectors.toList());
		if (!locationAbbreviations.isEmpty()) {
			final List<LocationDTO> existingLocations =
				this.locationService.searchLocations(
					new LocationSearchRequest(STORAGE_LOCATION_TYPE, null, new ArrayList<>(locationAbbreviations), null), null, null);
			if (existingLocations.size() != locationAbbreviations.size()) {
				final List<String> existingAbbreviations = existingLocations.stream().map(LocationDTO::getAbbreviation).collect(Collectors.toList());
				final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
				invalidAbbreviations.removeAll(existingAbbreviations);
				this.errors
					.reject("germplasm.import.storage.location.abbreviations.not.exist",
						new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateAllAttributesExists(final String programUUID, final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> attributes = new HashSet<>();
		germplasmImportDTOList.stream().filter(germ -> germ.getAttributes() != null).collect(Collectors.toList())
			.forEach(g -> attributes.addAll(g.getAttributes().keySet().stream().map(String::toUpperCase).collect(Collectors.toList())));
		if (!attributes.isEmpty()) {
			final VariableFilter variableFilter = new VariableFilter();
			variableFilter.setProgramUuid(programUUID);
			ATTRIBUTE_TYPES.forEach(variableFilter::addVariableType);
			attributes.forEach(variableFilter::addName);

			final List<Variable> existingAttributeVariables =
				this.ontologyVariableDataManager.getWithFilter(variableFilter);

			if (existingAttributeVariables.size() != attributes.size()) {
				//Check if same variable was used by name or alias
				existingAttributeVariables.forEach(v -> {
					if (attributes.contains(v.getName().toUpperCase()) && StringUtils.isNotEmpty(v.getAlias()) && attributes
						.contains(v.getAlias().toUpperCase())) {
						this.errors.reject("germplasm.import.two.columns.referring.to.same.variable",
							new String[] {v.getName(), v.getAlias()}, "");
						throw new ApiRequestValidationException(this.errors.getAllErrors());
					}
				});

				attributes.removeAll(existingAttributeVariables.stream()
					.map(v -> v.getName().toUpperCase())
					.collect(Collectors.toSet()));

				attributes.removeAll(existingAttributeVariables.stream().filter(va -> StringUtils.isNotEmpty(va.getAlias()))
					.map(v -> v.getAlias().toUpperCase())
					.collect(Collectors.toSet()));
				this.errors.reject("germplasm.import.attributes.not.exist",
					new String[] {Util.buildErrorMessageFromList(new ArrayList<>(attributes), 3)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateNotDuplicatedPUI(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final List<String> puiList =
			germplasmImportDTOList.stream().filter(g -> !StringUtils.isEmpty(g.getGermplasmPUI()))
				.map(GermplasmImportDTO::getGermplasmPUI)
				.collect(
					Collectors.toList());
		if (!puiList.stream().filter(i -> Collections.frequency(puiList, i) > 1)
			.collect(Collectors.toSet()).isEmpty()) {
			this.errors.reject("germplasm.import.duplicated.puis", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validatePUINotExists(final List<? extends GermplasmImportDTO> germplasmImportDTOList) {
		final List<String> puisList = new ArrayList<>();
			germplasmImportDTOList.stream().forEach(g -> puisList.addAll(g.collectGermplasmPUIs()));
		final List<String> existingGermplasmPUIs = this.germplasmNameService.getExistingGermplasmPUIs(puisList);
		if (!existingGermplasmPUIs.isEmpty()) {
			this.errors.reject("germplasm.import.existent.puis",
				new String[] {
					Util.buildErrorMessageFromList(existingGermplasmPUIs, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateStockIds(final List<GermplasmInventoryImportDTO> germplasmInventoryImportDTOList) {
		final List<String> stockIds =
			germplasmInventoryImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getStockId())).map(GermplasmInventoryImportDTO::getStockId)
				.collect(Collectors.toList());

		if (!stockIds.isEmpty()) {
			final List<String> uniqueNotNullStockIds = stockIds.stream().distinct().collect(Collectors.toList());
			if (stockIds.size() != uniqueNotNullStockIds.size()) {
				this.errors.reject("lot.input.list.stock.ids.duplicated", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			if (uniqueNotNullStockIds.stream().anyMatch(c -> c.length() > STOCK_ID_MAX_LENGTH)) {
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
			germplasmInventoryImportDTOList.stream().filter(g -> StringUtils.isNotEmpty(g.getUnit())).map(GermplasmInventoryImportDTO::getUnit).distinct()
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
			return attributes.values().stream().anyMatch(n -> {
				if (StringUtils.isNotEmpty(n) && n.length() > AttributeValidator.GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH) {
					this.errors.reject("germplasm.import.attribute.value.invalid.length", "");
					return true;
				}
				return false;
			});
		}
		return false;
	}

}
