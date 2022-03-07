package org.ibp.api.vsni.oauth;

import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.HashMap;
import java.util.Map;

public class CustomClientCredentialsResourceDetails extends ClientCredentialsResourceDetails {

	private final Map<String, String> customParameters = new HashMap<>();

	public CustomClientCredentialsResourceDetails(final Map<String, String> customParameters) {
		this.customParameters.putAll(customParameters);
	}

	public Map<String, String> getCustomParameters() {
		return customParameters;
	}
}
