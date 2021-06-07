package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.service.api.GermplasmGroup;
import org.ibp.api.domain.germplasm.GermplasmUngroupingResponse;
import org.ibp.api.java.germplasm.GermplasmGroupingRequest;
import org.ibp.api.java.germplasm.GermplasmGroupingService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class GermplasmGroupingServiceImpl implements GermplasmGroupingService {

	@Autowired
	private org.generationcp.middleware.service.api.GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Override
	public List<GermplasmGroup> markFixed(final GermplasmGroupingRequest germplasmGroupingRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, germplasmGroupingRequest.getGids());
		return this.germplasmGroupingService.markFixed(germplasmGroupingRequest.getGids(), germplasmGroupingRequest.isIncludeDescendants(),
			germplasmGroupingRequest.isPreserveExistingGroup());
	}

	@Override
	public GermplasmUngroupingResponse unfixLines(final List<Integer> gids) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, gids);
		final List<Integer> unfixedGids = this.germplasmGroupingService.unfixLines(gids);
		final GermplasmUngroupingResponse response = new GermplasmUngroupingResponse();
		response.setUnfixedGids(unfixedGids);
		gids.removeAll(unfixedGids);
		response.setNumberOfGermplasmWithoutGroup(gids.size());
		return response;
	}
}
