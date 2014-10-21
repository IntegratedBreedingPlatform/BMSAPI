package org.generationcp.bms.resource;

import java.util.Arrays;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.StudyDetails;
import org.generationcp.bms.domain.StudySummary;
import org.generationcp.bms.domain.Trait;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StudyResourceTest {
	
	private StudyDataManager studyDataManager;
	private SimpleDao simpleDao;
	private FieldbookService fieldbookService;
	private DataImportService dataImportService;
	
	@Before
	public void beforeEachTest() {
		this.studyDataManager = Mockito.mock(StudyDataManager.class);
		this.simpleDao = Mockito.mock(SimpleDao.class);
		this.fieldbookService = Mockito.mock(FieldbookService.class);
		this.dataImportService = Mockito.mock(DataImportService.class);
	}
		
	@Test(expected = NotFoundException.class)
	public void testGetStudySummaryNotFound() throws MiddlewareQueryException {
		StudyResource resource = new StudyResource(studyDataManager, simpleDao, fieldbookService, dataImportService);
		int studyId = 123;
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(null);
		
		resource.getStudySummary(studyId);
	}
	
	@Test(expected = NotFoundException.class)
	public void testGetStudyDetailsNotFound() throws MiddlewareQueryException {
		StudyResource resource = new StudyResource(studyDataManager, simpleDao, fieldbookService, dataImportService);
		int studyId = 123;
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(null);
		
		resource.getStudyDetails(studyId);
	}
	
	@Test
	public void testGetStudySummaryFound() throws MiddlewareQueryException {
		StudyResource resource = new StudyResource(studyDataManager, simpleDao, fieldbookService, dataImportService);
		int studyId = 123;
		
		Study study = new Study(studyId, new VariableList(), new VariableList());
		
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(study);
		
		StudySummary studySummary = resource.getStudySummary(studyId);
		Assert.assertNotNull(studySummary);
		Assert.assertEquals(study.getId(), studySummary.getId());
	}
	
	@Test
	public void testGetStudyDetailsFound() throws MiddlewareQueryException {
		StudyResource resource = new StudyResource(studyDataManager, simpleDao, fieldbookService, dataImportService);
		int studyId = 123;
		
		Study study = new Study(studyId, new VariableList(), new VariableList());
		
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(study);
		Mockito.when(studyDataManager.getAllStudyFactors(studyId)).thenReturn(new VariableTypeList());
		Mockito.when(simpleDao.getMeasuredTraitsForStudy(studyId)).thenReturn(Arrays.asList(new Trait(1)));
		
		StudyDetails studyDetails = resource.getStudyDetails(studyId);
		Assert.assertNotNull(studyDetails);
		Assert.assertEquals(study.getId(), studyDetails.getId());
		Assert.assertTrue(studyDetails.getMeasuredTraits().size() == 1);
		
	}
}
