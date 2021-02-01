package org.ibp.api.java.breedingmethod;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.MethodClassDTO;

import java.util.List;
import java.util.Set;

public interface BreedingMethodService {

	List<MethodClassDTO> getMethodClasses();

	BreedingMethodDTO getBreedingMethod(Integer breedingMethodDbId);

	List<BreedingMethodDTO> getBreedingMethods(String cropName, BreedingMethodSearchRequest searchRequest);

	Integer countBreedingMethod(BreedingMethodSearchRequest searchRequest);

}
