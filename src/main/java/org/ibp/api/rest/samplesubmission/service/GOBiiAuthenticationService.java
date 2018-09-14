package org.ibp.api.rest.samplesubmission.service;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;

/**
 * Created by clarysabel on 9/13/18.
 */
public interface GOBiiAuthenticationService {

	GOBiiToken authenticate() throws Exception;

}
