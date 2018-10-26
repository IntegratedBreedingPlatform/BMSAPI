package org.ibp.api.java.impl.middleware.dataset;

import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.DatasetServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DatasetServiceImplTest {
	
	@Mock
	private DatasetService middlewareDatasetService;
	
	@InjectMocks
	private DatasetServiceImpl studyDatasetService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCountPhenotypesForDataset() {
		final int datasetId = 123;
		final List<Integer> traitIds = Arrays.asList(1,2,3);
		this.studyDatasetService.countPhenotypesForDataset(datasetId, traitIds);
		Mockito.verify(this.middlewareDatasetService).countPhenotypes(datasetId, traitIds);
	}
	
}
