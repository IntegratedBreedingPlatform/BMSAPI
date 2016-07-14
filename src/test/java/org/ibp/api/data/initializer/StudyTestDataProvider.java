package org.ibp.api.data.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.manager.Season;
import org.ibp.api.domain.study.FieldMapPlantingDetails;
import org.ibp.api.domain.study.FieldMapStudySummary;

import com.google.common.collect.Lists;

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
}
