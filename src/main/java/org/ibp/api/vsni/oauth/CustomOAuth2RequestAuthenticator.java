package org.ibp.api.vsni.oauth;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RequestAuthenticator;
import org.springframework.security.oauth2.client.http.AccessTokenRequiredException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class CustomOAuth2RequestAuthenticator implements OAuth2RequestAuthenticator {

	@Override
	public void authenticate(final OAuth2ProtectedResourceDetails resource, final OAuth2ClientContext clientContext,
		final ClientHttpRequest request) {
		OAuth2AccessToken accessToken = clientContext.getAccessToken();
		if (accessToken == null) {
			throw new AccessTokenRequiredException(resource);
		}
		request.getHeaders().set("Authorization", accessToken.getValue());
	}

}
