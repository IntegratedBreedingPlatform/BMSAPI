package org.ibp.api.java.impl.middleware.crop;

import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.ibp.api.java.crop.CropGenotypingParameterService;
import org.ibp.api.java.impl.middleware.common.validator.CropGenotypingParameterValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CropGenotypingParameterServiceImpl implements CropGenotypingParameterService {

	@Autowired
	private org.generationcp.middleware.service.api.crop.CropGenotypingParameterService cropGenotypiongParameterMiddlewareService;

	@Autowired
	private CropGenotypingParameterValidator cropGenotypingParameterValidator;

	@Override
	public CropGenotypingParameterDTO getCropGenotypingParameter(final String cropName) {
		final Optional<CropGenotypingParameterDTO> cropGenotypingParameterDTOOptional =
			this.cropGenotypiongParameterMiddlewareService.getCropGenotypingParameter(cropName);
		if (cropGenotypingParameterDTOOptional.isPresent()) {
			return cropGenotypingParameterDTOOptional.get();
		} else {
			// return an empty object
			return new CropGenotypingParameterDTO();
		}
	}

	@Override
	public void updateCropGenotypingParameter(final String cropName, final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		this.cropGenotypingParameterValidator.validateEdition(cropName, cropGenotypingParameterDTO);
		this.cropGenotypiongParameterMiddlewareService.updateCropGenotypingParameter(cropGenotypingParameterDTO);
	}

	@Override
	public void createCropGenotypingParameter(final String cropName, final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		this.cropGenotypingParameterValidator.validateCreation(cropName, cropGenotypingParameterDTO);
		this.cropGenotypiongParameterMiddlewareService.createCropGenotypingParameter(cropGenotypingParameterDTO);
	}

}
