package org.ibp.api.java.crop;

import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;

public interface CropGenotypingParameterService {

	CropGenotypingParameterDTO getCropGenotypingParameter(String cropname);

	void updateCropGenotypingParameter(CropGenotypingParameterDTO cropGenotypingParameterDTO);

	void createCropGenotypingParameter(CropGenotypingParameterDTO cropGenotypingParameterDTO);
}
