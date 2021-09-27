package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Value;
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
	private GermplasmService germplasmServiceMiddleware;

	@Value("${germplasm.merge.max.number}")
	public int maximumGermplasmToMerge;

	public void validate(final GermplasmMergeRequestDto germplasmMergeRequestDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmUpdateDTO.class.getName());
		final List<Integer> gidsOfNonSelectedGermplasm =
			germplasmMergeRequestDto.getNonSelectedGermplasm().stream().filter(o -> !o.isOmit()).map(
				GermplasmMergeRequestDto.NonSelectedGermplasm::getGermplasmId).collect(Collectors.toList());
		final Set<Integer> germplasmWithDescendants =
			this.germplasmServiceMiddleware.getGidsOfGermplasmWithDescendants(gidsOfNonSelectedGermplasm);

		if (CollectionUtils.isEmpty(gidsOfNonSelectedGermplasm)) {
			errors.reject("germplasm.merge.no.germplasm.to.merge", "");
		}

		if (gidsOfNonSelectedGermplasm.size() + 1 > this.maximumGermplasmToMerge) {
			errors.reject("germplasm.merge.maximum.number.exceeeded", new String[] {String.valueOf(this.maximumGermplasmToMerge)}, "");
		}

		if (!CollectionUtils.isEmpty(germplasmWithDescendants)) {
			errors.reject("germplasm.merge.cannot.merge.germplasm.with.progeny",
				new String[] {StringUtils.join(germplasmWithDescendants, ", ")}, "");
		}

		final Set<Integer> gidsFixed = this.germplasmServiceMiddleware.getCodeFixedGidsByGidList(gidsOfNonSelectedGermplasm);
		if (!CollectionUtils.isEmpty(gidsFixed)) {
			errors.reject("germplasm.merge.cannot.merge.germplasm.already.grouped", new String[] {StringUtils.join(gidsFixed, ", ")}, "");
		}

		final Set<Integer> gidsLockedList = this.germplasmServiceMiddleware.getGermplasmUsedInLockedList(gidsOfNonSelectedGermplasm);
		if (!CollectionUtils.isEmpty(gidsLockedList)) {
			errors.reject("germplasm.merge.cannot.merge.germplasm.is.in.locked.list", new String[] {StringUtils.join(gidsLockedList, ", ")},
				"");
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	protected void setMaximumGermplasmToMerge(final int maximumGermplasmToMerge) {
		this.maximumGermplasmToMerge = maximumGermplasmToMerge;
	}

}
