package org.ibp.api.rest.sample_submission;

import org.ibp.api.rest.sample_submission.domain.GOBiiProject;
import org.ibp.api.rest.sample_submission.domain.GOBiiToken;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Created by clarysabel on 9/12/18.
 */
public class GOBiiProjectResource {

	private final RestOperations restTemplate;

	GOBiiProjectResource() {
		// It can be replaced by RestTemplateBuilder when Spring Boot is upgraded
		restTemplate = new RestTemplate();
	}

	public Integer postGOBiiProject (final GOBiiToken goBiiToken, final GOBiiProject goBiiProject) {
		return null;
	}

}
