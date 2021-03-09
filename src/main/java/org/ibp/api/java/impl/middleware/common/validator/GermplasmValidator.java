package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GermplasmValidator {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmService germplasmService;

	public void validateGermplasmId(final BindingResult errors, final Integer germplasmId) {
		if (germplasmId == null) {
			errors.reject("germplasm.required", "");
			return;
		}
		final Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(germplasmId);
		if (germplasm == null) {
			errors.reject("germplasm.invalid", "");
		}
	}

	public void validateGids(final BindingResult errors, final List<Integer> gids) {
		final List<Germplasm> existingGermplasms = germplasmDataManager.getGermplasms(gids);
		if (existingGermplasms.size() != gids.size() || existingGermplasms.stream().filter(g -> g.getDeleted()).count() > 0) {
			final List<Integer> existingGids =
				existingGermplasms.stream().filter(g -> !g.getDeleted()).map(Germplasm::getGid).collect(Collectors.toList());
			final List<Integer> invalidGids = new ArrayList<>(gids);
			invalidGids.removeAll(existingGids);
			errors.reject("gids.invalid", new String[] {Util.buildErrorMessageFromList(invalidGids, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
	public void validateGermplasmGUID(final BindingResult errors, final String germplasmGUID) {
		if (germplasmGUID == null) {
			errors.reject("germplasm.required", "");
			return;
		}
		final GermplasmDTO germplasm = this.germplasmService.getGermplasmDTObyGUID(germplasmGUID);
		if (germplasm == null) {
			errors.reject("germplasm.invalid", "");
		}
	}
}
