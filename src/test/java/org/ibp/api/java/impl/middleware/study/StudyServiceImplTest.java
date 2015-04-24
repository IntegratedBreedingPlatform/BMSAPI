
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.domain.study.StudySummary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StudyServiceImplTest {

	private StudyServiceImpl studyServiceImpl;

	@Mock
	private StudyService mockMiddlewareStudyService;

	private final String programUID = UUID.randomUUID().toString();

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		studyServiceImpl = new StudyServiceImpl();
		studyServiceImpl.setMiddlewareStudyService(this.mockMiddlewareStudyService);
	}

	@Test
	public void listAllStudies() throws MiddlewareQueryException {

		List<org.generationcp.middleware.service.api.study.StudySummary> mockResult = new ArrayList<>();
		org.generationcp.middleware.service.api.study.StudySummary studySummary = new org.generationcp.middleware.service.api.study.StudySummary();
		studySummary.setId(1);
		studySummary.setName("Study Name");
		studySummary.setObjective("Study Objective");
		studySummary.setTitle("Study Title");
		studySummary.setProgramUUID(programUID);
		studySummary.setStartDate("2015-01-01");
		studySummary.setEndDate("2015-12-31");
		studySummary.setType(StudyType.T);

		mockResult.add(studySummary);
		Mockito.when(mockMiddlewareStudyService.listAllStudies(programUID)).thenReturn(mockResult);

		List<StudySummary> studySummaries = studyServiceImpl.listAllStudies(programUID);
		Assert.assertEquals(mockResult.size(), studySummaries.size());
		Assert.assertEquals(studySummary.getId(), studySummaries.get(0).getId());
		Assert.assertEquals(studySummary.getName(), studySummaries.get(0).getName());
		Assert.assertEquals(studySummary.getTitle(), studySummaries.get(0).getTitle());
		Assert.assertEquals(studySummary.getObjective(), studySummaries.get(0).getObjective());
		Assert.assertEquals(studySummary.getStartDate(), studySummaries.get(0).getStartDate());
		Assert.assertEquals(studySummary.getEndDate(), studySummaries.get(0).getEndDate());
		Assert.assertEquals(studySummary.getType().toString(), studySummaries.get(0).getType());

	}
}
