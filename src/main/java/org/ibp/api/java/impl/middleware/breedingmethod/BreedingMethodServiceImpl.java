package org.ibp.api.java.impl.middleware.breedingmethod;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.MethodClassDTO;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.ibp.api.java.impl.middleware.common.validator.BreedingMethodValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BreedingMethodServiceImpl implements BreedingMethodService {

	@Autowired
	private org.generationcp.middleware.api.breedingmethod.BreedingMethodService breedingMethodService;

	@Autowired
	private ProgramValidator programValidator;

	@Autowired
	private BreedingMethodValidator breedingMethodValidator;

	@Override
	public List<MethodClassDTO> getMethodClasses() {
		return this.breedingMethodService.getMethodClasses();
	}

	@Override
	public BreedingMethodDTO getBreedingMethod(final Integer breedingMethodDbId) {
		final Optional<BreedingMethodDTO> breedingMethodDTO =  this.breedingMethodService.getBreedingMethod(breedingMethodDbId);
		if (!breedingMethodDTO.isPresent()) {
			final MapBindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("methoddbid.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		return breedingMethodDTO.get();
	}

	@Override
	public BreedingMethodDTO create(final BreedingMethodNewRequest breedingMethod) {
		this.breedingMethodValidator.validate(breedingMethod);
		return this.breedingMethodService.create(breedingMethod);
	}

	@Override
	public List<BreedingMethodDTO> getBreedingMethods(final String cropName, final BreedingMethodSearchRequest searchRequest,
		final Pageable pageable) {
		final MapBindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final String programUUID = searchRequest.getProgramUUID();
		if (searchRequest.isFavoritesOnly() && StringUtils.isEmpty(programUUID)) {
			errors.reject("breeding.methods.favorite.requires.program", "");
		}

		if (programUUID != null) {
			this.programValidator.validate(new ProgramDTO(cropName, programUUID), errors);
		}

		if (!CollectionUtils.isEmpty(searchRequest.getMethodTypes()) ) {
			final List<String> allMethodTypes = Arrays.stream(MethodType.values()).map(MethodType::getCode).collect(
				Collectors.toList());
			final boolean hasInvalidMethodType = searchRequest.getMethodTypes().stream().anyMatch(type -> !allMethodTypes.contains(type));
			if (hasInvalidMethodType) {
				errors.reject("invalid.breeding.method.type", "");
			}
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		return this.breedingMethodService.getBreedingMethods(searchRequest, pageable);
	}

	@Override
	public Long countBreedingMethods(final BreedingMethodSearchRequest searchRequest) {
		return this.breedingMethodService.countBreedingMethods(searchRequest);
	}

}
