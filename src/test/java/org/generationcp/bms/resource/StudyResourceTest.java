package org.generationcp.bms.resource;

import org.generationcp.bms.domain.StudySummary;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StudyResourceTest {
	
	private StudyDataManager studyDataManager;
	
	@Before
	public void beforeEachTest() {
		studyDataManager = Mockito.mock(StudyDataManager.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateFailure() {
		new StudyResource(null);
	}
	
	@Test
	public void testCreateSuccess() {
		new StudyResource(studyDataManager);
	}
	
	@Test(expected = NotFoundException.class)
	public void testGetStudySummaryNotFound() throws MiddlewareQueryException {
		StudyResource resource = new StudyResource(studyDataManager);
		int studyId = 123;
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(null);
		
		resource.getStudySummary(studyId);
	}
	
	@Test
	public void testGetStudySummaryFound() throws MiddlewareQueryException {
		StudyResource resource = new StudyResource(studyDataManager);
		int studyId = 123;
		
		Study study = new Study(studyId, new VariableList(), new VariableList());
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(study);
		
		StudySummary studySummary = resource.getStudySummary(studyId);
		Assert.assertNotNull(studySummary);
		Assert.assertEquals(study.getId(), studySummary.getId());
	}
}
