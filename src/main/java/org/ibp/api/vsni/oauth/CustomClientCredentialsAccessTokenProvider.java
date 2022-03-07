package org.ibp.api.vsni.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.List;

public class CustomClientCredentialsAccessTokenProvider extends ClientCredentialsAccessTokenProvider {

	@Override
	public OAuth2AccessToken obtainAccessToken(final OAuth2ProtectedResourceDetails details, final AccessTokenRequest request)
		throws UserRedirectRequiredException, AccessDeniedException, OAuth2AccessDeniedException {
		CustomClientCredentialsResourceDetails resource = (CustomClientCredentialsResourceDetails) details;
		return retrieveToken(request, resource, getParametersForTokenRequest(resource), new HttpHeaders());
	}

	private MultiValueMap<String, String> getParametersForTokenRequest(CustomClientCredentialsResourceDetails resource) {

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.set("grant_type", "client_credentials");

		resource.getCustomParameters().forEach(form::set);

		if (resource.isScoped()) {

			StringBuilder builder = new StringBuilder();
			List<String> scope = resource.getScope();

			if (scope != null) {
				Iterator<String> scopeIt = scope.iterator();
				while (scopeIt.hasNext()) {
					builder.append(scopeIt.next());
					if (scopeIt.hasNext()) {
						builder.append(' ');
					}
				}
			}

			form.set("scope", builder.toString());
		}

		return form;

	}

}
