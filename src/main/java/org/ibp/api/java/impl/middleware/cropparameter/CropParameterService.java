package org.ibp.api.java.impl.middleware.cropparameter;

import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterEnum;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.generationcp.middleware.pojos.CropParameter;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CropParameterService {

	List<CropParameterDTO> getCropParameters(Pageable pageable);

	void modifyCropParameter(String key, CropParameterPatchRequestDTO request);

	Optional<CropParameter> getCropParameter(CropParameterEnum cropParameterEnum);
}
