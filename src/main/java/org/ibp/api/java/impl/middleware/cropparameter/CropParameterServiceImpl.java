package org.ibp.api.java.impl.middleware.cropparameter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterEnum;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.generationcp.middleware.pojos.CropParameter;
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

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CropParameterServiceImpl implements CropParameterService {

	@Autowired
	private org.generationcp.middleware.api.cropparameter.CropParameterService cropParameterService;

	@Autowired
	private RestTemplate restTemplate;

	private static final String UTF_8 = "UTF-8";

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
	public List<CropParameterDTO> getCropParametersByGroupName(final String groupName) {
		return this.cropParameterService.getCropParametersByGroupName(groupName).stream()
			.map(CropParameterDTO::new).collect(Collectors.toList());
	}

	@Override
	public String getGenotypingToken(final String groupName) {
		final Map<String, CropParameter> cropParametersMap = this.cropParameterService.getCropParametersByGroupName(groupName)
			.stream().collect(Collectors.toMap(CropParameter::getKey, Function.identity()));
		try {
			final HttpEntity<String> request = this.createTokenRequest(cropParametersMap);
			final String tokenEndpoint = cropParametersMap.get(CropParameterEnum.GIGWA_TOKEN_ENDPOINT.getKey()).getValue();
			final String resultAsString =
				this.restTemplate.postForObject(URLDecoder.decode(tokenEndpoint, UTF_8), request, String.class);
			final JsonNode jsonNode = new ObjectMapper().readTree(resultAsString);
			return jsonNode.get("access_token").asText();
		} catch (final Exception ex) {
			throw new ApiRequestValidationException("crop.genotyping.parameter.cannot.generate.token", new String[] {});
		}
	}

	private HttpEntity<String> createTokenRequest(final Map<String, CropParameter> cropParametersMap)
		throws Exception {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		final JSONObject data = new JSONObject();
		data.put("username", URLDecoder.decode(cropParametersMap.get(CropParameterEnum.GIGWA_USERNAME.getKey()).getValue(), UTF_8));
		data.put("password", URLDecoder.decode(cropParametersMap.get(CropParameterEnum.GIGWA_PASSWORD.getKey()).getEncryptedValue(), UTF_8));
		return new HttpEntity<>(data.toString(), headers);
	}
}
