package org.ibp.api.security.oauth;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiParam;
import org.ibp.api.exception.ApiRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

/*
 * TODO
 *  - does spring-security 5 Authorization server has auto-configuration for this?
 */
@RestController
public class OAuthController {

	@Value("${baseUrl}")
	private String baseUrl;

	@RequestMapping(value = "/{cropName}/.well-known/openid-configuration", method = RequestMethod.GET)
	@ResponseBody
	public Map getOpenidConfiguration(@ApiParam("Ignored, for tools that include crop in the url") @PathVariable final String cropName) {
		return this.getOpenidConfigurationMap();
	}

	@RequestMapping(value = "/.well-known/openid-configuration", method = RequestMethod.GET)
	@ResponseBody
	public Map getOpenidConfiguration2() {
		return this.getOpenidConfigurationMap();
	}

	private ImmutableMap<Object, Object> getOpenidConfigurationMap() {
		if (isBlank(this.baseUrl)) {
			throw new ApiRuntimeException("baseUrl property not configured");
		}
		return ImmutableMap.builder()
			.put("issuer", "")
			// TODO is there a dynamic way? or redirect?
			.put("authorization_endpoint", this.baseUrl + "/ibpworkbench/controller/auth/login")
			.put("jwks_uri", "")
			.put("token_endpoint", "")
			.put("grant_types_supported", Collections.singletonList("implicit"))
			.put("response_types_supported", Collections.singletonList("token"))
			.put("subject_types_supported", Collections.singletonList("public"))
			.put("id_token_signing_alg_values_supported", Collections.emptyList())
			.build();
	}
}
