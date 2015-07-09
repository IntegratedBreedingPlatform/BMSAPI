
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.DefaultedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.FieldMapMetaData;
import org.ibp.api.domain.study.FieldMapPlantingDetails;
import org.ibp.api.domain.study.FieldMapStudySummary;
import org.ibp.api.domain.study.FieldPlot;
import org.ibp.api.exception.ApiRuntimeException;

import com.google.common.collect.Lists;

public class FieldMapService {

	private StudyDataManager studyDataManager;

	public FieldMapService(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	public Map<Integer, FieldMap> getFieldMap(String studyId) {

		final Map<Integer, FieldMap> fieldMaps = this.getDefaultFieldMap();
		final StudyFieldMap rawDataFromMiddleware = this.getRawDataFromMiddleware(studyId);

		final FieldMapInfo fieldMapInfo = rawDataFromMiddleware.getMiddlewareFieldMapInfo();

		if (fieldMapInfo == null) {
			return fieldMaps;
		}

		this.populateStudyInformation(fieldMaps, rawDataFromMiddleware);

		this.extractRangesFromPlotData(fieldMaps);

		return fieldMaps;
	}

	private void extractRangesFromPlotData(Map<Integer, FieldMap> fieldMaps) {
		Collection<FieldMap> createdFieldMaps = fieldMaps.values();
		for (FieldMap fieldMap : createdFieldMaps) {
			final FieldPlot[][] fieldPlots = fieldMap.getPlots();

			for (int column = 0; column < fieldPlots.length; column++) {
				for (int range = 0; range < fieldPlots[column].length; range++) {
					FieldPlot fieldPlot = fieldPlots[column][range];

					this.addToList(this.getDefaultList(fieldMap.getRange()), range + 1, fieldPlot);

					this.addToList(this.getDefaultList(fieldMap.getColumns()), column + 1, fieldPlot);

				}
			}
		}
	}

	private void addToList(final Map<Integer, List<FieldPlot>> mapToAddTo, int range, FieldPlot fieldPlot) {
		List<FieldPlot> rangeList = mapToAddTo.get(range);
		rangeList.add(fieldPlot);
	}

	private Map<Integer, List<FieldPlot>> getDefaultList(final Map<Integer, List<FieldPlot>> rangeMap) {
		return DefaultedMap.defaultedMap(rangeMap, new Transformer<Integer, List<FieldPlot>>() {

			@Override
			public List<FieldPlot> transform(Integer number) {
				final List<FieldPlot> plotList = new ArrayList<>();
				rangeMap.put(number, plotList);
				return plotList;
			}
		});
	}

	private void populateStudyInformation(final Map<Integer, FieldMap> filedMaps, StudyFieldMap rawDataFromMiddleware) {
		for (final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo : rawDataFromMiddleware.getMiddlewareTiralInstances()) {
			if (middlewareFieldMapTrialInstanceInfo.getBlockId() == null) {
				// No field map generated
				continue;
			}

			final FieldMap fieldMap = filedMaps.get(middlewareFieldMapTrialInstanceInfo.getBlockId());
			this.setBlockName(middlewareFieldMapTrialInstanceInfo, fieldMap);

			this.mapPlantingDetails(middlewareFieldMapTrialInstanceInfo, fieldMap.getFieldMapMetaData());

			final FieldMapStudySummary plantingSummary =
					this.mapPlantingSummary(rawDataFromMiddleware, middlewareFieldMapTrialInstanceInfo);

			fieldMap.getFieldMapMetaData().getRelevantStudies().add(plantingSummary);
			this.populateFieldMap(fieldMap, middlewareFieldMapTrialInstanceInfo, rawDataFromMiddleware);
			this.updatePlotsDeleted(fieldMap, middlewareFieldMapTrialInstanceInfo);

		}
	}

	private void updatePlotsDeleted(FieldMap fieldMap, final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo) {

		final List<String> deletedPlots = middlewareFieldMapTrialInstanceInfo.getDeletedPlots();
		final FieldPlot[][] fieldPlots = fieldMap.getPlots();
		for (String deletedPlot : deletedPlots) {
			String[] plots = deletedPlot.split(",");
			// FieldPlot fieldPlot = fieldPlots.get(new FieldCoordinates(Integer.parseInt(plots[0])+1, Integer.parseInt(plots[1])+1));
			int column = Integer.parseInt(plots[0]);
			int range = Integer.parseInt(plots[1]);
			FieldPlot fieldPlot = fieldPlots[column][range];
			fieldPlot.setPlotDeleted(true);
		}

	}

	private void populateFieldMap(final FieldMap fieldMap, final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo,
			final StudyFieldMap rawDataFromMiddleware) {

		this.initFieldPlots(fieldMap, middlewareFieldMapTrialInstanceInfo);
		FieldPlot[][] fieldPlots = fieldMap.getPlots();
		for (FieldMapLabel fieldMapLabel : middlewareFieldMapTrialInstanceInfo.getFieldMapLabels()) {
			fieldPlots[fieldMapLabel.getRange() - 1][fieldMapLabel.getColumn() - 1] =
					this.mapMiddlewareFieldLableToFieldPlot(fieldMapLabel, middlewareFieldMapTrialInstanceInfo, rawDataFromMiddleware);
		}

	}

	private FieldPlot mapMiddlewareFieldLableToFieldPlot(final FieldMapLabel fieldMapLabel,
			FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo, StudyFieldMap rawDataFromMiddleware) {
		final FieldPlot fieldPlot = new FieldPlot();
		fieldPlot.setEntryNumber(fieldMapLabel.getEntryNumber());
		fieldPlot.setObservationUniqueIdentifier(fieldMapLabel.getExperimentId());
		fieldPlot.setPlotNumber(fieldMapLabel.getPlotNo());
		fieldPlot.setRepetitionNumber(fieldMapLabel.getRep());
		fieldPlot.setDatasetId(rawDataFromMiddleware.getFieldmapDataset().getDatasetId());
		fieldPlot.setGeolocationId(middlewareFieldMapTrialInstanceInfo.getGeolocationId());
		return fieldPlot;
	}

	private void initFieldPlots(final FieldMap fieldMap, final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo) {
		if (fieldMap.getPlots() == null) {
			FieldPlot[][] fieldPlots = StudyFieldMapUtility.getDefaultPlots(middlewareFieldMapTrialInstanceInfo);
			fieldMap.setPlots(fieldPlots);
		}
	}

	private void setBlockName(final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo, final FieldMap fieldMap) {
		if (StringUtils.isBlank(fieldMap.getBlockName())) {
			fieldMap.setBlockName(middlewareFieldMapTrialInstanceInfo.getBlockName());
		} else {
			if (!fieldMap.getBlockName().equals(middlewareFieldMapTrialInstanceInfo.getBlockName())) {
				throw new IllegalStateException();
			}
		}
	}

	private FieldMapStudySummary mapPlantingSummary(StudyFieldMap rawDataFromMiddleware,
			final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo) {
		final FieldMapStudySummary plantingSummary = new FieldMapStudySummary();
		plantingSummary.setDataset(rawDataFromMiddleware.getFieldmapDataset().getDatasetName());
		plantingSummary.setEnvironment(NumberUtils.toInt(fieldMapTrialInstanceInfo.getTrialInstanceNo()));
		plantingSummary.setNumbeOfReps(fieldMapTrialInstanceInfo.getRepCount());
		plantingSummary.setNumberOfEntries(fieldMapTrialInstanceInfo.getEntryCount());
		plantingSummary.setOrder(fieldMapTrialInstanceInfo.getOrder());
		plantingSummary.setPlotsNeeded(fieldMapTrialInstanceInfo.getPlotCount());
		plantingSummary.setStudyName(rawDataFromMiddleware.getMiddlewareFieldMapInfo().getFieldbookName());
		plantingSummary.setType(rawDataFromMiddleware.getStudyType().toString());
		return plantingSummary;
	}

	private void mapPlantingDetails(final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo, final FieldMapMetaData fieldMapMetaData) {
		if (fieldMapMetaData.getFieldPlantingDetails() == null && fieldMapTrialInstanceInfo.getBlockId() != null) {
			FieldMapPlantingDetails plantingDetails = new FieldMapPlantingDetails();
			plantingDetails.setBlockCapacity(fieldMapTrialInstanceInfo.getRowsInBlock() + " Rows ,"
					+ fieldMapTrialInstanceInfo.getRangesInBlock() + " Ranges");
			plantingDetails.setRowsPerPlot(fieldMapTrialInstanceInfo.getRowsPerPlot());
			plantingDetails.setColumns(fieldMapTrialInstanceInfo.getRowsInBlock() / fieldMapTrialInstanceInfo.getRowsPerPlot());
			plantingDetails.setFieldLocation(fieldMapTrialInstanceInfo.getLocationName());
			plantingDetails.setFieldName(fieldMapTrialInstanceInfo.getFieldName());
			plantingDetails.setPlotLayout(this.getPlanningOrderString(fieldMapTrialInstanceInfo.getPlantingOrder()));
			plantingDetails.setRowCapacityOfPlantingMachine(fieldMapTrialInstanceInfo.getMachineRowCapacity());
			// TODO: Need to find out how this works.
			plantingDetails.setStartingCoordinates("Column 1, Range 1");
			fieldMapMetaData.setFieldPlantingDetails(plantingDetails);
		}

	}

	private String getPlanningOrderString(Integer plantingOrder) {
		switch (plantingOrder) {
			case 1:
				return "Row/Column";
			case 2:
				return "Serpentine";
			default:
				throw new IllegalArgumentException("This should never happen");
		}
	}

	private Map<Integer, FieldMap> getDefaultFieldMap() {
		final Map<Integer, FieldMap> fieldMaps = new HashMap<Integer, FieldMap>();
		return DefaultedMap.defaultedMap(fieldMaps, new Transformer<Integer, FieldMap>() {

			@Override
			public FieldMap transform(Integer blockId) {
				final FieldMap fieldMap = new FieldMap();
				fieldMap.setBlockId(blockId);
				fieldMaps.put(blockId, fieldMap);
				return fieldMap;
			}
		});
	}

	private StudyFieldMap getRawDataFromMiddleware(final String studyId) {
		Integer studyIdentifier = Integer.valueOf(studyId);
		try {
			final StudyType studyType = this.studyDataManager.getStudyType(studyIdentifier);

			final List<FieldMapInfo> fieldMapInfoOfStudy =
					this.studyDataManager.getFieldMapInfoOfStudy(Lists.newArrayList(studyIdentifier), studyType,
							StudyFieldMapUtility.getCrossExpansionProperties());
			return new StudyFieldMap(studyType, fieldMapInfoOfStudy);
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException(String.format("There was an error retriving infomration for studyId %s was found.", studyId), e);
		}
	}

	private static class StudyFieldMap {

		private StudyType studyType;

		private List<FieldMapInfo> middlewareFieldMapInfo;

		/**
		 * @param studyType
		 * @param middlewareFieldMapInfo
		 */
		public StudyFieldMap(StudyType studyType, List<FieldMapInfo> middlewareFieldMapInfo) {
			super();
			this.studyType = studyType;
			this.middlewareFieldMapInfo = middlewareFieldMapInfo;
		}

		/**
		 * @return the studyType
		 */
		public StudyType getStudyType() {
			return this.studyType;
		}

		/**
		 * @return the middlewareFieldMapInfo
		 */
		public FieldMapInfo getMiddlewareFieldMapInfo() {
			return this.middlewareFieldMapInfo.get(0);
		}

		public List<FieldMapTrialInstanceInfo> getMiddlewareTiralInstances() {
			return this.getFieldmapDataset().getTrialInstances();
		}

		public FieldMapDatasetInfo getFieldmapDataset() {
			return this.getMiddlewareFieldMapInfo().getDatasets().get(0);
		}

	}

}
