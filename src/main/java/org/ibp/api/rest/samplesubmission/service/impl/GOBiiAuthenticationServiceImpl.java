package org.ibp.api.rest.samplesubmission.service.impl;

import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.service.GOBiiAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Created by clarysabel on 9/12/18.
 */
@Service
public class GOBiiAuthenticationServiceImpl implements GOBiiAuthenticationService {

	private static final Logger LOG = LoggerFactory.getLogger(GOBiiAuthenticationServiceImpl.class);

	private final RestOperations restTemplate;

	@Autowired
	private Environment environment;

	private String gobiiURL;

	private String username;

	private String password;

	public GOBiiAuthenticationServiceImpl() {
		// It can be replaced by RestTemplateBuilder when Spring Boot is upgraded
		restTemplate = new RestTemplate();
	}

	@PostConstruct
	public void resolveGobiiURL () {
		gobiiURL = environment.getProperty("gobii.url");
		username = environment.getProperty("gobii.test.username");
		password = environment.getProperty("gobii.test.password");
	}

	public GOBiiToken authenticate() throws Exception {
		LOG.debug("Trying to authenticate user {} with Gobii to obtain a token.");
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Username", username);
			headers.set("X-Password", password);

			HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

			String urlFormat = "%s/gobii-dev/gobii/v1/auth";
			String url = String.format(urlFormat, gobiiURL);

			ResponseEntity<GOBiiToken> apiAuthToken =
					restTemplate.exchange(url, HttpMethod.POST, entity, GOBiiToken.class);

			if (apiAuthToken.getStatusCode().equals(HttpStatus.OK)) {
				LOG.debug("Successfully authenticated and obtained a token from Gobii for user {}.", username);
			} else {
				throw new ApiRuntimeException("Could not connect to GOBii");
			}
			return apiAuthToken.getBody();
		} catch (RestClientException e) {
			LOG.debug("Error encountered while trying authenticate user {} with Gobii to obtain a token: {}", username, e.getMessage());
			throw e;
		}
	}

}
