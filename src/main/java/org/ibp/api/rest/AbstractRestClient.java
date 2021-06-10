package org.ibp.api.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public abstract class AbstractRestClient {

	@Value("${bmsapi.url}")
	private String apiUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public String getBaseURL() {
		return this.apiUrl;
	}
}
