package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.api.germplasm.pedigree.GermplasmTreeNode;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.ibp.api.java.germplasm.GermplasmPedigreeService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class GermplasmPedigreeServiceImpl implements GermplasmPedigreeService {

	@Autowired
	private org.generationcp.middleware.api.germplasm.pedigree.GermplasmPedigreeService germplasmPedigreeService;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Override
	public GermplasmTreeNode getGermplasmPedigreeTree(final Integer gid, final Integer level, final boolean includeDerivativeLines) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		return this.germplasmPedigreeService.getGermplasmPedigreeTree(gid, level, includeDerivativeLines);
	}

	@Override
	public List<GermplasmDto> getGenerationHistory(final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		return this.germplasmPedigreeService.getGenerationHistory(gid);
	}

	@Override
	public List<GermplasmDto> getManagementNeighbors(final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		return this.germplasmPedigreeService.getManagementNeighbors(gid);
	}

	@Override
	public List<GermplasmDto> getGroupRelatives(final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		return this.germplasmPedigreeService.getGroupRelatives(gid);
	}

}
