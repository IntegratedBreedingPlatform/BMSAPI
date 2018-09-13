package org.ibp.api.rest.samplesubmission.service;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.domain.project.GOBiiProject;

/**
 * Created by clarysabel on 9/13/18.
 */
public interface GOBiiProjectService {

	 Integer postGOBiiProject(GOBiiToken goBiiToken, GOBiiProject goBiiProject);

}
