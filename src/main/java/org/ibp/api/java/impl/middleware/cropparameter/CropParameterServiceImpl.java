package org.ibp.api.java.impl.middleware.cropparameter;

import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterEnum;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CropParameterServiceImpl implements CropParameterService {

	@Autowired
	private org.generationcp.middleware.api.cropparameter.CropParameterService cropParameterService;

	@Override
	public List<CropParameterDTO> getCropParameters(final Pageable pageable) {
		return this.cropParameterService.getCropParameters(pageable).stream().map(CropParameterDTO::new).collect(Collectors.toList());
	}

	@Override
	public void modifyCropParameter(final String key, final CropParameterPatchRequestDTO request) {
		this.cropParameterService.modifyCropParameter(key, request);
	}

	@Override
	public CropParameterDTO getCropParameter(final CropParameterEnum cropParameterEnum) {
		BaseValidator.checkNotNull(cropParameterEnum, "crop.parameter.required");

		return this.cropParameterService.getCropParameter(cropParameterEnum)
			.map(CropParameterDTO::new)
			.orElseThrow(() -> new ApiValidationException("", "crop.parameter.not.exists", cropParameterEnum.getKey()));
	}

}
