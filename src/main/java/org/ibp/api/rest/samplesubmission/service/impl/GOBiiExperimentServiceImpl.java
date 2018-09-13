package org.ibp.api.rest.samplesubmission.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.domain.experiment.GOBiiExperiment;
import org.ibp.api.rest.samplesubmission.service.GOBiiExperimentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by clarysabel on 9/13/18.
 */
@Service
public class GOBiiExperimentServiceImpl implements GOBiiExperimentService{

	private static final Logger LOG = LoggerFactory.getLogger(GOBiiExperimentServiceImpl.class);

	private final RestTemplate restTemplate;

	public GOBiiExperimentServiceImpl() {
		restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}

	public Integer postGOBiiExperiment(final GOBiiToken goBiiToken, final GOBiiExperiment goBiiExperiment) {
		LOG.debug("Trying to post experiment {} to GOBii", goBiiExperiment.getPayload().getData().get(0).getExperimentId());
		try {

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Content-Type", "application/json");
			headers.add("X-Auth-Token", goBiiToken.getToken());

			HttpEntity<GOBiiExperiment> entity = new HttpEntity<>(goBiiExperiment, headers);

			ResponseEntity<GOBiiExperiment> response = restTemplate
					.exchange("http://192.168.9.145:8282/gobii-dev/gobii/v1/experiments", HttpMethod.POST, entity,
							GOBiiExperiment.class);

			if (response.getStatusCode().equals(HttpStatus.CREATED)) {
				return response.getBody().getPayload().getData().get(0).getId();
			} else {
				return null;
			}

		} catch (RestClientException e) {
			LOG.debug("Error encountered while trying to post experiment {} to GOBii", goBiiExperiment.getPayload().getData().get(0).getExperimentId(), e.getMessage());
			throw e;
		}
	}

}
