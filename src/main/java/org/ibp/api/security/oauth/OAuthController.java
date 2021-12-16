package org.ibp.api.security.oauth;

import org.ibp.api.exception.ApiRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
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

	@RequestMapping(value = "/{cropName}/brapi/v2/.well-known/openid-configuration", method = RequestMethod.GET)
	@ResponseBody
	public Map getOpenidConfigurationWithCropNameAndBrapiVersion(@PathVariable final String cropName) {
		final Map<Object, Object> openIdConfiguration = this.getOpenidConfigurationMap();
		openIdConfiguration.putIfAbsent("issuer", this.baseUrl + "/bmsapi/" + cropName + "/brapi/v2");
		return Collections.unmodifiableMap(openIdConfiguration);
	}

	@RequestMapping(value = "/{cropName}/.well-known/openid-configuration", method = RequestMethod.GET)
	@ResponseBody
	public Map getOpenidConfiguration(@PathVariable final String cropName) {
		final Map<Object, Object> openIdConfiguration = this.getOpenidConfigurationMap();
		openIdConfiguration.putIfAbsent("issuer", this.baseUrl + "/bmsapi/" + cropName);
		return Collections.unmodifiableMap(openIdConfiguration);
	}

	@RequestMapping(value = "/.well-known/openid-configuration", method = RequestMethod.GET)
	@ResponseBody
	public Map getOpenidConfiguration2() {
		final Map<Object, Object> openIdConfiguration = this.getOpenidConfigurationMap();
		openIdConfiguration.putIfAbsent("issuer", "");
		return Collections.unmodifiableMap(openIdConfiguration);
	}

	private Map<Object, Object> getOpenidConfigurationMap() {
		if (isBlank(this.baseUrl)) {
			throw new ApiRuntimeException("baseUrl property not configured");
		}
		final Map<Object, Object> map = new HashMap<>();
		// TODO is there a dynamic way? or redirect?
		map.put("authorization_endpoint", this.baseUrl + "/ibpworkbench/controller/auth/login");
		map.put("jwks_uri", "");
		map.put("token_endpoint", "");
		map.put("grant_types_supported", Collections.singletonList("implicit"));
		map.put("response_types_supported", Collections.singletonList("token"));
		map.put("subject_types_supported", Collections.singletonList("public"));
		map.put("id_token_signing_alg_values_supported", Collections.emptyList());
		return map;
	}
}
