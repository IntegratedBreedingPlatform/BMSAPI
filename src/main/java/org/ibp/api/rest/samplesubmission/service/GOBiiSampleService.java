package org.ibp.api.rest.samplesubmission.service;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.domain.sample.GOBiiSampleList;

public interface GOBiiSampleService {

	GOBiiSampleList postGOBiiSampleList(GOBiiToken goBiiToken, GOBiiSampleList goBiiSampleList);

}
