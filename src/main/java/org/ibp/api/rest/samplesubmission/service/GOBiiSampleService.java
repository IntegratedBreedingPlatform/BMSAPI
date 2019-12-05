package org.ibp.api.rest.samplesubmission.service;

import org.ibp.api.rest.samplesubmission.domain.GOBiiSampleList;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;

public interface GOBiiSampleService {

	GOBiiSampleList postGOBiiSampleList(GOBiiToken goBiiToken, GOBiiSampleList goBiiSampleList);

}
