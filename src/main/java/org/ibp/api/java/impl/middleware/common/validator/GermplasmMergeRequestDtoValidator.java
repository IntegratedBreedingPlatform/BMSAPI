package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GermplasmMergeRequestDtoValidator {

	@Resource
	private GermplasmService germplasmService;

	public void validate(final GermplasmMergeRequestDto germplasmMergeRequestDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmUpdateDTO.class.getName());
		final List<Integer> gidsOfNonSelectedGermplasm =
			germplasmMergeRequestDto.getNonSelectedGermplasm().stream().filter(o -> !o.isOmit()).map(
				GermplasmMergeRequestDto.NonSelectedGermplasm::getGermplasmId).collect(Collectors.toList());
		final Set<Integer> germplasmWithDescendants = this.germplasmService.getGidsOfGermplasmWithDescendants(gidsOfNonSelectedGermplasm);

		if (!CollectionUtils.isEmpty(germplasmWithDescendants)) {
			errors.reject("germplasm.merge.cannot.merge.germplasm.with.progeny", "");
		}

		final List<Germplasm> germplasmList = this.germplasmService.getGermplasmByGIDs(gidsOfNonSelectedGermplasm);
		final boolean oneOfGermplasmIsFixed =
			germplasmList.stream().anyMatch(o -> o.getMgid() != null && !o.getMgid().equals(0));
		if (oneOfGermplasmIsFixed) {
			errors.reject("germplasm.merge.cannot.merge.germplasm.already.grouped", "");
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}