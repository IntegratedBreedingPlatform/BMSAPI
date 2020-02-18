package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.impl.middleware.study.FieldMapService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class DatasetCollectionOrderServiceImplTest {

	@Mock
	private FieldMapService fieldMapService;

	@Mock
	private DataCollectionSorter dataCollectionSorter;

	@InjectMocks
	private DatasetCollectionOrderServiceImpl datasetCollectionOrderService;

	@Test
	public void testReorderForPLOT_ORDER() {
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap = this.createStudyInstanceMap();
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = this.createObservationUnitRowMap();

		this.datasetCollectionOrderService.reorder(DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER,
			1, selectedDatasetInstancesMap, observationUnitRowMap);

		Mockito.verify(this.fieldMapService, Mockito.times(2)).getBlockId(anyInt(), anyInt());
		Mockito.verify(this.fieldMapService, Mockito.never()).getBlockInformation(anyInt());
		Mockito.verify(this.dataCollectionSorter, Mockito.never()).orderByRange(any(FieldmapBlockInfo.class), any(ArrayList.class));
		Mockito.verify(this.dataCollectionSorter, Mockito.never()).orderByColumn(any(FieldmapBlockInfo.class), any(ArrayList.class));
	}

	@Test
	public void testReorderForSERPENTINE_ALONG_ROWS() {
		Mockito.when(this.fieldMapService.getBlockId(anyInt(), anyInt())).thenReturn("1");
		final FieldmapBlockInfo fieldmapBlockInfo = Mockito.mock(FieldmapBlockInfo.class);
		Mockito.when(this.fieldMapService.getBlockInformation(anyInt())).thenReturn(fieldmapBlockInfo);

		final Map<Integer, StudyInstance> selectedDatasetInstancesMap = this.createStudyInstanceMap();
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = this.createObservationUnitRowMap();

		this.datasetCollectionOrderService.reorder(DatasetCollectionOrderServiceImpl.CollectionOrder.SERPENTINE_ALONG_ROWS,
			1, selectedDatasetInstancesMap, observationUnitRowMap);

		Mockito.verify(this.fieldMapService, Mockito.times(2)).getBlockId(anyInt(), anyInt());
		Mockito.verify(this.fieldMapService, Mockito.times(2)).getBlockInformation(anyInt());
		Mockito.verify(this.dataCollectionSorter, Mockito.times(2)).orderByRange(eq(fieldmapBlockInfo), any(ArrayList.class));
		Mockito.verify(this.dataCollectionSorter, Mockito.never()).orderByColumn(eq(fieldmapBlockInfo), any(ArrayList.class));
	}

	@Test
	public void testReorderForSERPENTINE_ALONG_COLUMNS() {
		Mockito.when(this.fieldMapService.getBlockId(anyInt(), anyInt())).thenReturn("1");
		final FieldmapBlockInfo fieldmapBlockInfo = Mockito.mock(FieldmapBlockInfo.class);
		Mockito.when(this.fieldMapService.getBlockInformation(anyInt())).thenReturn(fieldmapBlockInfo);

		final Map<Integer, StudyInstance> selectedDatasetInstancesMap = this.createStudyInstanceMap();
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = this.createObservationUnitRowMap();

		this.datasetCollectionOrderService.reorder(DatasetCollectionOrderServiceImpl.CollectionOrder.SERPENTINE_ALONG_COLUMNS,
			1, selectedDatasetInstancesMap, observationUnitRowMap);

		Mockito.verify(this.fieldMapService, Mockito.times(2)).getBlockId(anyInt(), anyInt());
		Mockito.verify(this.fieldMapService, Mockito.times(2)).getBlockInformation(anyInt());
		Mockito.verify(this.dataCollectionSorter, Mockito.never()).orderByRange(eq(fieldmapBlockInfo), any(ArrayList.class));
		Mockito.verify(this.dataCollectionSorter, Mockito.times(2)).orderByColumn(eq(fieldmapBlockInfo), any(ArrayList.class));
	}

	private Map<Integer, StudyInstance> createStudyInstanceMap() {
		final StudyInstance studyInstance1 = this.createStudyInstance(1);
		final StudyInstance studyInstance2 = this.createStudyInstance(2);
		final Map<Integer, StudyInstance> studyInstanceMap = new HashMap<>();
		studyInstanceMap.put(1, studyInstance1);
		studyInstanceMap.put(2, studyInstance2);
		return studyInstanceMap;
	}

	private StudyInstance createStudyInstance(final Integer instanceId) {
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setExperimentId(instanceId);
		studyInstance.setInstanceNumber(instanceId);
		studyInstance.setLocationName("LOC - " + instanceId);
		return studyInstance;
	}

	private Map<Integer, List<ObservationUnitRow>> createObservationUnitRowMap() {
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = new HashMap<>();
		observationUnitRowMap.put(1, new ArrayList<ObservationUnitRow>());
		observationUnitRowMap.put(2, new ArrayList<ObservationUnitRow>());
		return  observationUnitRowMap;
	}
}
