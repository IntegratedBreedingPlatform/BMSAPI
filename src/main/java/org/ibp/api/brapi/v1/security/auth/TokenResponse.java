
package org.ibp.api.brapi.v1.security.auth;

import org.ibp.api.brapi.v1.common.Metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"metadata", "userDisplayName", "access_token", "expires_in"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

	private final Metadata metadata;

	private final String userDisplayName;

	@JsonProperty("access_token")
	private final String accessToken;

	@JsonProperty("expires_in")
	private final long expiresIn;

	/**
	 *
	 * @param metadata
	 */
	public TokenResponse(final Metadata metadata, final String userDisplayName, final String accessToken, final long expiresIn) {
		this.metadata = metadata;
		this.userDisplayName = userDisplayName;
		this.accessToken = accessToken;
		this.expiresIn = expiresIn;
	}

	/**
	 *
	 * @return The metadata
	 */
	public Metadata getMetadata() {
		return this.metadata;
	}

	/**
	 * @return the userDisplayName
	 */
	public String getUserDisplayName() {
		return this.userDisplayName;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return this.accessToken;
	}

	/**
	 * @return the expiresIn
	 */
	public long getExpiresIn() {
		return this.expiresIn;
	}
}
