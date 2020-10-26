package org.ibp.api.java.impl.middleware.common.validator;

import liquibase.util.StringUtils;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.Util;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
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

	public void validate(final BindingResult errors, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		if (germplasmUpdateDTOList == null || germplasmUpdateDTOList.isEmpty()) {
			errors.reject("germplasm.update.empty.list", "");
			return;
		}

		this.validateGermplasmIdAndGermplasmUUID(errors, germplasmUpdateDTOList);
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
			errors.reject("germplasm.update.invalid.gid", "");
		}

		if (!germplasmUUIDs.isEmpty()) {
			errors.reject("germplasm.update.invalid.uuid", "");
		}

	}

}
