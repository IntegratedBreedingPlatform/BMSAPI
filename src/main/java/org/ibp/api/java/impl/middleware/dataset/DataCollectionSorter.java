package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.dao.dms.ExperimentDao;
import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class DataCollectionSorter {

	public List<ObservationUnitRow> orderByRange(
		final FieldmapBlockInfo blockInfo, final List<ObservationUnitRow> observationUnitRows) {

		final List<ObservationUnitRow> reorderedObservationUnitRows = new LinkedList<>();
		final int ranges = blockInfo.getRangesInBlock();
		final int columns = blockInfo.getRowsInBlock() / blockInfo.getNumberOfRowsInPlot();

		// we now need to arrange
		// we set it to map first then we iterate now
		final Map<String, List<ObservationUnitRow>> observationUnitRowsGroupMap =
			this.groupObservationUnitRowsByRangeAndColumn(observationUnitRows);

		boolean leftToRight = true;
		for (int y = 1; y <= ranges; y++) {
			if (leftToRight) {
				for (int x = 1; x <= columns; x++) {
					final List<ObservationUnitRow> observationUnitRowsGroup = observationUnitRowsGroupMap.get(coordinateKey(x, y));
					if (observationUnitRowsGroup != null) {
						reorderedObservationUnitRows.addAll(observationUnitRowsGroup);
					}
				}
			} else {
				for (int x = columns; x >= 1; x--) {
					final List<ObservationUnitRow> observationUnitRowsGroup = observationUnitRowsGroupMap.get(coordinateKey(x, y));
					if (observationUnitRowsGroup != null) {
						reorderedObservationUnitRows.addAll(observationUnitRowsGroup);
					}
				}
			}
			leftToRight = !leftToRight;
		}

		return reorderedObservationUnitRows;

	}

	public List<ObservationUnitRow> orderByColumn(
		final FieldmapBlockInfo blockInfo, final List<ObservationUnitRow> observationUnitRows) {

		final List<ObservationUnitRow> reorderedObservationUnitRows = new LinkedList<>();
		final int ranges = blockInfo.getRangesInBlock();
		final int columns = blockInfo.getRowsInBlock() / blockInfo.getNumberOfRowsInPlot();

		// we now need to arrange
		// we set it to map first then we iterate now
		final Map<String, List<ObservationUnitRow>> observationUnitRowsGroupMap =
			this.groupObservationUnitRowsByRangeAndColumn(observationUnitRows);

		boolean downToUp = true;
		for (int x = 1; x <= columns; x++) {
			if (downToUp) {
				for (int y = 0; y <= ranges; y++) {
					final List<ObservationUnitRow> observationUnitRowsGroup = observationUnitRowsGroupMap.get(coordinateKey(x, y));
					if (observationUnitRowsGroup != null) {
						reorderedObservationUnitRows.addAll(observationUnitRowsGroup);
					}
				}
			} else {
				for (int y = ranges; y >= 0; y--) {
					final List<ObservationUnitRow> observationUnitRowsGroup = observationUnitRowsGroupMap.get(coordinateKey(x, y));
					if (observationUnitRowsGroup != null) {
						reorderedObservationUnitRows.addAll(observationUnitRowsGroup);
					}
				}
			}
			downToUp = !downToUp;
		}

		return reorderedObservationUnitRows;

	}

	protected Map<String, List<ObservationUnitRow>> groupObservationUnitRowsByRangeAndColumn(
		final List<ObservationUnitRow> observationUnitRows) {

		final Map<String, List<ObservationUnitRow>> fieldmapExperiments = new HashMap();
		for (final ObservationUnitRow observationUnitRow : observationUnitRows) {
			final String range = observationUnitRow.getVariables().get(ExperimentDao.FIELD_MAP_RANGE).getValue();
			final String column = observationUnitRow.getVariables().get(ExperimentDao.FIELD_MAP_COLUMN).getValue();
			if (range != null && column != null) {
				// Group the ObservationUnitRow by column and range combination key
				final String key = coordinateKey(Integer.valueOf(column), Integer.valueOf(range));
				if (!fieldmapExperiments.containsKey(key)) {
					fieldmapExperiments.put(key, new LinkedList<ObservationUnitRow>());
				}
				fieldmapExperiments.get(key).add(observationUnitRow);
			}

		}
		return fieldmapExperiments;
	}

	protected String coordinateKey(final Integer column, final Integer range) {
		return column.toString() + ":" + range.toString();
	}

}
