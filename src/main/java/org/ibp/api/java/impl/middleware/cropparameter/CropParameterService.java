package org.ibp.api.java.impl.middleware.cropparameter;

import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterEnum;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CropParameterService {

	List<CropParameterDTO> getCropParameters(Pageable pageable);

	void modifyCropParameter(String key, CropParameterPatchRequestDTO request);

	CropParameterDTO getCropParameter(CropParameterEnum cropParameterEnum);

	CropGenotypingParameterDTO getCropGenotypingParameter(String keyFilter);

	String getToken(String cropName);
}
