package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.service.api.userdefinedfield.UserDefinedFieldService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	@Autowired
	private UserDefinedFieldService userDefinedFieldService;

	public void validateEmptyList(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		if (germplasmUpdateDTOList == null || germplasmUpdateDTOList.isEmpty()) {
			errors.reject("germplasm.update.empty.list", "");
		}
	}

	public void validateCodes(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> attributesAndNamesCodes = new HashSet<>(germplasmUpdateDTOList.get(0).getData().keySet());

		final Map<String, Integer> attributeCodes =
			this.userDefinedFieldService
				.getByTableAndCodesInMap(UDTableType.ATRIBUTS_ATTRIBUTE.getTable(), new ArrayList<>(attributesAndNamesCodes));
		final Map<String, Integer> nameCodes =
			this.userDefinedFieldService
				.getByTableAndCodesInMap(UDTableType.NAMES_NAME.getTable(), new ArrayList<>(attributesAndNamesCodes));

		attributesAndNamesCodes.removeAll(attributeCodes.keySet());
		attributesAndNamesCodes.removeAll(nameCodes.keySet());

		if (!attributesAndNamesCodes.isEmpty()) {
			errors.reject("germplasm.update.invalid.attribute.or.name.code", new String[] {String.join(",", attributesAndNamesCodes)}, "");
		}

		final Collection<String> ambiguosCodes = CollectionUtils.intersection(attributeCodes.keySet(), nameCodes.keySet());
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

	public void validateLocationAbbreviation(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> locationAbbreviationSet =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isNotEmpty(dto.getLocationAbbreviation()))
				.map(dto -> dto.getLocationAbbreviation()).collect(Collectors.toSet());
		final List<String> abbreviations =
			this.locationDataManager.getLocationsByAbbreviation(locationAbbreviationSet).stream().map(loc -> loc.getLabbr()).collect(
				Collectors.toList());

		locationAbbreviationSet.removeAll(abbreviations);

		if (!locationAbbreviationSet.isEmpty()) {
			errors.reject("germplasm.update.invalid.location.abbreviation", new String[] {String.join(",", locationAbbreviationSet)}, "");
		}

	}

	public void validateBreedingMethod(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Set<String> breedingMethodCodes =
			germplasmUpdateDTOList.stream().filter(dto -> StringUtils.isNotEmpty(dto.getBreedingMethod()))
				.map(dto -> dto.getBreedingMethod()).collect(Collectors.toSet());
		final List<String> codes =
			this.breedingMethodService.getBreedingMethodsByCodes(breedingMethodCodes).stream().map(loc -> loc.getCode()).collect(
				Collectors.toList());

		breedingMethodCodes.removeAll(codes);

		if (!breedingMethodCodes.isEmpty()) {
			errors.reject("germplasm.update.invalid.breeding.method", new String[] {String.join(",", breedingMethodCodes)}, "");
		}

	}

	public void validateCreationDate(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Optional<GermplasmUpdateDTO> optionalGermplasmUpdateDTOWithInvalidDate =
			germplasmUpdateDTOList.stream().filter(o -> StringUtils.isNotEmpty(o.getCreationDate()) && !DateUtil.isValidDate(o.getCreationDate())).findAny();

		if (optionalGermplasmUpdateDTOWithInvalidDate.isPresent()) {
			errors.reject("germplasm.update.invalid.creation.date", "");
		}

	}

}
