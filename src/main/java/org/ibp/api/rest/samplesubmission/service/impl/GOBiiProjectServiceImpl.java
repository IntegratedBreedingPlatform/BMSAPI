package org.ibp.api.rest.samplesubmission.service.impl;

import org.ibp.api.rest.samplesubmission.domain.GOBiiProject;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.service.GOBiiProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

import javax.annotation.PostConstruct;

/**
 * Created by clarysabel on 9/12/18.
 */
@Service
public class GOBiiProjectServiceImpl implements GOBiiProjectService{

	private static final Logger LOG = LoggerFactory.getLogger(GOBiiProjectServiceImpl.class);

	private final RestTemplate restTemplate;

	@Autowired
	private Environment environment;

	private String gobiiURL;

	public GOBiiProjectServiceImpl() {
		// It can be replaced by RestTemplateBuilder when Spring Boot is upgraded
		restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

	}

	@PostConstruct
	public void resolveGobiiURL () {
		gobiiURL = environment.getProperty("gobii.url");
	}

	public Integer postGOBiiProject(final GOBiiToken goBiiToken, final GOBiiProject goBiiProject) {
		LOG.debug("Trying to post project {} to GOBii", goBiiProject.getProjectName());
		try {

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Content-Type", "application/json");
			headers.add("X-Auth-Token", goBiiToken.getToken());

			HttpEntity<GOBiiProject> entity = new HttpEntity<>(goBiiProject, headers);

			String urlFormat = "%s/gobii-dev/sample-tracking/v1/projects";
			String url = String.format(urlFormat, gobiiURL);

			ResponseEntity<GOBiiProject> response = restTemplate
					.exchange(url, HttpMethod.POST, entity,
						GOBiiProject.class);

			if (response.getStatusCode().equals(HttpStatus.CREATED)) {
				return response.getBody().getProjectId();
			} else {
				return null;
			}

		} catch (RestClientException e) {
			LOG.debug("Error encountered while trying to post project {} to GOBii", goBiiProject.getProjectName(), e.getMessage());
			throw e;
		}
	}

}
