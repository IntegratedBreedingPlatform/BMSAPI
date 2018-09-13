package org.ibp.api.rest.samplesubmission.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ibp.api.rest.samplesubmission.domain.project.GOBiiProject;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.service.GOBiiProjectService;
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
 * Created by clarysabel on 9/12/18.
 */
@Service
public class GOBiiProjectServiceImpl implements GOBiiProjectService{

	private static final Logger LOG = LoggerFactory.getLogger(GOBiiProjectServiceImpl.class);

	private final RestTemplate restTemplate;

	public GOBiiProjectServiceImpl() {
		// It can be replaced by RestTemplateBuilder when Spring Boot is upgraded
		restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

	}

	public Integer postGOBiiProject(final GOBiiToken goBiiToken, final GOBiiProject goBiiProject) {
		LOG.debug("Trying to post project {} to GOBii", goBiiProject.getPayload().getData().get(0).getProjectId());
		try {

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Content-Type", "application/json");
			headers.add("X-Auth-Token", goBiiToken.getToken());

			HttpEntity<GOBiiProject> entity = new HttpEntity<>(goBiiProject, headers);

			ResponseEntity<GOBiiProject> response = restTemplate
					.exchange("http://192.168.9.145:8282/gobii-dev/gobii/v1/projects", HttpMethod.POST, entity,
							GOBiiProject.class);

			if (response.getStatusCode().equals(HttpStatus.CREATED)) {
				return response.getBody().getPayload().getData().get(0).getId();
			} else {
				return null;
			}

		} catch (RestClientException e) {
			LOG.debug("Error encountered while trying to post project {} to GOBii", goBiiProject.getPayload().getData().get(0).getProjectId(), e.getMessage());
			throw e;
		}
	}

}
