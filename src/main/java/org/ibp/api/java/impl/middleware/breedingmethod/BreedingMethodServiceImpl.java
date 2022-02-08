package org.ibp.api.java.impl.middleware.breedingmethod;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.MethodClassDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.ibp.api.java.impl.middleware.common.validator.BreedingMethodValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
			final MapBindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
			errors.reject("methoddbid.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		return breedingMethodDTO.get();
	}

	@Override
	public BreedingMethodDTO create(final BreedingMethodNewRequest breedingMethod) {
		this.breedingMethodValidator.validateCreation(breedingMethod);
		return this.breedingMethodService.create(breedingMethod);
	}

	@Override
	public BreedingMethodDTO edit(final Integer breedingMethodDbId, final BreedingMethodNewRequest breedingMethod) {
		this.breedingMethodValidator.validateEdition(breedingMethodDbId, breedingMethod);
		return this.breedingMethodService.edit(breedingMethodDbId, breedingMethod);
	}

	@Override
	public void delete(final Integer breedingMethodDbId) {
		this.breedingMethodValidator.validateDeletion(breedingMethodDbId);
		this.breedingMethodService.delete(breedingMethodDbId);
	}

	@Override
	public List<BreedingMethodDTO> searchBreedingMethods(final BreedingMethodSearchRequest searchRequest,
			final Pageable pageable, final String programUUID) {
		return this.breedingMethodService.searchBreedingMethods(searchRequest, pageable, programUUID);
	}

	@Override
	public Long countSearchBreedingMethods(final BreedingMethodSearchRequest searchRequest, final String programUUID) {
		return this.breedingMethodService.countSearchBreedingMethods(searchRequest, programUUID);
	}

}
