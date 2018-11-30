
package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.DefaultedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.FieldMapMetaData;
import org.ibp.api.domain.study.FieldMapPlantingDetails;
import org.ibp.api.domain.study.FieldMapStudySummary;
import org.ibp.api.domain.study.FieldPlot;
import org.ibp.api.exception.ApiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FieldMapService {

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private StudyDataManager studyDataManager;

	public Map<Integer, FieldMap> getFieldMap(final String studyId) {

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

	public String getBlockId(final int datasetId, final String trialInstance) {
		return studyDataManager.getBlockId(datasetId, trialInstance);
	}

	public FieldmapBlockInfo getBlockInformation(int blockId) {
		return studyDataManager.getBlockInformation(blockId);
	}

	private void extractRangesFromPlotData(final Map<Integer, FieldMap> fieldMaps) {
		final Collection<FieldMap> createdFieldMaps = fieldMaps.values();
		for (final FieldMap fieldMap : createdFieldMaps) {
			final FieldPlot[][] fieldPlots = fieldMap.getPlots();

			for (int column = 0; column < fieldPlots.length; column++) {
				for (int range = 0; range < fieldPlots[column].length; range++) {
					final FieldPlot fieldPlot = fieldPlots[column][range];

					this.addToList(this.getDefaultList(fieldMap.getRange()), range + 1, fieldPlot);

					this.addToList(this.getDefaultList(fieldMap.getColumns()), column + 1, fieldPlot);

				}
			}
		}
	}

	private void addToList(final Map<Integer, List<FieldPlot>> mapToAddTo, final int range, final FieldPlot fieldPlot) {
		final List<FieldPlot> rangeList = mapToAddTo.get(range);
		rangeList.add(fieldPlot);
	}

	private Map<Integer, List<FieldPlot>> getDefaultList(final Map<Integer, List<FieldPlot>> rangeMap) {
		return DefaultedMap.defaultedMap(rangeMap, new Transformer<Integer, List<FieldPlot>>() {

			@Override
			public List<FieldPlot> transform(final Integer number) {
				final List<FieldPlot> plotList = new ArrayList<>();
				rangeMap.put(number, plotList);
				return plotList;
			}
		});
	}

	private void populateStudyInformation(final Map<Integer, FieldMap> filedMaps, final StudyFieldMap rawDataFromMiddleware) {
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

	private void updatePlotsDeleted(final FieldMap fieldMap, final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo) {

		final List<String> deletedPlots = middlewareFieldMapTrialInstanceInfo.getDeletedPlots();
		final FieldPlot[][] fieldPlots = fieldMap.getPlots();
		for (final String deletedPlot : deletedPlots) {
			final String[] plots = deletedPlot.split(",");
			final int column = Integer.parseInt(plots[0]);
			final int range = Integer.parseInt(plots[1]);
			final FieldPlot fieldPlot = fieldPlots[column][range];
			fieldPlot.setPlotDeleted(true);
		}

	}

	private void populateFieldMap(final FieldMap fieldMap, final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo,
			final StudyFieldMap rawDataFromMiddleware) {

		this.initFieldPlots(fieldMap, middlewareFieldMapTrialInstanceInfo);
		final FieldPlot[][] fieldPlots = fieldMap.getPlots();
		for (final FieldMapLabel fieldMapLabel : middlewareFieldMapTrialInstanceInfo.getFieldMapLabels()) {
			fieldPlots[fieldMapLabel.getRange() - 1][fieldMapLabel.getColumn() - 1] =
					this.mapMiddlewareFieldLableToFieldPlot(fieldMapLabel, middlewareFieldMapTrialInstanceInfo, rawDataFromMiddleware);
		}

	}

	private FieldPlot mapMiddlewareFieldLableToFieldPlot(final FieldMapLabel fieldMapLabel,
			final FieldMapTrialInstanceInfo middlewareFieldMapTrialInstanceInfo, final StudyFieldMap rawDataFromMiddleware) {
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
			final FieldPlot[][] fieldPlots = StudyFieldMapUtility.getDefaultPlots(middlewareFieldMapTrialInstanceInfo);
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

	private FieldMapStudySummary mapPlantingSummary(final StudyFieldMap rawDataFromMiddleware,
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
			final FieldMapPlantingDetails plantingDetails = new FieldMapPlantingDetails();
			plantingDetails.setBlockCapacity(fieldMapTrialInstanceInfo.getRowsInBlock() + " Rows ,"
					+ fieldMapTrialInstanceInfo.getRangesInBlock() + " Ranges");
			plantingDetails.setRowsPerPlot(fieldMapTrialInstanceInfo.getRowsPerPlot());
			plantingDetails.setColumns(fieldMapTrialInstanceInfo.getRowsInBlock() / fieldMapTrialInstanceInfo.getRowsPerPlot());
			plantingDetails.setFieldLocation(fieldMapTrialInstanceInfo.getLocationName());
			plantingDetails.setFieldName(fieldMapTrialInstanceInfo.getFieldName());
			plantingDetails.setPlotLayout(this.getPlanningOrderString(fieldMapTrialInstanceInfo.getPlantingOrder()));
			plantingDetails.setRowCapacityOfPlantingMachine(fieldMapTrialInstanceInfo.getMachineRowCapacity());
			plantingDetails.setStartingCoordinates("Column 1, Range 1");
			fieldMapMetaData.setFieldPlantingDetails(plantingDetails);
		}

	}

	private String getPlanningOrderString(final Integer plantingOrder) {
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
			public FieldMap transform(final Integer blockId) {
				final FieldMap fieldMap = new FieldMap();
				fieldMap.setBlockId(blockId);
				fieldMaps.put(blockId, fieldMap);
				return fieldMap;
			}
		});
	}

	private StudyFieldMap getRawDataFromMiddleware(final String studyId) {
		final Integer studyIdentifier = Integer.valueOf(studyId);
		try {
			final StudyTypeDto studyType = this.studyDataManager.getStudyTypeByStudyId(studyIdentifier);

			final List<FieldMapInfo> fieldMapInfoOfStudy =
					this.studyDataManager.getFieldMapInfoOfStudy(Lists.newArrayList(studyIdentifier),
							crossExpansionProperties);
			return new StudyFieldMap(studyType, fieldMapInfoOfStudy);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException(String.format("There was an error retriving infomration for studyId %s was found.", studyId), e);
		}
	}

	private static class StudyFieldMap {

		private final StudyTypeDto studyType;

		private final List<FieldMapInfo> middlewareFieldMapInfo;

		/**
		 * @param studyType
		 * @param middlewareFieldMapInfo
		 */
		public StudyFieldMap(final StudyTypeDto studyType, final List<FieldMapInfo> middlewareFieldMapInfo) {
			super();
			this.studyType = studyType;
			this.middlewareFieldMapInfo = middlewareFieldMapInfo;
		}


		/**
		 * @return the studyType
		 */
		public StudyTypeDto getStudyType() {
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
