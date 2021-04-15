package org.ibp.api.java.impl.middleware.germplasm.validator;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.ProgenitorsUpdateRequestDto;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProgenitorsUpdateRequestDtoValidator {

	private BindingResult errors;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private BreedingMethodService breedingMethodService;

	private static final String GENERATIVE = MethodType.GENERATIVE.getCode();

	private static final Integer MUTATION_MPRGN = 1;

	public void validate(final Integer gid, final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), ProgenitorsUpdateRequestDto.class.getName());

		BaseValidator.checkNotNull(progenitorsUpdateRequestDto, "request.null");

		final Integer gpid1 = progenitorsUpdateRequestDto.getGpid1();
		final Integer gpid2 = progenitorsUpdateRequestDto.getGpid2();
		final List<Integer> otherProgenitors = progenitorsUpdateRequestDto.getOtherProgenitors();

		BreedingMethodDTO newMethod = null;
		if (progenitorsUpdateRequestDto.getBreedingMethodId() != null) {
			final BreedingMethodSearchRequest searchRequest =
				new BreedingMethodSearchRequest();
			searchRequest.setMethodIds(Collections.singletonList(progenitorsUpdateRequestDto.getBreedingMethodId()));
			final List<BreedingMethodDTO> methodDTOS = this.breedingMethodService.getBreedingMethods(searchRequest, null);
			if (methodDTOS.isEmpty()) {
				this.errors.reject("germplasm.update.breeding.method.invalid", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			newMethod = methodDTOS.get(0);
		}

		final BreedingMethodDTO finalBreedingMethod = this.getFinalBreedingMethod(gid, newMethod);
		final boolean isMutation =
			(GENERATIVE.equalsIgnoreCase(finalBreedingMethod.getType()) && MUTATION_MPRGN
				.equals(finalBreedingMethod.getNumberOfProgenitors())) ? true : false;

		//For mutations, gpid2 can be null
		if ((gpid1 == null && gpid2 != null) || (gpid2 == null && gpid1 != null && !isMutation)) {
			this.errors.reject("germplasm.import.invalid.progenitors.combination", "");
		}

		if ((gpid2 == null || gpid2.equals(0)) && CollectionUtils.isNotEmpty(otherProgenitors)) {
			this.errors.reject("germplasm.update.no.extra.progenitors.allowed", "");
		}

		if (!GENERATIVE.equalsIgnoreCase(finalBreedingMethod.getType()) && CollectionUtils.isNotEmpty(otherProgenitors)) {
			this.errors.reject("germplasm.update.no.extra.progenitors.allowed.non.gen.breeding.method", "");
		}

		if (GENERATIVE.equalsIgnoreCase(finalBreedingMethod.getType()) && CollectionUtils.isNotEmpty(otherProgenitors)
			&& !finalBreedingMethod.getNumberOfProgenitors().equals(0)) {
			this.errors
				.reject("germplasm.update.no.extra.progenitors.allowed.final.gen.method", new String[] {finalBreedingMethod.getName()}, "");
		}

		if (CollectionUtils.isNotEmpty(otherProgenitors)
			&& otherProgenitors.stream().filter(i -> Objects.isNull(i) || i.equals(0)).count() > 0) {
			this.errors.reject("germplasm.update.extra.progenitors.can.not.be.null", "");
		}

		final Set<Integer> allProgenitors = this.getAllProgenitors(gpid1, gpid2, otherProgenitors);

		final List<Integer> existingGermplasm =
			germplasmService.getGermplasmByGIDs(Lists.newArrayList(allProgenitors)).stream().map(Germplasm::getGid).collect(
				Collectors.toList());
		if (existingGermplasm.size() != allProgenitors.size()) {
			errors.reject("germplasm.update.invalid.gid", new String[] {
				String.join(",",
					allProgenitors.stream().filter(g -> !existingGermplasm.contains(g)).map(o -> String.valueOf(o)).collect(
						Collectors.toSet()))}, "");
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private BreedingMethodDTO getFinalBreedingMethod(final Integer gid, final BreedingMethodDTO newMethod) {
		BreedingMethodDTO method = newMethod;
		if (method == null) {
			final GermplasmDto germplasm = this.germplasmService.getGermplasmDtoById(gid);
			final BreedingMethodSearchRequest searchRequest =
				new BreedingMethodSearchRequest();
			searchRequest.setMethodIds(Collections.singletonList(germplasm.getBreedingMethodId()));
			final List<BreedingMethodDTO> methodDTOS = this.breedingMethodService.getBreedingMethods(searchRequest, null);
			method = methodDTOS.get(0);
		}
		return method;
	}

	private Set<Integer> getAllProgenitors(final Integer gpid1, final Integer gpid2, final List<Integer> otherProgenitors) {
		final Set<Integer> allProgenitors = new HashSet<>();
		if (Objects.nonNull(gpid1) && !gpid1.equals(0)) {
			allProgenitors.add(gpid1);
		}
		if (Objects.nonNull(gpid2) && !gpid2.equals(0)) {
			allProgenitors.add(gpid2);
		}
		if (CollectionUtils.isNotEmpty(otherProgenitors)) {
			allProgenitors
				.addAll(otherProgenitors.stream().filter(p -> Objects.nonNull(p) && !p.equals(0))
					.collect(Collectors.toList()));
		}
		return allProgenitors;
	}
}
