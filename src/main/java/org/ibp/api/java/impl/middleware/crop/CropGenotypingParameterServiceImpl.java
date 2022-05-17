package org.ibp.api.java.impl.middleware.crop;

import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.ibp.api.java.crop.CropGenotypingParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CropGenotypingParameterServiceImpl implements CropGenotypingParameterService {

	@Autowired
	private org.generationcp.middleware.service.api.crop.CropGenotypingParameterService cropGenotypiongParameterMiddlewareService;

	@Override
	public CropGenotypingParameterDTO getCropGenotypingParameter(final String cropName) {
		return this.cropGenotypiongParameterMiddlewareService.getCropGenotypingParameter(cropName);
	}

	@Override
	public void updateCropGenotypingParameter(final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		this.cropGenotypiongParameterMiddlewareService.updateCropGenotypingParameter(cropGenotypingParameterDTO);
	}

	@Override
	public void createCropGenotypingParameter(final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		this.cropGenotypiongParameterMiddlewareService.createCropGenotypingParameter(cropGenotypingParameterDTO);
	}

}
