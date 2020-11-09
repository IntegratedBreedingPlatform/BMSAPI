package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.UDTableType;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GermplasmUpdateValidator {

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private BreedingMethodService breedingMethodService;

	public void validateEmptyList(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		if (germplasmUpdateDTOList == null || germplasmUpdateDTOList.isEmpty()) {
			errors.reject("germplasm.update.empty.list", "");
		}
	}

	public void validateCodes(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> attributesAndNamesCodes = new HashSet<>(germplasmUpdateDTOList.get(0).getData().keySet());

		final Set<String> attributeCodes =
			this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCodes(UDTableType.ATRIBUTS_ATTRIBUTE.getTable(),
				Collections.singleton(UDTableType.ATRIBUTS_ATTRIBUTE.getType()), attributesAndNamesCodes).stream().map(o -> o.getFcode())
				.collect(Collectors.toSet());
		final Set<String> nameCodes =
			this.germplasmDataManager
				.getUserDefinedFieldByTableTypeAndCodes(UDTableType.NAMES_NAME.getTable(),
					Collections.singleton(UDTableType.NAMES_NAME.getType()), attributesAndNamesCodes).stream().map(o -> o.getFcode())
				.collect(Collectors.toSet());

		attributesAndNamesCodes.removeAll(attributeCodes);
		attributesAndNamesCodes.removeAll(nameCodes);

		if (!attributesAndNamesCodes.isEmpty()) {
			errors.reject("germplasm.update.invalid.attribute.or.name.code", new String[] {String.join(",", attributesAndNamesCodes)}, "");
		}

		final Collection<String> ambiguosCodes = CollectionUtils.intersection(attributeCodes, nameCodes);
		if (!ambiguosCodes.isEmpty()) {
			errors.reject("germplasm.update.ambiguous.code", new String[] {String.join(",", ambiguosCodes)}, "");
		}

	}

	public void validateGermplasmIdAndGermplasmUUID(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		// Find rows (GermplasmUpdateDTO) with blank GID and UUID.
		final Optional<GermplasmUpdateDTO> optionalGermplasmUpdateDTO =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isEmpty(dto.getGermplasmUUID()) && dto.getGid() == null).findAny();
		if (optionalGermplasmUpdateDTO.isPresent()) {
			errors.reject("germplasm.update.missing.gid.and.uuid", "");
		}

		final Set<Integer> gids = germplasmUpdateDTOList.stream().map(dto -> dto.getGid()).collect(Collectors.toSet());
		final Set<String> germplasmUUIDs = germplasmUpdateDTOList.stream().map(dto -> dto.getGermplasmUUID()).collect(Collectors.toSet());

		final List<Germplasm> germplasmByGIDs = this.germplasmDataManager.getGermplasms(new ArrayList<>(gids));
		final List<Germplasm> germplasmByUUIDs = this.germplasmDataManager.getGermplasmByUUIDs(germplasmUUIDs);

		gids.removeAll(germplasmByGIDs.stream().map(dto -> dto.getGid()).collect(Collectors.toSet()));
		germplasmUUIDs.removeAll(germplasmByUUIDs.stream().map(dto -> dto.getGermplasmUUID()).collect(Collectors.toSet()));

		if (!gids.isEmpty()) {
			errors.reject("germplasm.update.invalid.gid", new String[] {
				String.join(",", gids.stream().map(o -> String.valueOf(o)).collect(
					Collectors.toSet()))}, "");
		}

		if (!germplasmUUIDs.isEmpty()) {
			errors.reject("germplasm.update.invalid.uuid", new String[] {String.join(",", germplasmUUIDs)}, "");
		}

	}

	public void validateLocationAbbreviation(final BindingResult errors, final String programUUID,
		final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> locationAbbrs =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isNotEmpty(dto.getLocationAbbreviation()))
				.map(dto -> dto.getLocationAbbreviation()).collect(Collectors.toSet());
		final List<String> abbreviations =
			this.locationDataManager.getFilteredLocations(programUUID, null, null, new ArrayList<>(locationAbbrs), false).stream()
				.map(Location::getLabbr).collect(
				Collectors.toList());

		locationAbbrs.removeAll(abbreviations);

		if (!locationAbbrs.isEmpty()) {
			errors.reject("germplasm.update.invalid.location.abbreviation", new String[] {String.join(",", locationAbbrs)}, "");
		}

	}

	public void validateBreedingMethod(final BindingResult errors, final String programUUID,
		final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> breedingMethodsAbbrs =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isNotEmpty(dto.getBreedingMethod()))
				.map(dto -> dto.getBreedingMethod()).collect(Collectors.toSet());
		final List<String> codes =
			this.breedingMethodService.getBreedingMethods(programUUID, breedingMethodsAbbrs, false).stream().map(loc -> loc.getCode())
				.collect(
					Collectors.toList());

		breedingMethodsAbbrs.removeAll(codes);

		if (!breedingMethodsAbbrs.isEmpty()) {
			errors.reject("germplasm.update.invalid.breeding.method", new String[] {String.join(",", breedingMethodsAbbrs)}, "");
		}

	}

	public void validateCreationDate(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Optional<GermplasmUpdateDTO> optionalGermplasmUpdateDTOWithInvalidDate =
			germplasmUpdateDTOList.stream()
				.filter(o -> StringUtils.isNotEmpty(o.getCreationDate()) && !DateUtil.isValidDate(o.getCreationDate())).findAny();

		if (optionalGermplasmUpdateDTOWithInvalidDate.isPresent()) {
			errors.reject("germplasm.update.invalid.creation.date", "");
		}

	}

}
