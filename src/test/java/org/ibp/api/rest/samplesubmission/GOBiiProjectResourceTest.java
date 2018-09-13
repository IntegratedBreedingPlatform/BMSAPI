package org.ibp.api.rest.samplesubmission;

import java.util.List;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiHeader;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.domain.project.GOBiiProject;
import org.ibp.api.rest.samplesubmission.domain.project.GOBiiProjectPayload;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by clarysabel on 9/12/18.
 */
@Ignore
public class GOBiiProjectResourceTest {

	private GOBiiProjectService goBiiProjectResource;

	@Before
	public void before() {
		goBiiProjectResource = new GOBiiProjectService();
	}

	@Test
	public void postProjectTest () {
		GOBiiToken goBiiToken = new GOBiiToken();
		goBiiToken.setToken("X62NWlE/WzmNt0hJbkIHLWUfMnj0FGxW2u9B6n7kcWQ=");

		GOBiiProjectPayload.ProjectData data = new GOBiiProjectPayload.ProjectData();
		data.setPiContact(1);
		data.setProjectName("test01-nahcla");
		data.setProjectStatus(1);
		data.setEntityNameType("PROJECT");
		data.setProjectCode("micod");
		data.setCreatedBy(1);
		data.setModifiedBy(1);

		List<GOBiiProjectPayload.ProjectData> dataList = new ArrayList<>();
		dataList.add(data);

		GOBiiProject goBiiProject = new GOBiiProject();
		GOBiiProjectPayload goBiiProjectPayload = new GOBiiProjectPayload();
		goBiiProjectPayload.setData(dataList);

		GOBiiHeader goBiiHeader = new GOBiiHeader();
		goBiiHeader.setGobiiProcessType("CREATE");

		goBiiProject.setPayload(goBiiProjectPayload);
		goBiiProject.setHeader(goBiiHeader);

		goBiiProjectResource.postGOBiiProject(goBiiToken, goBiiProject);
	}

}
