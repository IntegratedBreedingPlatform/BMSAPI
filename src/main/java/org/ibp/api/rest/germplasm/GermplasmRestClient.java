package org.ibp.api.rest.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.ibp.api.rest.AbstractRestClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class GermplasmRestClient extends AbstractRestClient implements GermplasmAPI {

	@Override
	public ResponseEntity<GermplasmDto> getGermplasmDtoById(final String cropName, final Integer gid, final String programUUID,
		final String token) {

		final HttpHeaders headers = new HttpHeaders();
		headers.set("X-Auth-Token", token);

		final HttpEntity<?> entity = new HttpEntity<>(headers);

		Map<String, Object> urlParams = new HashMap<>();
		urlParams.put("cropName", cropName);
		urlParams.put("gid", gid);

		final UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(this.getBaseURL() + "crops/{cropName}/germplasm/{gid}")
			.queryParam("programUUID", programUUID)
			.buildAndExpand(urlParams);

		return this.getRestTemplate().exchange(
			uriComponents.toUriString(),
			HttpMethod.GET,
			entity,
			GermplasmDto.class);
	}

}
