package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GermplasmMergeRequestDtoValidator {

	@Resource
	private GermplasmService germplasmServiceMiddleware;

	@Resource
	private GermplasmValidator germplasmValidator;

	@Value("${germplasm.merge.max.number}")
	public int maximumGermplasmToMerge;

	public void validate(final GermplasmMergeRequestDto germplasmMergeRequestDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmMergeRequestDto.class.getName());
		BaseValidator.checkNotNull(germplasmMergeRequestDto, "germplasm.merge.request.null");
		BaseValidator.checkNotEmpty(germplasmMergeRequestDto.getNonSelectedGermplasm(), "germplasm.import.list.null");

		// Remove non-selected germplasm for omission
		germplasmMergeRequestDto.getNonSelectedGermplasm().removeIf(GermplasmMergeRequestDto.NonSelectedGermplasm::isOmit);
		if (CollectionUtils.isEmpty(germplasmMergeRequestDto.getNonSelectedGermplasm())) {
			errors.reject("germplasm.merge.no.germplasm.to.merge", "");
		} else {
			final boolean hasNullNonSelectedGermplasmId = germplasmMergeRequestDto.getNonSelectedGermplasm().stream().anyMatch(g -> Objects.isNull(g.getGermplasmId()));
			if (hasNullNonSelectedGermplasmId) {
				errors.reject("germplasm.merge.non.selected.null.gid", "");
			}

			final List<GermplasmMergeRequestDto.NonSelectedGermplasm> nonSelectedGermplasm =
				germplasmMergeRequestDto.getNonSelectedGermplasm().stream().collect(Collectors.toList());
			final List<Integer> gidsOfNonSelectedGermplasm = nonSelectedGermplasm.stream().map(
				GermplasmMergeRequestDto.NonSelectedGermplasm::getGermplasmId).collect(Collectors.toList());
			if (gidsOfNonSelectedGermplasm.contains(germplasmMergeRequestDto.getTargetGermplasmId())) {
				errors.reject("germplasm.merge.target.gid.in.non.selected.germplasm", "");
			}
			if (gidsOfNonSelectedGermplasm.size() > this.maximumGermplasmToMerge) {
				errors.reject("germplasm.merge.maximum.number.exceeeded", new String[] {String.valueOf(this.maximumGermplasmToMerge)}, "");
			}
			this.throwExceptionForValidationErrors(errors);

			this.germplasmValidator.validateGids(errors,
				ListUtils.union(gidsOfNonSelectedGermplasm, Collections.singletonList(germplasmMergeRequestDto.getTargetGermplasmId())));
			this.validateNonSelectedGermplasm(errors, nonSelectedGermplasm, gidsOfNonSelectedGermplasm);
		}

		this.throwExceptionForValidationErrors(errors);
	}

	private void throwExceptionForValidationErrors(final BindingResult errors) {
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateNonSelectedGermplasm(final BindingResult errors,
		final List<GermplasmMergeRequestDto.NonSelectedGermplasm> nonSelectedGermplasm, final List<Integer> gidsOfNonSelectedGermplasm) {

		final Set<Integer> germplasmWithDescendants =
			this.germplasmServiceMiddleware.getGidsOfGermplasmWithDescendants(gidsOfNonSelectedGermplasm);
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

		final Set<Integer> gidsLockedStudy = this.germplasmServiceMiddleware.getGermplasmUsedInLockedStudies(gidsOfNonSelectedGermplasm);
		if (!CollectionUtils.isEmpty(gidsLockedStudy)) {
			errors.reject("germplasm.merge.cannot.merge.germplasm.is.in.locked.study", new String[] {StringUtils.join(gidsLockedStudy, ", ")},
				"");
		}

		nonSelectedGermplasm.stream().anyMatch(g -> {
			if (g.isCloseLots() && g.isMigrateLots()) {
				errors.reject("germplasm.merge.invalid.lots.spec", new String[] {g.getGermplasmId().toString()}, "");
				return true;
			}
			return false;
		});
	}

	protected void setMaximumGermplasmToMerge(final int maximumGermplasmToMerge) {
		this.maximumGermplasmToMerge = maximumGermplasmToMerge;
	}

}
