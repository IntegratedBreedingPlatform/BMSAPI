package org.ibp.api.java.impl.middleware.breedingmethod;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.MethodClassDTO;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Service
public class BreedingMethodServiceImpl implements BreedingMethodService {

	@Autowired
	private org.generationcp.middleware.api.breedingmethod.BreedingMethodService breedingMethodService;

	@Autowired
	private ProgramValidator programValidator;

	@Override
	public List<MethodClassDTO> getMethodClasses() {
		return this.breedingMethodService.getMethodClasses();
	}

	@Override
	public BreedingMethodDTO getBreedingMethod(final Integer breedingMethodDbId) {
		return this.breedingMethodService.getBreedingMethod(breedingMethodDbId);
	}

	@Override
	public List<BreedingMethodDTO> getBreedingMethods(final String cropName, final BreedingMethodSearchRequest searchRequest) {
		final MapBindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final String programUUID = searchRequest.getProgramUUID();
		if (searchRequest.isFavoritesOnly() && StringUtils.isEmpty(programUUID)) {
			errors.reject("breeding.methods.favorite.requires.program", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (programUUID != null) {
			this.programValidator.validate(new ProgramSummary(cropName, programUUID), errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}

		return this.breedingMethodService.getBreedingMethods(searchRequest);
	}

}
