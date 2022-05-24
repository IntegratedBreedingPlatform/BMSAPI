package org.ibp.api.java.crop;

import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;

public interface CropGenotypingParameterService {

	CropGenotypingParameterDTO getCropGenotypingParameter(String cropname);

	void updateCropGenotypingParameter(String cropName, CropGenotypingParameterDTO cropGenotypingParameterDTO);

	void createCropGenotypingParameter(String cropName, CropGenotypingParameterDTO cropGenotypingParameterDTO);

	String getToken(String cropName);
}
