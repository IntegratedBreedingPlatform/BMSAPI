package org.ibp.api.vsni.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RequestAuthenticator;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OAuthConfiguration {

	@Value("${spring.security.oauth2.client.registration.vsni.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.vsni.client-secret}")
	private String clientSecret;

	@Value("${spring.security.oauth2.client.registration.vsni.token-uri}")
	private String tokenUri;

	@Value("${spring.security.oauth2.client.registration.vsni.audience}")
	private String audience;

	@Bean
	public OAuth2RestTemplate oAuth2RestTemplate() {
		final OAuth2RestTemplate restTemplate = new CustomOAuth2RestTemplate(this.clientCredentialsResourceDetails());
		restTemplate.setAccessTokenProvider(this.clientCredentialsAccessTokenProvider());
		restTemplate.setRequestFactory(this.simpleClientHttpRequestFactory());
		restTemplate.setAuthenticator(this.oAuth2RequestAuthenticator());
		return restTemplate;
	}

	@Bean
	public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setOutputStreaming(false);
		return requestFactory;
	}

	private ClientCredentialsResourceDetails clientCredentialsResourceDetails() {
		final Map<String, String> customParameters = new HashMap<>();
		customParameters.put("audience", this.audience);

		final CustomClientCredentialsResourceDetails clientCredentialsResourceDetails = new CustomClientCredentialsResourceDetails(customParameters);
		clientCredentialsResourceDetails.setClientId(this.clientId);
		clientCredentialsResourceDetails.setClientSecret(this.clientSecret);
		clientCredentialsResourceDetails.setAccessTokenUri(this.tokenUri);
		return clientCredentialsResourceDetails;
	}

	private ClientCredentialsAccessTokenProvider clientCredentialsAccessTokenProvider() {
		final CustomClientCredentialsAccessTokenProvider tokenProvider =
			new CustomClientCredentialsAccessTokenProvider();
		return tokenProvider;
	}

	private OAuth2RequestAuthenticator oAuth2RequestAuthenticator() {
		return new CustomOAuth2RequestAuthenticator();
	}

}
