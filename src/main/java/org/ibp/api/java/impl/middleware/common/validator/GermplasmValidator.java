package org.ibp.api.java.impl.middleware.common.validator;

import liquibase.util.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GermplasmValidator {

	@Autowired
	private GermplasmService germplasmServiceMiddleware;

	public void validateGermplasmId(final BindingResult errors, final Integer germplasmId) {
		if (germplasmId == null) {
			errors.reject("germplasm.required", "");
			return;
		}
		final List<Germplasm> germplasm = this.germplasmServiceMiddleware.getGermplasmByGIDs(Arrays.asList(germplasmId));
		if (CollectionUtils.isEmpty(germplasm)) {
			errors.reject("germplasm.invalid", "");
		}
	}

	public void validateGids(final BindingResult errors, final List<Integer> gids) {
		final List<Germplasm> existingGermplasms = this.germplasmServiceMiddleware.getGermplasmByGIDs(gids);
		if (existingGermplasms.size() != gids.size() || existingGermplasms.stream().filter(Germplasm::getDeleted).count() > 0) {
			final List<Integer> existingGids =
				existingGermplasms.stream().filter(g -> !g.getDeleted()).map(Germplasm::getGid).collect(Collectors.toList());
			final List<Integer> invalidGids = new ArrayList<>(gids);
			invalidGids.removeAll(existingGids);
			errors.reject("gids.invalid", new String[] {Util.buildErrorMessageFromList(invalidGids, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
	public void validateGermplasmUUID(final BindingResult errors, final String germplasmUUID) {
		if (StringUtils.isEmpty(germplasmUUID)) {
			errors.reject("germplasm.required", "");
			return;
		}
		final List<Germplasm> germplasm = this.germplasmServiceMiddleware.getGermplasmByGUIDs(Collections.singletonList(germplasmUUID));
		if (CollectionUtils.isEmpty(germplasm)) {
			errors.reject("germplasm.invalid", "");
		}
	}
}
