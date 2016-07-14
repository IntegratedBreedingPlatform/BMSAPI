package org.ibp.api.data.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.manager.Season;
import org.ibp.api.domain.study.FieldMapPlantingDetails;
import org.ibp.api.domain.study.FieldMapStudySummary;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyImportDTO;

public class StudyTestDataProvider {

	public static FieldMapLabel createFieldMapLabel() {
		//Setting fieldMapLabel Values
		FieldMapLabel fieldMapLabel = new FieldMapLabel();
		fieldMapLabel.setExperimentId(1);
		fieldMapLabel.setEntryNumber(2);
		fieldMapLabel.setGermplasmName("Germplasm Name");
		fieldMapLabel.setRep(1);
		fieldMapLabel.setBlockNo(9);
		fieldMapLabel.setPlotNo(1);
		fieldMapLabel.setColumn(1);
		fieldMapLabel.setRange(2);
		fieldMapLabel.setStudyName("Study Name");
		fieldMapLabel.setDatasetId(1);
		fieldMapLabel.setGeolocationId(9);
		fieldMapLabel.setSiteName("Site name");
		fieldMapLabel.setGid(1);
		fieldMapLabel.setSeason(Season.DRY);
		fieldMapLabel.setStartYear("2015");

		return fieldMapLabel;
	}

	public static FieldMapTrialInstanceInfo createFieldMapTrialInstanceInfo(FieldMapLabel fieldMapLabel) {
		List<String> deletedPlots = new ArrayList<>();

		Map<Integer, String> labelHeaders  = new HashMap<>();
		labelHeaders.put(1 , "Label1");

		//Setting FieldMapTrialInstanceInfo values
		FieldMapTrialInstanceInfo trialInstanceInfo = new FieldMapTrialInstanceInfo();
		trialInstanceInfo.setGeolocationId(9);
		trialInstanceInfo.setSiteName("Site name");
		trialInstanceInfo.setTrialInstanceNo("TI1");
		trialInstanceInfo.setFieldMapLabels(Lists.newArrayList(fieldMapLabel));
		trialInstanceInfo.setLabelHeaders(labelHeaders);
		trialInstanceInfo.setBlockName("Block Name");
		trialInstanceInfo.setFieldName("Field Name");
		trialInstanceInfo.setLocationName("Location");
		trialInstanceInfo.setFieldmapUUID(UUID.randomUUID().toString());
		trialInstanceInfo.setRowsInBlock(2);
		trialInstanceInfo.setRangesInBlock(3);
		trialInstanceInfo.setPlantingOrder(2);
		trialInstanceInfo.setStartColumn(0);
		trialInstanceInfo.setStartRange(2);
		trialInstanceInfo.setRowsPerPlot(1);
		trialInstanceInfo.setMachineRowCapacity(1);
		trialInstanceInfo.setOrder(3);
		trialInstanceInfo.setLocationId(9);
		trialInstanceInfo.setFieldId(1);
		trialInstanceInfo.setBlockId(1);
		trialInstanceInfo.setEntryCount(1);
		trialInstanceInfo.setLabelsNeeded(1);
		trialInstanceInfo.setDeletedPlots(deletedPlots);

		return trialInstanceInfo;
	}

	public static FieldMapPlantingDetails createFieldMapPlantingDetails() {
		FieldMapPlantingDetails plantingDetails = new FieldMapPlantingDetails();
		plantingDetails.setBlockCapacity("2");
		plantingDetails.setColumns(2);
		plantingDetails.setFieldLocation("Field Location");
		plantingDetails.setPlotLayout("Plot Layout");
		plantingDetails.setFieldName("Field Name");
		plantingDetails.setRowCapacityOfPlantingMachine(2);
		plantingDetails.setRowsPerPlot(1);
		plantingDetails.setStartingCoordinates("0");

		return plantingDetails;
	}

	public static FieldMapStudySummary createFieldMapStudySummary() {
		FieldMapStudySummary studySummary = new FieldMapStudySummary();
		studySummary.setDataset("Dataset");
		studySummary.setEnvironment(9);
		studySummary.setNumbeOfReps(2L);
		studySummary.setNumberOfEntries(3L);
		studySummary.setOrder(2);
		studySummary.setPlotsNeeded(1L);
		studySummary.setStudyName("Study Name");
		studySummary.setTotalNumberOfPlots(1L);
		studySummary.setType("Type");

		return studySummary;
	}

	public static Observation createObservationData(Integer measurementId, String entryNumber, String entryType, String trialInstance,
			String designation,	Integer gid, String plotNumber, String repitionNumber, String seedSource) {

		final Observation observation = new Observation();
		observation.setUniqueIdentifier(measurementId);
		observation.setEntryNumber(entryNumber);
		observation.setEntryType(entryType);
		observation.setEnvironmentNumber(trialInstance);
		observation.setGermplasmDesignation(designation);
		observation.setGermplasmId(gid);
		observation.setPlotNumber(plotNumber);
		observation.setReplicationNumber(repitionNumber);
		observation.setSeedSource(seedSource);

		return observation;
	}

	public static void fillStudyImportDTO(StudyImportDTO studyImportDTO, final String studyType, final String name, final String objective,
			final String title, final String startdate, final String endDate, final Integer userId, final Long folderId,
			final String siteName, final String studyInstitute) {
		studyImportDTO.setStudyType(studyType);
		studyImportDTO.setName(name);
		studyImportDTO.setObjective(objective);
		studyImportDTO.setTitle(title);
		studyImportDTO.setStartDate(startdate);
		studyImportDTO.setEndDate(endDate);
		studyImportDTO.setUserId(userId);
		studyImportDTO.setFolderId(folderId);
		studyImportDTO.setSiteName(siteName);
		studyImportDTO.setStudyInstitute(studyInstitute);
	}
}
