package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StudyResourceBrapiTest extends ApiUnitTestBase {

	@SuppressWarnings("unused") // temporary
	@Autowired
	private StudyDataManager studyDataManager;

	@SuppressWarnings("unused") // temporary
	@Autowired
	private StudyService studyServiceMW;

	@Test
	public void testListStudySummaries() throws Exception {

		// TODO with StudyResourceBrapi implementation
	}


	@Test
	public void testGetStudyObservationsAsTable() throws Exception {

		// TODO with StudyResourceBrapi implementation
	}

}
