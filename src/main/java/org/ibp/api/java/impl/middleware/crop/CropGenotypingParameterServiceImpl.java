package org.ibp.api.java.impl.middleware.crop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.crop.CropGenotypingParameterService;
import org.ibp.api.java.impl.middleware.common.validator.CropGenotypingParameterValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class CropGenotypingParameterServiceImpl implements CropGenotypingParameterService {

	@Autowired
	private org.generationcp.middleware.service.api.crop.CropGenotypingParameterService cropGenotypiongParameterMiddlewareService;

	@Autowired
	private CropGenotypingParameterValidator cropGenotypingParameterValidator;

	@Autowired
	private RestTemplate restTemplate;

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

	@Override
	public String getToken(final String cropName) {
		final Optional<CropGenotypingParameterDTO> cropGenotypingParameterDTOOptional =
			this.cropGenotypiongParameterMiddlewareService.getCropGenotypingParameter(cropName);
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
