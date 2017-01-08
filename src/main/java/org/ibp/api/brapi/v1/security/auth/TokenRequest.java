
package org.ibp.api.brapi.v1.security.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenRequest {

	@JsonProperty("grant_type")
	private String grantType;

	private String username;

	private String password;

	@JsonProperty("client_id")
	private String clientId;

	/**
	 * @return the grantType
	 */
	@JsonProperty("grant_type")
	public String getGrantType() {
		return this.grantType;
	}

	/**
	 * @param grantType the grantType to set
	 */
	@JsonProperty("grant_type")
	public void setGrantType(final String grantType) {
		this.grantType = grantType;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * @return the clientId
	 */
	@JsonProperty("client_id")
	public String getClientId() {
		return this.clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	@JsonProperty("client_id")
	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}

}
