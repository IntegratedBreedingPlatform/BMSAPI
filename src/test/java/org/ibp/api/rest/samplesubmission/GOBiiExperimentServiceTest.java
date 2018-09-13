package org.ibp.api.rest.samplesubmission;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiHeader;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.domain.experiment.GOBiiExperiment;
import org.ibp.api.rest.samplesubmission.domain.experiment.GOBiiExperimentPayload;
import org.ibp.api.rest.samplesubmission.service.impl.GOBiiExperimentServiceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clarysabel on 9/13/18.
 */
@Ignore
public class GOBiiExperimentServiceTest {

	private GOBiiExperimentServiceImpl goBiiExperimentService;

	@Before
	public void before () {
		goBiiExperimentService = new GOBiiExperimentServiceImpl();
	}

	@Test
	public void testPostExperiment() {
		GOBiiToken goBiiToken = new GOBiiToken();
		goBiiToken.setToken("nRsdPHFMXnUsaKKY+ORO7GaO0/EkdW5Ik1qAvVL4ZTY=");

		GOBiiExperimentPayload.ExperimentData data = new GOBiiExperimentPayload.ExperimentData();
		data.setExperimentName("test01-exp-bms04");
		data.setStatusId(1);
		data.setEntityNameType("EXPERIMENT");
		data.setExperimentCode("micod");
		data.setCreatedBy(1);
		data.setModifiedBy(1);
		data.setProjectId(22);

		List<GOBiiExperimentPayload.ExperimentData> dataList = new ArrayList<>();
		dataList.add(data);

		GOBiiExperiment goBiiExperiment = new GOBiiExperiment();
		GOBiiExperimentPayload goBiiProjectPayload = new GOBiiExperimentPayload();
		goBiiProjectPayload.setData(dataList);

		GOBiiHeader goBiiHeader = new GOBiiHeader();
		goBiiHeader.setGobiiProcessType("CREATE");

		goBiiExperiment.setPayload(goBiiProjectPayload);
		goBiiExperiment.setHeader(goBiiHeader);

		goBiiExperimentService.postGOBiiExperiment(goBiiToken, goBiiExperiment);
	}

}
