package org.ibp.api.java.impl.middleware.study;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StudyDatasetServiceImplTest {
	
	@Mock
	private org.generationcp.middleware.service.api.study.StudyDatasetService middlewareDatasetService;
	
	@InjectMocks
	private StudyDatasetServiceImpl studyDatasetService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCountPhenotypesForDataset() {
		final int datasetId = 123;
		final List<Integer> traitIds = Arrays.asList(1,2,3);
		this.studyDatasetService.countPhenotypesForDataset(datasetId, traitIds);
		Mockito.verify(this.middlewareDatasetService).countPhenotypesForDataset(datasetId, traitIds);
	}
	
	@Test
	public void testDatasetExists() {
		final int studyId = 250;
		final int datasetId = 252;
		this.studyDatasetService.datasetExists(studyId, datasetId);
		Mockito.verify(this.middlewareDatasetService).datasetExists(studyId, datasetId);
	}

}
