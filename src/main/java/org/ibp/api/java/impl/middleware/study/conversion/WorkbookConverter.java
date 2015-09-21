package org.ibp.api.java.impl.middleware.study.conversion;

import static org.ibp.api.java.impl.middleware.study.StudyConditions.END_DATE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.OBJECTIVE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.START_DATE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.STUDY_NAME;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.STUDY_TITLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.StudyWorkbook;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.impl.middleware.study.StudyBaseFactors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts from a front-end rest-domain {@link StudyWorkbook} to a back-end middleware-domain Workbook
 * @author j-alberto
 *
 */
@Component
public class WorkbookConverter implements Converter<StudyWorkbook, Workbook>{
	
	@Autowired
	MeasurementVariableConverter converter;
	
	private final Logger LOGGER = LoggerFactory.getLogger(WorkbookConverter.class);
	private Workbook workbook;
	private List<MeasurementVariable> variates;

	@Override
	public Workbook convert(StudyWorkbook source) {

		variates = null;
		workbook = new Workbook();
		
		buildStudyDetails(source); //details, done
		buildConditions(source); //details2, done
		buildConstants(source);
		buildFactors(source);
		buildVariates(source);
		buildObservations(source);
		
		return workbook;
	}

	private void buildStudyDetails(StudyWorkbook source){

		StudyDetails studyDetails = new StudyDetails();

		StudyType stype = StudyType.getStudyType( source.getStudyType() );
		studyDetails.setStudyType(stype);
		
		studyDetails.setStudyName( source.getName() );
		studyDetails.setObjective( source.getObjective() );
		studyDetails.setTitle( source.getTitle() );
		studyDetails.setStartDate( source.getStartDate() );
		studyDetails.setEndDate( source.getEndDate() );
		studyDetails.setSiteName( source.getSiteName() );
		
		LOGGER.info("setting default folder: 25133 (folder 'JRNurseriesFolder')");
		studyDetails.setParentFolderId(25133);

		workbook.setStudyDetails(studyDetails);

	}

	/**
	 * Basic information for Nurseries and Trials
	 * @param source
	 */
	private void buildConditions(StudyWorkbook source){
		List<MeasurementVariable> conditions = new ArrayList<>();
		conditions.add(STUDY_NAME.asMeasurementVariable(source.getName()));
		conditions.add(STUDY_TITLE.asMeasurementVariable(source.getTitle()));
		conditions.add(START_DATE.asMeasurementVariable(source.getStartDate()));
		conditions.add(END_DATE.asMeasurementVariable(source.getEndDate()));
		conditions.add(OBJECTIVE.asMeasurementVariable(source.getObjective()) );
		
		
		workbook.setConditions(conditions);

	}
	/**
	 * Constan values across a study, apply for the whole experiment.
	 * @param source
	 */
	private void buildConstants(StudyWorkbook source){
		List<MeasurementVariable> constants = new ArrayList<>();
		constants.add(StudyBaseFactors.TRIAL_INSTANCE.asFactor());
		
		workbook.setConstants(constants);
		
	}
	
	private void buildFactors(StudyWorkbook source){
		List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
		
		factors.add( StudyBaseFactors.ENTRY_NUMBER.asFactor() );
		factors.add( StudyBaseFactors.DESIGNATION.asFactor() );
		factors.add( StudyBaseFactors.CROSS.asFactor() );
		factors.add( StudyBaseFactors.GID.asFactor() );
		factors.add( StudyBaseFactors.PLOT_NUMBER.asFactor() );
		workbook.setFactors(factors);

	}
	
	private void buildVariates(StudyWorkbook source){
		variates = new ArrayList<>();
		
		for(Trait trait : source.getTraits()){
			variates.add(converter.convert(trait));
		}
		
		workbook.setVariates(variates);

	}
	
	private void buildObservations(StudyWorkbook source){
		List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
		List<MeasurementData> dataList;
		
		for (int numGermEntry = 0; numGermEntry < source.getGermplasms().size(); numGermEntry++) {
			MeasurementRow row = new MeasurementRow();
			GermplasmListEntrySummary germ = source.getGermplasms().get(numGermEntry).getGermplasmListEntrySummary();
			dataList = new ArrayList<MeasurementData>();

			MeasurementData entryData = new MeasurementData(StudyBaseFactors.ENTRY_NUMBER.name(), germ.getEntryCode());
			entryData.setMeasurementVariable(StudyBaseFactors.ENTRY_NUMBER.asFactor());
			dataList.add(entryData);

			MeasurementData designationData = new MeasurementData(StudyBaseFactors.DESIGNATION.name(), germ.getDesignation());
			designationData.setMeasurementVariable(StudyBaseFactors.DESIGNATION.asFactor());
			dataList.add(designationData);

			MeasurementData crossData =
					new MeasurementData(StudyBaseFactors.CROSS.name(), germ.getCross());
			crossData.setMeasurementVariable(StudyBaseFactors.CROSS.asFactor());
			dataList.add(crossData);

			MeasurementData gidData = new MeasurementData(StudyBaseFactors.GID.name(), germ.getGid().toString());
			gidData.setMeasurementVariable(StudyBaseFactors.GID.asFactor());
			dataList.add(gidData);

			MeasurementData plotData = new MeasurementData(StudyBaseFactors.PLOT_NUMBER.name(), germ.getEntryCode());
			plotData.setMeasurementVariable(StudyBaseFactors.PLOT_NUMBER.asFactor());
			dataList.add(plotData);

			for(int numVariate = 0; numVariate < variates.size(); numVariate++){
				MeasurementData variateData = new MeasurementData(variates.get(numVariate).getLabel(), ""); //empty value for now, get from trait values
				variateData.setMeasurementVariable(variates.get(numVariate));
				
				String traitValue = source.getTraitValues()[numGermEntry][numVariate];
				variateData.setValue(traitValue);
				
				
				
				dataList.add(variateData);
			}

			row.setDataList(dataList);
			observations.add(row);
			
			workbook.setObservations(observations);
		}

	}

//	
//	private void buildOthers(StudyWorkbook source){
//		
//		
//		Map<String, VariableTypeList> variableMapType = new HashMap<>();
//		VariableTypeList trialVariableTypeList = new VariableTypeList();
//		
//		variableMapType.put("trialVariableTypeList", trialVariableTypeList);
//		
//		workbook.setVariableMap(variableMapType);
//		
//		
//		List<MeasurementRow> observations = new ArrayList<>();
//		workbook.setObservations(observations);
//		
//		workbook.setVariableMap(null); //to trigger saveVariables in WbSaver
//
//	}

}
