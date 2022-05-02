package org.ibp.api.java.impl.middleware.cropparameter;

import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
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
	public List<CropParameterDTO> getCropParameter(final Pageable pageable) {
		return this.cropParameterService.getCropParameter(pageable).stream().map(CropParameterDTO::new).collect(Collectors.toList());
	}

	@Override
	public void modifyCropParameter(final String key, final CropParameterPatchRequestDTO request) {
		this.cropParameterService.modifyCropParameter(key, request);
	}
}
