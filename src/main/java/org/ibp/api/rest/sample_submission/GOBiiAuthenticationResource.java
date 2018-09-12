package org.ibp.api.rest.sample_submission;

import org.ibp.api.rest.sample_submission.domain.GOBiiToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Created by clarysabel on 9/12/18.
 */
public class GOBiiAuthenticationResource {

	private static final Logger LOG = LoggerFactory.getLogger(GOBiiAuthenticationResource.class);

	private final RestOperations restTemplate;

	GOBiiAuthenticationResource() {
		// It can be replaced by RestTemplateBuilder when Spring Boot is upgraded
		restTemplate = new RestTemplate();
	}

	public GOBiiToken authenticate(final String username, final String password) {
		LOG.debug("Trying to authenticate user {} with Gobii to obtain a token.", username);
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Username", username);
			headers.set("X-Password", password);

			HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

			ResponseEntity<GOBiiToken> apiAuthToken =
					restTemplate.exchange("http://192.168.9.145:8282/gobii-dev/gobii/v1/auth", HttpMethod.POST, entity, GOBiiToken.class);

			if (apiAuthToken != null && apiAuthToken.getBody() != null) {
				LOG.debug("Successfully authenticated and obtained a token from Gobii for user {}.", username);
			}
			return apiAuthToken.getBody();
		} catch (RestClientException e) {
			LOG.debug("Error encountered while trying authenticate user {} with Gobii to obtain a token: {}", username, e.getMessage());
			throw e;
		}
	}

}
