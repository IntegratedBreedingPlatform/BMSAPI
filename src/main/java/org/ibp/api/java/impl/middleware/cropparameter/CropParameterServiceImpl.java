package org.ibp.api.java.impl.middleware.cropparameter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterEnum;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CropParameterServiceImpl implements CropParameterService {

	@Autowired
	private org.generationcp.middleware.api.cropparameter.CropParameterService cropParameterService;

	@Autowired
	private RestTemplate restTemplate;

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

	@Override
	public CropGenotypingParameterDTO getCropGenotypingParameter(final String keyFilter) {
		final Optional<CropGenotypingParameterDTO> cropGenotypingParameterDTOOptional =
			this.cropParameterService.getCropGenotypingParameter(keyFilter);
		if (cropGenotypingParameterDTOOptional.isPresent()) {
			return cropGenotypingParameterDTOOptional.get();
		} else {
			// return an empty object
			return new CropGenotypingParameterDTO();
		}
	}

	@Override
	public String getToken(final String cropName) {
		final Optional<CropGenotypingParameterDTO> cropGenotypingParameterDTOOptional =
			this.cropParameterService.getCropGenotypingParameter(cropName);
		if (cropGenotypingParameterDTOOptional.isPresent()) {
			final CropGenotypingParameterDTO cropGenotypingParameterDTO = cropGenotypingParameterDTOOptional.get();
			try {
				final HttpEntity<String> request = this.createTokenRequest(cropGenotypingParameterDTO);
				final String resultAsString =
					this.restTemplate.postForObject(cropGenotypingParameterDTO.getTokenEndpoint(), request, String.class);
				final JsonNode jsonNode = new ObjectMapper().readTree(resultAsString);
				return jsonNode.get("access_token").asText();
			} catch (final Exception ex) {
				throw new ApiRequestValidationException("crop.genotyping.parameter.cannot.generate.token", new String[] {});
			}
		}
		return StringUtils.EMPTY;
	}

	private HttpEntity<String> createTokenRequest(final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		final JSONObject data = new JSONObject();
		data.put("username", cropGenotypingParameterDTO.getUserName());
		data.put("password", cropGenotypingParameterDTO.getPassword());
		return new HttpEntity<>(data.toString(), headers);
	}
}
