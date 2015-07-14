package org.ibp.api.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.ibp.api.domain.study.StudyWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.generationcp.middleware.domain.dms.VariableTypeList;

/**
 * Converts from a front-end rest-domain {@link StudyWorkbook} to a back-end middleware-domain Workbook
 * @author j-alberto
 *
 */
@Component
public class WorkbookConverter implements Converter<StudyWorkbook, Workbook>{
	
	private final Logger LOGGER = LoggerFactory.getLogger(WorkbookConverter.class);
	private Workbook workbook = new Workbook();

	@Override
	public Workbook convert(StudyWorkbook source) {

		LOGGER.info("into converter!!");
		
		buildStudyDetails(source);
		buildOthers(source);
		
		LOGGER.info("out of converter!!");

		return workbook;
	}

	private void buildStudyDetails(StudyWorkbook source){

		StudyDetails sd = new StudyDetails();
		
		sd.setStudyName( source.getName() );
		sd.setObjective( source.getObjective() );
		sd.setStartDate( source.getStartDate() );
		sd.setEndDate( source.getEndDate() );
		sd.setTitle( source.getTitle() );
		
		StudyType stype = StudyType.getStudyType( source.getStudyType() );
		sd.setStudyType(stype);
		
		sd.setSiteName( source.getSiteName() );
		
		workbook.setStudyDetails(sd);

	}
	
	private void buildOthers(StudyWorkbook source){
		workbook.setStudyId(25200);
		workbook.setTrialDatasetId(25201);
		workbook.setMeasurementDatesetId(25202);//default 0
		workbook.setMeansDatasetId(25203);//default 0. might be null?
		
		
		Map<String, VariableTypeList> variableMapType = new HashMap<>();
		VariableTypeList trialVariableTypeList = new VariableTypeList();
		
		variableMapType.put("trialVariableTypeList", trialVariableTypeList);
		
		workbook.setVariableMap(variableMapType);
		
		
//		f.setId(8256);
//		f.setName("STUDY_BMETH");
//		f.setDescription("Breeding method applied to all plots (DBCV)");
		List<MeasurementRow> observations = new ArrayList<>();
		workbook.setObservations(observations);
		
		workbook.setVariableMap(null); //to trigger saveVariables in WbSaver

	}

}
