package org.ibp.api.java.impl.middleware.germplasm.validator;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.germplasm.GermplasmPatchDto;
import org.generationcp.middleware.pojos.Germplasm;
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
public class GermplasmPatchDtoValidator {

	static final Integer REFERENCE_MAX_LENGTH = 255;

	private BindingResult errors;

	@Autowired
	private LocationService locationService;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private GermplasmService germplasmService;

	public void validate(final String programUUID, final GermplasmPatchDto germplasmPatchDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmPatchDto.class.getName());
		BaseValidator.checkNotNull(germplasmPatchDto, "germplasm.import.request.null");
		if (germplasmPatchDto.getBreedingLocationId() != null) {
			if (this.locationService
				.getFilteredLocations(
					new LocationSearchRequest(programUUID, null, Collections.singletonList(germplasmPatchDto.getBreedingLocationId()), null,
						null, false),
					null).isEmpty()) {
				this.errors.reject("germplasm.update.breeding.location.invalid", "");
			}
		}
		if (germplasmPatchDto.getBreedingMethodId() != null) {
			final BreedingMethodSearchRequest searchRequest =
				new BreedingMethodSearchRequest();
			searchRequest.setMethodIds(Collections.singletonList(germplasmPatchDto.getBreedingMethodId()));
			if (this.breedingMethodService.getBreedingMethods(searchRequest, null).isEmpty()) {
				this.errors.reject("germplasm.update.breeding.method.invalid", "");
			}
		}
		if (StringUtils.isNotEmpty(germplasmPatchDto.getReference()) && germplasmPatchDto.getReference().length() > REFERENCE_MAX_LENGTH) {
			this.errors.reject("germplasm.import.reference.length.error", "");
		}

		if (StringUtils.isNotEmpty(germplasmPatchDto.getCreationDate()) && !DateUtil.isValidDate(germplasmPatchDto.getCreationDate())) {
			this.errors.reject("germplasm.import.creation.date.invalid", "");
		}

		this.validateProgenitors(germplasmPatchDto);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateProgenitors(final GermplasmPatchDto germplasmPatchDto) {
		if ((germplasmPatchDto.getGpid1() == null && germplasmPatchDto.getGpid2() != null) || (germplasmPatchDto.getGpid2() == null
			&& germplasmPatchDto.getGpid1() != null)) {
			this.errors.reject("germplasm.import.invalid.progenitors.combination", "");
		}

		//To confirm if there can be other progenitors if gpid1 or gpid2 are not specified
		if ((germplasmPatchDto.getGpid1() == null || germplasmPatchDto.getGpid2() == null || germplasmPatchDto.getGpid1().equals(0)
			|| germplasmPatchDto.getGpid2().equals(0)) && CollectionUtils.isNotEmpty(germplasmPatchDto.getOtherProgenitors())) {
			this.errors.reject("germplasm.update.no.extra.progenitors.allowed", "");
		}

		if (CollectionUtils.isNotEmpty(germplasmPatchDto.getOtherProgenitors())
			&& germplasmPatchDto.getOtherProgenitors().stream().filter(Objects::isNull).count() > 0) {
			this.errors.reject("germplasm.update.extra.progenitors.can.not.be.null", "");
		}

		final Set<Integer> allProgenitors = new HashSet<>();
		if (germplasmPatchDto.getGpid1() != null && germplasmPatchDto.getGpid1() != 0) {
			allProgenitors.add(germplasmPatchDto.getGpid1());
		}
		if (germplasmPatchDto.getGpid2() != null && germplasmPatchDto.getGpid2() != 0) {
			allProgenitors.add(germplasmPatchDto.getGpid2());
		}
		if (CollectionUtils.isNotEmpty(germplasmPatchDto.getOtherProgenitors())) {
			allProgenitors
				.addAll(germplasmPatchDto.getOtherProgenitors().stream().filter(p -> p != null && p != 0).collect(Collectors.toList()));
		}

		final List<Integer> existingGermplasm =
			germplasmService.getGermplasmByGIDs(Lists.newArrayList(allProgenitors)).stream().map(Germplasm::getGid).collect(
				Collectors.toList());
		if (existingGermplasm.size() != allProgenitors.size()) {
			errors.reject("germplasm.update.invalid.gid", new String[] {
				String.join(",",
					allProgenitors.stream().filter((gid) -> !existingGermplasm.contains(gid)).map(o -> String.valueOf(o)).collect(
						Collectors.toSet()))}, "");
		}
	}

}
