package org.ibp.api.vsni.oauth;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.vsni.VSNIContextHolder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

public class CustomOAuth2RestTemplate extends OAuth2RestTemplate {

	public CustomOAuth2RestTemplate(final OAuth2ProtectedResourceDetails resource) {
		super(resource);
	}

	@Override
	protected OAuth2AccessToken acquireAccessToken(final OAuth2ClientContext oauth2Context) throws UserRedirectRequiredException {
		final OAuth2AccessToken oAuth2AccessToken = super.acquireAccessToken(oauth2Context);

		final Map<String, Object> claims = this.getJWTClaims(oAuth2AccessToken.getValue());
		final String userId = Optional.ofNullable((String) claims.get("sub"))
			.orElseThrow(() -> new MiddlewareException("Claim 'subject' was not present in JWT token."));
		VSNIContextHolder.setUserId(userId);

		return oAuth2AccessToken;
	}

	private Map<String, Object> getJWTClaims(final String accessToken) {
		try {
			final JWT jwt = JWTParser.parse(accessToken);
			final Payload payload = ((SignedJWT) jwt).getPayload();
			return payload.toJSONObject();
		}
		catch (ParseException e) {
			this.getOAuth2ClientContext().setAccessToken(null);
			throw new IllegalStateException("Unable to parse jwt access token. Error: " + e.getMessage());
		}
	}

}
