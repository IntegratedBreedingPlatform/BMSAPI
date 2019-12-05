package org.ibp.api.rest.samplesubmission.service;

import org.ibp.api.rest.samplesubmission.domain.GOBiiProject;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;

/**
 * Created by clarysabel on 9/13/18.
 */
public interface GOBiiProjectService {

	 Integer postGOBiiProject(GOBiiToken goBiiToken, GOBiiProject goBiiProject);

}
