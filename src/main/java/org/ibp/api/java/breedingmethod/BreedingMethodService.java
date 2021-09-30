package org.ibp.api.java.breedingmethod;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.MethodClassDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BreedingMethodService {

	List<MethodClassDTO> getMethodClasses();

	BreedingMethodDTO getBreedingMethod(Integer breedingMethodDbId);

	BreedingMethodDTO create(BreedingMethodNewRequest breedingMethod);

	BreedingMethodDTO edit(Integer breedingMethodDbId, BreedingMethodNewRequest breedingMethod);

	void delete(Integer breedingMethodDbId);

	List<BreedingMethodDTO> getBreedingMethods(String cropName, BreedingMethodSearchRequest searchRequest, Pageable pageable);

	Long countBreedingMethods(BreedingMethodSearchRequest searchRequest);
}
