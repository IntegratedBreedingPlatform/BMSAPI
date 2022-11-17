package org.ibp.api.java.crop;

import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;

public interface CropGenotypingParameterService {

	CropGenotypingParameterDTO getCropGenotypingParameter(String keyFilter);

	String getToken(String cropName);
}
