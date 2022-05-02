package org.ibp.api.java.impl.middleware.cropparameter;

import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CropParameterService {

	List<CropParameterDTO> getCropParameters(Pageable pageable);

	void modifyCropParameter(String key, CropParameterPatchRequestDTO request);
}
