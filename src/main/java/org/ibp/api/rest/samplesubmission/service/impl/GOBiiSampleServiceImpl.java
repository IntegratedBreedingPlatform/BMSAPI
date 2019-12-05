package org.ibp.api.rest.samplesubmission.service.impl;

import org.ibp.api.rest.samplesubmission.domain.GOBiiSampleList;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.service.GOBiiSampleService;
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

@Service
public class GOBiiSampleServiceImpl implements GOBiiSampleService {

	private static final Logger LOG = LoggerFactory.getLogger(GOBiiProjectServiceImpl.class);

	private final RestTemplate restTemplate;

	@Autowired
	private Environment environment;

	private String gobiiURL;

	public GOBiiSampleServiceImpl() {
		// It can be replaced by RestTemplateBuilder when Spring Boot is upgraded
		restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

	}


	@PostConstruct
	public void resolveGobiiURL () {
		gobiiURL = environment.getProperty("gobii.url");
	}

	@Override
	public GOBiiSampleList postGOBiiSampleList(
		final GOBiiToken goBiiToken, final GOBiiSampleList goBiiSampleList) {
		LOG.debug("Trying to post sample list for project {} to GOBii", goBiiSampleList.getProjectId());
		try {

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Content-Type", "application/json");
			headers.add("X-Auth-Token", goBiiToken.getToken());

			HttpEntity<GOBiiSampleList> entity = new HttpEntity<>(goBiiSampleList, headers);

			String urlFormat = "%s/gobii-dev/sample-tracking/v1/samples";
			String url = String.format(urlFormat, gobiiURL);

			ResponseEntity<GOBiiSampleList> response = restTemplate
				.exchange(url, HttpMethod.POST, entity,
					GOBiiSampleList.class);

			if (response.getStatusCode().equals(HttpStatus.CREATED)) {
				return response.getBody();
			} else {
				return null;
			}

		} catch (RestClientException e) {
			LOG.debug("Error encountered while trying to post sample list for project {} to GOBii",  goBiiSampleList.getProjectId(), e.getMessage());
			throw e;
		}
	}
}
