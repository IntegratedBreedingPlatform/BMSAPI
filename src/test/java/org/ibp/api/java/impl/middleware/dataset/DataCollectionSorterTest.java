package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.dao.dms.ExperimentDao;
import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DataCollectionSorterTest {

	private final DataCollectionSorter dataCollectionSorter = new DataCollectionSorter();

	@Test
	public void testOrderByColumn() {

		final List<ObservationUnitRow> observationUnitRows = this.createObservationUnitRows();
		final FieldmapBlockInfo fieldmapBlockInfo = new FieldmapBlockInfo(0, 0, 0, 0, false);
		fieldmapBlockInfo.setRangesInBlock(5);
		fieldmapBlockInfo.setRowsInBlock(2);
		fieldmapBlockInfo.setNumberOfRowsInPlot(1);

		final List<ObservationUnitRow> result =
			dataCollectionSorter.orderByColumn(fieldmapBlockInfo, observationUnitRows);

		final Map<String, List<ObservationUnitRow>> groupResult =
			dataCollectionSorter.groupObservationUnitRowsByRangeAndColumn(result);

		assertEquals("[1:1, 1:2, 2:1, 1:3, 2:2, 1:4, 2:3, 1:5, 2:4, 2:5]", groupResult.keySet().toString());

	}

	@Test
	public void testOrderByRange() {

		final List<ObservationUnitRow> observationUnitRows = this.createObservationUnitRows();
		final FieldmapBlockInfo fieldmapBlockInfo = new FieldmapBlockInfo(0, 0, 0, 0, false);
		fieldmapBlockInfo.setRangesInBlock(5);
		fieldmapBlockInfo.setRowsInBlock(2);
		fieldmapBlockInfo.setNumberOfRowsInPlot(1);

		final List<ObservationUnitRow> result =
			dataCollectionSorter.orderByRange(fieldmapBlockInfo, observationUnitRows);

		final Map<String, List<ObservationUnitRow>> groupResult =
			dataCollectionSorter.groupObservationUnitRowsByRangeAndColumn(result);

		assertEquals("[1:1, 2:1, 1:2, 2:2, 1:3, 2:3, 1:4, 2:4, 1:5, 2:5]", groupResult.keySet().toString());

	}

	@Test
	public void testGroupObservationUnitRowsByRangeAndColumn() {

		final List<ObservationUnitRow> observationUnitRows = this.createObservationUnitRows();
		final Map<String, List<ObservationUnitRow>> result =
			dataCollectionSorter.groupObservationUnitRowsByRangeAndColumn(observationUnitRows);

		assertEquals(10, result.size());
		assertEquals("[1:1, 1:2, 2:1, 1:3, 2:2, 1:4, 2:3, 1:5, 2:4, 2:5]", result.keySet().toString());

		for (final List<ObservationUnitRow> entryItem : result.values()) {
			assertEquals(2, entryItem.size());
		}
	}

	private List<ObservationUnitRow> createObservationUnitRows() {

		final List<ObservationUnitRow> observationUnitRows = new LinkedList<>();
		observationUnitRows.addAll(createObservationUnitRowsGroup(1, 5, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(2, 5, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(1, 4, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(2, 4, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(1, 3, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(2, 3, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(1, 2, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(2, 2, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(1, 1, 2));
		observationUnitRows.addAll(createObservationUnitRowsGroup(2, 1, 2));

		return observationUnitRows;

	}

	private List<ObservationUnitRow> createObservationUnitRowsGroup(final int column, final int range, final int noOfItems) {

		final List<ObservationUnitRow> observationUnitRows = new LinkedList<>();

		for (int i = 1; i <= noOfItems; i++) {
			final ObservationUnitData fieldMapRangeObservationData = new ObservationUnitData();
			fieldMapRangeObservationData.setValue(String.valueOf(range));
			final ObservationUnitData fieldMapColumnObservationData = new ObservationUnitData();
			fieldMapColumnObservationData.setValue(String.valueOf(column));

			final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
			final Map<String, ObservationUnitData> variables = new HashMap<>();
			variables.put(ExperimentDao.FIELD_MAP_RANGE, fieldMapRangeObservationData);
			variables.put(ExperimentDao.FIELD_MAP_COLUMN, fieldMapColumnObservationData);
			observationUnitRow.setVariables(variables);
			observationUnitRows.add(observationUnitRow);
		}
		return observationUnitRows;
	}

	@Test
	public void testCoordinateKey() {

		assertEquals("1:2", dataCollectionSorter.coordinateKey(1, 2));

	}

}
