/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package org.generationcp.ibpworkbench.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.generationcp.commons.util.ObjectUtil;
import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.exceptions.IBPWebServiceException;
import org.generationcp.ibpworkbench.util.OutlierCSV;
import org.generationcp.ibpworkbench.util.SummaryStatsCSV;
import org.generationcp.ibpworkbench.util.CSVUtil;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.DatasetValues;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.ExperimentType;
import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Stock;
import org.generationcp.middleware.domain.dms.Stocks;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.PhenotypeOutlier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.rits.cloning.Cloner;

@Configurable
public class BreedingViewServiceImpl implements BreedingViewService {

	@Autowired
	private StudyDataManager studyDataManager;
	@Autowired
	private OntologyDataManager ontologyDataManager;
	@Autowired
    private WorkbenchDataManager workbenchDataManager;
	@Autowired
	private Cloner cloner;

	private Map<String, String> nameToAliasMapping;
	private boolean meansDataSetExists;
	private VariableTypeList variableTypeListSummaryStats;

	private List<ExperimentValues> experimentValuesList;
	private List<ExperimentValues> summaryStatsExperimentValuesList;

	private static final Logger LOG = LoggerFactory.getLogger(BreedingViewServiceImpl.class);

	public void execute(Map<String, String> params, List<String> errors) throws IBPWebServiceException {

		try {
		
			meansDataSetExists = false;
			nameToAliasMapping = getNameToAliasMapping();
			
			CSVUtil csvUtil = getCsvUtil();

			String mainOutputFilePath = params.get(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue());
			String summaryOutputFilePath = params.get(WebAPIConstants.SUMMARY_OUTPUT_FILE_PATH.getParamValue());
			String outlierOutputFilePath = params.get(WebAPIConstants.OUTLIER_OUTPUT_FILE_PATH.getParamValue());

			Map<String, ArrayList<String>> traitsAndMeans = csvUtil.csvToMap(mainOutputFilePath);
			
			Map<String, Integer> ndGeolocationIds = new HashMap<String, Integer>();
			LOG.info("Traits and Means: " + traitsAndMeans);


			if(!traitsAndMeans.isEmpty()) {

				DataSet meansDataSet = null;
				String[] csvHeader = traitsAndMeans.keySet().toArray(new String[0]);
				int studyId = Integer.valueOf(params.get(WebAPIConstants.STUDY_ID.getParamValue()));
				int outputDataSetId = Integer.valueOf(params.get(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue()));
				int inputDatasetId = Integer.valueOf(params.get(WebAPIConstants.INPUT_DATASET_ID.getParamValue()));

				List<DataSet> ds = studyDataManager.getDataSetsByType(studyId, DataSetType.MEANS_DATA);
				if (ds != null){
					if (!ds.isEmpty()){
						meansDataSet = ds.get(0);
					}else if (outputDataSetId != 0){
						meansDataSet = studyDataManager.getDataSet(outputDataSetId);
					}

					if (meansDataSet != null) {

						meansDataSet = additionalVariableTypes(csvHeader ,studyDataManager.getDataSet(inputDatasetId), meansDataSet );
						meansDataSetExists = true;

					}
				}


				TrialEnvironments trialEnvironments = 
						studyDataManager.getTrialEnvironmentsInDataset(inputDatasetId);
				for (TrialEnvironment trialEnv : trialEnvironments.getTrialEnvironments()){
					ndGeolocationIds.put(trialEnv.getVariables()
							.findByLocalName(csvHeader[0]).getValue(), trialEnv.getId());
				}

				Stocks stocks = studyDataManager.getStocksInDataset(inputDatasetId);
				DataSet dataSet = studyDataManager.getDataSet(inputDatasetId);
				VariableTypeList meansVariatesList = getMeansVariableTypeList();
				
				//Get only the trial environment and germplasm factors

				for (VariableType factorFromDataSet : dataSet.getVariableTypes().getFactors().getVariableTypes()){
					if (factorFromDataSet.getStandardVariable().getPhenotypicType() 
							== PhenotypicType.TRIAL_ENVIRONMENT
							|| factorFromDataSet.getStandardVariable().getPhenotypicType() 
							== PhenotypicType.GERMPLASM) {
						meansVariatesList.makeRoom(1);
						factorFromDataSet.setRank(1);
						meansVariatesList.add(factorFromDataSet);
					}
				}
				//get variates only
				VariableTypeList allVariatesList = dataSet.getVariableTypes().getVariates();
				

				Integer numOfFactorsAndVariates = 
						meansVariatesList.getFactors().getVariableTypes().size()
						+ meansVariatesList.getVariates().getVariableTypes().size() + 1;
				
				for(int i = 2; i < csvHeader.length; i++) {
					createMeansVariableType(numOfFactorsAndVariates, csvHeader[i], allVariatesList, meansVariatesList);		
				}


				//please make sure that the study name is unique and does not exist in the db.
				VariableList variableList = new VariableList();
				Study study = studyDataManager.getStudy(studyId);
				Variable variable = createVariable(TermId.DATASET_NAME.getId()
						, study.getName() + "-MEANS"  , 1);
				meansVariatesList.makeRoom(1);
				variable.getVariableType().setRank(1);
				meansVariatesList.add(variable.getVariableType());

				//name of dataset [STUDY NAME]-MEANS
				updateVariableType(variable.getVariableType(), study.getName() + "-MEANS", "Dataset name (local)");
				variableList.add(variable);

				variable = createVariable(TermId.DATASET_TITLE.getId(), "My Dataset Description", 2);
				meansVariatesList.makeRoom(1);
				variable.getVariableType().setRank(1);
				meansVariatesList.add(variable.getVariableType());
				updateVariableType(variable.getVariableType(), "DATASET_TITLE", "Dataset title (local)");
				variableList.add(variable);

				variable = createVariable(TermId.DATASET_TYPE.getId(), "10070", 3);
				meansVariatesList.makeRoom(1);
				variable.getVariableType().setRank(1);
				meansVariatesList.add(variable.getVariableType());
				updateVariableType(variable.getVariableType(), "DATASET_TYPE", "Dataset type (local)");
				variableList.add(variable);
				DatasetValues datasetValues = new DatasetValues();
				datasetValues.setVariables(variableList);

				DatasetReference datasetReference = null;
				if (meansDataSet == null){
					//save data
					//get dataset using new datasetid
					datasetReference = studyDataManager.addDataSet(studyId, meansVariatesList, datasetValues, 
							getProgramUUID(studyDataManager,studyId));
					meansDataSet = studyDataManager.getDataSet(datasetReference.getId());
				}

				experimentValuesList = new ArrayList<ExperimentValues>();
				List<String> environments = traitsAndMeans.get(csvHeader[0]);
				for(int i = 0; i < environments.size(); i++) {

					String envName = traitsAndMeans.get(csvHeader[0]).get(i).replace(";", ",");

					Stock stock = stocks.findOnlyOneByLocalName(
							csvHeader[1], traitsAndMeans.get(csvHeader[1]).get(i));
					if (stock != null){
						ExperimentValues experimentRow = new ExperimentValues();
						experimentRow.setGermplasmId(stock.getId());
						Integer ndLocationId = ndGeolocationIds.get(envName);
						experimentRow.setLocationId(ndLocationId);

						List<Variable> list = new ArrayList<Variable>();

						for(int j = 2; j < csvHeader.length; j++) {
							if (meansDataSetExists){
								if (meansDataSet.getVariableTypes().getVariates()
										.findByLocalName(csvHeader[j]) == null){
									continue;
								}
							}

							String variableValue = traitsAndMeans.get(csvHeader[j]).get(i).trim();
							if (!variableValue.trim().isEmpty()) {
								Variable var = new Variable(meansDataSet.getVariableTypes()
										.findByLocalName(csvHeader[j]), variableValue);
								list.add(var);
							}

						}
						VariableList variableList1 = new VariableList();
						variableList1.setVariables(list);
						experimentRow.setVariableList(variableList1);
						experimentValuesList.add(experimentRow);


					}


				}

				studyDataManager.addOrUpdateExperiment(
						meansDataSet.getId(), ExperimentType.AVERAGE, experimentValuesList);

				if(outlierOutputFilePath!=null && !outlierOutputFilePath.equals("")) {
					uploadAndSaveOutlierDataToDB(
							outlierOutputFilePath, studyId, ndGeolocationIds, dataSet);
				}

				//GCP-6209
				if(summaryOutputFilePath!=null && !summaryOutputFilePath.equals("")) {
					uploadAndSaveSummaryStatsToDB(
							summaryOutputFilePath, studyId, trialEnvironments, dataSet);
				}

			} 
			
		}catch(Exception e){
			LOG.error("ERROR:", e);
			throw new IBPWebServiceException(e.getMessage());
		}
		
		
		
	}
	
	protected String getProgramUUID(StudyDataManager studyDataManager, int studyId) 
			throws MiddlewareQueryException {
		return studyDataManager.getProject(studyId).getProgramUUID();
	}

	protected void createMeansVariableType(Integer numOfFactorsAndVariates, String headerName, VariableTypeList allVariatesList,VariableTypeList meansVariateList) throws MiddlewareQueryException {
		
		String traitName = "", localName = "", methodName = "";
		
		traitName = (headerName != null && headerName.lastIndexOf("_") != -1)
				? headerName.substring(0, headerName.lastIndexOf("_")) : "";
		if (headerName.endsWith("_Means")){
			localName = "_Means";
			methodName = "LS MEAN";
		} else if (headerName.endsWith("_UnitErrors")) {
			localName = "_UnitErrors";
			methodName = "ERROR ESTIMATE";
		} else {
			return;
		}
		
		VariableType originalVariableType = null;
		VariableType newVariableType = null;
		
		originalVariableType = allVariatesList.findByLocalName(traitName);
		newVariableType = cloner.deepClone(originalVariableType);
		
		newVariableType.setLocalName(traitName + localName);
		
		Term termMethod = ontologyDataManager.findMethodByName(methodName);
		if(termMethod == null) {
			String definitionMeans = 
					newVariableType.getStandardVariable().getMethod().getDefinition();
			termMethod = ontologyDataManager.addMethod(methodName, definitionMeans);
		}

		Integer stdVariableId = 
				ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(
						newVariableType.getStandardVariable().getProperty().getId()
						,newVariableType.getStandardVariable().getScale().getId()
						,termMethod.getId()
						,PhenotypicType.VARIATE
						);
		
		//check if the stdVariableId already exists in the variableTypeList
		for (VariableType vt : meansVariateList.getVariableTypes()){
			if (stdVariableId != null && vt.getStandardVariable().getId() == stdVariableId.intValue()){
				
				termMethod = ontologyDataManager.findMethodByName(methodName+ " (" + traitName + ")");
				
				if(termMethod == null) {
					String definitionMeans = 
							newVariableType.getStandardVariable().getMethod().getDefinition();
					termMethod = ontologyDataManager.addMethod(methodName + " (" + traitName + ")" , definitionMeans);
				}

				stdVariableId = 
						ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(
								newVariableType.getStandardVariable().getProperty().getId()
								,newVariableType.getStandardVariable().getScale().getId()
								,termMethod.getId()
								,PhenotypicType.VARIATE
								);
				break;
			}
		}
		

		if (stdVariableId == null){
			StandardVariable stdVariable = new StandardVariable();
			stdVariable = cloner.deepClone(newVariableType.getStandardVariable());
			stdVariable.setEnumerations(null);
			stdVariable.setConstraints(null);
			stdVariable.setId(0);
			stdVariable.setName(newVariableType.getLocalName());
			stdVariable.setMethod(termMethod);
			//check if name is already used
			Term existingStdVar = ontologyDataManager
					.findTermByName(stdVariable.getName(), CvId.VARIABLES);
			if (existingStdVar != null){
				//rename 
				stdVariable.setName(stdVariable.getName()+"_1");
			}
			ontologyDataManager.addStandardVariable(stdVariable);
			newVariableType.setStandardVariable(stdVariable);

		}else{
			StandardVariable stdVar = ontologyDataManager
					.getStandardVariable(stdVariableId);
						if (stdVar.getEnumerations() != null){
							for (Enumeration enumeration : stdVar.getEnumerations()){
								ontologyDataManager.deleteStandardVariableEnumeration(stdVariableId, enumeration.getId());
							}
						}
					stdVar.setEnumerations(null);
					ontologyDataManager.deleteStandardVariableLocalConstraints(stdVariableId);
			newVariableType.setStandardVariable(stdVar);
		}

		meansVariateList.makeRoom(numOfFactorsAndVariates);
		newVariableType.setRank(numOfFactorsAndVariates);
		meansVariateList.add(newVariableType);
		
	}


	private Variable createVariable(int termId, String value, int rank) throws MiddlewareQueryException {
		StandardVariable stVar = ontologyDataManager.getStandardVariable(termId);

		VariableType vtype = new VariableType();
		vtype.setStandardVariable(stVar);
		vtype.setRank(rank);
		Variable var = new Variable();
		var.setValue(value);
		var.setVariableType(vtype);
		return var;
	}

	private void updateVariableType(VariableType type, String name, String description) {
		type.setLocalName(name);
		type.setLocalDescription(description);
	}

	private DataSet additionalVariableTypes(
			String[] csvHeader,DataSet inputDataSet,DataSet meansDataSet) 
					throws MiddlewareQueryException{

		List<Integer> numericTypes = new ArrayList<Integer>();
		numericTypes.add(TermId.NUMERIC_VARIABLE.getId());
		numericTypes.add(TermId.MIN_VALUE.getId());
		numericTypes.add(TermId.MAX_VALUE.getId());
		numericTypes.add(TermId.DATE_VARIABLE.getId());
		numericTypes.add(TermId.NUMERIC_DBID_VARIABLE.getId());
		
		List<Integer> standardVariableIdTracker = new ArrayList<Integer>();

		int rank = meansDataSet.getVariableTypes().getVariableTypes()
				.get(meansDataSet.getVariableTypes().getVariableTypes().size()-1).getRank()+1;

		List<String> inputDataSetVariateNames = new ArrayList<String>( 
				Arrays.asList(Arrays.copyOfRange(csvHeader, 3, csvHeader.length)));
		List<String> meansDataSetVariateNames = new ArrayList<String>();

		Iterator<String> iterator = inputDataSetVariateNames.iterator();
		while (iterator.hasNext()){
			if (iterator.next().contains("_UnitErrors") || iterator.next().contains("_UnitErr")) {
				iterator.remove();
			}
		}

		for (VariableType var : meansDataSet.getVariableTypes().getVariates().getVariableTypes()){
			standardVariableIdTracker.add(var.getStandardVariable().getId());
			if (!var.getStandardVariable().getMethod().getName().equalsIgnoreCase("error estimate")){
				meansDataSetVariateNames.add(var.getLocalName().trim());
			}
				
		}

		if (meansDataSetVariateNames.size() < inputDataSetVariateNames.size()){

			inputDataSetVariateNames.removeAll(meansDataSetVariateNames);

			for (String variateName : inputDataSetVariateNames){
				String root = variateName.substring(0, variateName.lastIndexOf("_"));
				if(!"".equals(root)) {

					VariableType meansVariableType = cloner.deepClone(
							inputDataSet.getVariableTypes().findByLocalName(root));
					meansVariableType.setLocalName(root + "_Means");
					Term termLSMean = ontologyDataManager.findMethodByName("LS MEAN");
					if(termLSMean == null) {
						String definitionMeans = meansVariableType.getStandardVariable()
								.getMethod().getDefinition();
						termLSMean = ontologyDataManager.addMethod("LS MEAN", definitionMeans);
					}

					Integer stdVariableId = ontologyDataManager
							.getStandardVariableIdByPropertyScaleMethodRole(
									meansVariableType.getStandardVariable().getProperty().getId()
									,meansVariableType.getStandardVariable().getScale().getId()
									,termLSMean.getId()
									,PhenotypicType.VARIATE
									);
					
					//check if the stdVariableId already exists in the standardVariableIdTracker
					for (Integer vt : standardVariableIdTracker){
						if (stdVariableId != null && vt.intValue() == stdVariableId.intValue()){
							
							termLSMean = ontologyDataManager.findMethodByName("LS MEAN (" + root + ")");
							
							if(termLSMean == null) {
								String definitionMeans = 
										meansVariableType.getStandardVariable().getMethod().getDefinition();
								termLSMean = ontologyDataManager.addMethod("LS MEAN (" + root + ")" , definitionMeans);
							}

							stdVariableId = 
									ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(
											meansVariableType.getStandardVariable().getProperty().getId()
											,meansVariableType.getStandardVariable().getScale().getId()
											,termLSMean.getId()
											,PhenotypicType.VARIATE
											);
							break;
						}
					}

					if (stdVariableId == null){
						StandardVariable stdVariable = new StandardVariable();
						stdVariable = cloner.deepClone(meansVariableType.getStandardVariable());
						stdVariable.setEnumerations(null);
						stdVariable.setConstraints(null);
						stdVariable.setId(0);
						stdVariable.setName(meansVariableType.getLocalName());
						stdVariable.setMethod(termLSMean);
						//check if name is already used
						Term existingStdVar = ontologyDataManager
								.findTermByName(stdVariable.getName(), CvId.VARIABLES);
						if (existingStdVar != null){
							//rename 
							stdVariable.setName(stdVariable.getName()+"_1");
						}
						ontologyDataManager.addStandardVariable(stdVariable);
						meansVariableType.setStandardVariable(stdVariable);
						standardVariableIdTracker.add(stdVariable.getId());
					}else{
						StandardVariable stdVar = ontologyDataManager
								.getStandardVariable(stdVariableId);
									if (stdVar.getEnumerations() != null){
										for (Enumeration enumeration : stdVar.getEnumerations()){
											ontologyDataManager.deleteStandardVariableEnumeration(stdVariableId, enumeration.getId());
										}
									}
								stdVar.setEnumerations(null);
								ontologyDataManager.deleteStandardVariableLocalConstraints(stdVariableId);
						meansVariableType.setStandardVariable(stdVar);
						standardVariableIdTracker.add(stdVariableId);
					}


					meansVariableType.setRank(rank);
					try{ 
						studyDataManager.addDataSetVariableType(meansDataSet.getId(), meansVariableType); 
						rank++;
					} catch(MiddlewareQueryException e ) {  
						LOG.info("INFO: ",e);
					}

					stdVariableId = null;
					//Unit Errors
					VariableType unitErrorsVariableType = cloner.deepClone(
							inputDataSet.getVariableTypes().findByLocalName(root));
					unitErrorsVariableType.setLocalName(root + "_UnitErrors");
					Term termErrorEstimate = ontologyDataManager
							.findMethodByName("ERROR ESTIMATE");
					if(termErrorEstimate == null) {
						String definitionUErrors = unitErrorsVariableType
								.getStandardVariable().getMethod().getDefinition();
						termErrorEstimate = ontologyDataManager
								.addMethod("ERROR ESTIMATE", definitionUErrors);
					}

					stdVariableId = ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(
							unitErrorsVariableType.getStandardVariable().getProperty().getId()
							,unitErrorsVariableType.getStandardVariable().getScale().getId()
							,termErrorEstimate.getId()
							,PhenotypicType.VARIATE
							);
					
					//check if the stdVariableId already exists in the variableTypeList
					for (Integer vt : standardVariableIdTracker){
						if (stdVariableId != null && vt.intValue() == stdVariableId.intValue()){
							
							termErrorEstimate = ontologyDataManager.findMethodByName("ERROR ESTIMATE (" + root + ")");
							if(termErrorEstimate == null) {
								String definitionUErrors = 
										unitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
								termErrorEstimate = ontologyDataManager
										.addMethod("ERROR ESTIMATE (" + root + ")", definitionUErrors);
							}

							stdVariableId = ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(
									unitErrorsVariableType.getStandardVariable().getProperty().getId()
									,unitErrorsVariableType.getStandardVariable().getScale().getId()
									,termErrorEstimate.getId()
									,PhenotypicType.VARIATE
									);
							break;
						}
					}
					
					

					if (stdVariableId == null){
						StandardVariable stdVariable = new StandardVariable();
						stdVariable = cloner.deepClone(unitErrorsVariableType.getStandardVariable());
						stdVariable.setEnumerations(null);
						stdVariable.setConstraints(null);
						stdVariable.setId(0);
						stdVariable.setName(unitErrorsVariableType.getLocalName());
						stdVariable.setMethod(termErrorEstimate);
						//check if name is already used
						Term existingStdVar = ontologyDataManager
								.findTermByName(stdVariable.getName(), CvId.VARIABLES);
						if (existingStdVar != null){
							//rename 
							stdVariable.setName(stdVariable.getName()+"_1");
						}
						ontologyDataManager.addStandardVariable(stdVariable);
						unitErrorsVariableType.setStandardVariable(stdVariable);
						standardVariableIdTracker.add(stdVariable.getId());
					}else{
						StandardVariable stdVar = ontologyDataManager
								.getStandardVariable(stdVariableId);
									if (stdVar.getEnumerations() != null){
										for (Enumeration enumeration : stdVar.getEnumerations()){
											ontologyDataManager.deleteStandardVariableEnumeration(stdVariableId, enumeration.getId());
										}
									}
								stdVar.setEnumerations(null);
								ontologyDataManager.deleteStandardVariableLocalConstraints(stdVariableId);
						unitErrorsVariableType.setStandardVariable(stdVar);
						standardVariableIdTracker.add(stdVariableId);
					}


					unitErrorsVariableType.setRank(rank);
					try {
						studyDataManager.addDataSetVariableType(
								meansDataSet.getId(), unitErrorsVariableType);
						rank++;
					} catch (MiddlewareQueryException e) {
						LOG.info("INFO: ", e);
					}                     
				}

			}

			return studyDataManager.getDataSet(meansDataSet.getId());
		}

		return meansDataSet;


	}

	public void deleteDataSet(Integer dataSetId) throws MiddlewareQueryException {
		studyDataManager.deleteDataSet(dataSetId);
	}

	public void uploadAndSaveOutlierDataToDB(
			String outlierOutputFilePath, int studyId, 
			Map<String, Integer> ndGeolocationIds, DataSet measurementDataSet) 
					throws MiddlewareQueryException, IOException {

		
		OutlierCSV outlierCSV = getOutlierCSV(outlierOutputFilePath);

		Map<String, Map<String, ArrayList<String>>> outlierData = 
				outlierCSV.getData();


		Map<Integer, Integer> stdVariableIds = new HashMap<Integer, Integer>();
		Integer i = 0;
		for (String l : outlierCSV.getHeaderTraits()){
			Integer traitId = measurementDataSet.getVariableTypes().findByLocalName(l).getId();
			stdVariableIds.put(i, traitId);
			i++;
		}

		Set<String> environments = outlierData.keySet();
		for(String env : environments) {

			List<PhenotypeOutlier> outliers = new ArrayList<PhenotypeOutlier>();
			Integer ndGeolocationId = ndGeolocationIds.get(env);

			for (Entry<String, ArrayList<String>> plot : outlierData.get(env).entrySet()){

				List<Integer> cvTermIds = new ArrayList<Integer>();
				Integer plotNo = Integer.valueOf(plot.getKey());
				Map<Integer, String> plotMap = new HashMap<Integer, String>();

				for (int x = 0; x < plot.getValue().size(); x++){
					String traitValue = plot.getValue().get(x);
					if (traitValue.isEmpty()){
						cvTermIds.add(stdVariableIds.get(x));
						plotMap.put(stdVariableIds.get(x), traitValue);
					}

				}

				List<Object[]> list = studyDataManager.getPhenotypeIdsByLocationAndPlotNo(measurementDataSet.getId(), ndGeolocationId, plotNo, cvTermIds);
				for (Object[] object : list){
					PhenotypeOutlier outlier = new PhenotypeOutlier();
					outlier.setPhenotypeId(Integer.valueOf(object[2].toString()));
					outlier.setValue(plotMap.get(Integer.valueOf(object[1].toString())));
					outliers.add(outlier);
				}

			}

			studyDataManager.saveOrUpdatePhenotypeOutliers(outliers);
		}

	}

	public void uploadAndSaveSummaryStatsToDB(
			String summaryStatsOutputFilePath, int studyId, 
			TrialEnvironments trialEnvironments, DataSet measurementDataSet) 
					throws MiddlewareQueryException, IOException {

		    SummaryStatsCSV summaryStatsCSV = getSummaryStatsCSV(summaryStatsOutputFilePath);

			Map<String, Map<String, ArrayList<String>>> summaryStatsData = 
					summaryStatsCSV.getData();


			int trialDatasetId = studyId-1;//default
			List<DatasetReference> datasets = studyDataManager.getDatasetReferences(studyId);
			for (DatasetReference datasetReference : datasets) {
				String name = datasetReference.getName();
				int id = datasetReference.getId();
				if(measurementDataSet.getId()!=id){
					if(name!=null && (name.startsWith("TRIAL_") || name.startsWith("NURSERY_") || name.endsWith("-ENVIRONMENT"))) {
						trialDatasetId = id;
						break;
					} else {
						DataSet ds = studyDataManager.getDataSet(id);
						if(ds!=null && ds.getVariableTypes().getVariableTypes()!=null) {
							boolean aTrialDataset = true;
							for (VariableType variableType: ds.getVariableTypes().getVariableTypes()) {
								if(variableType.getStandardVariable().getPhenotypicType() 
										== PhenotypicType.GERMPLASM) {
									aTrialDataset = false;
									break;
								}
							}
							if(aTrialDataset) {
								trialDatasetId = id;
							}
						}
					}
				}
			}
			LOG.info("Trial dataset id = "+trialDatasetId);
			DataSet trialDataSet = studyDataManager.getDataSet(trialDatasetId);

			//used in getting the new project properties
			VariableTypeList variableTypeListVariates = measurementDataSet.getVariableTypes().getVariates();

			//list that will contain all summary stats project properties
			variableTypeListSummaryStats = new VariableTypeList();

			List<String> summaryStatsList = summaryStatsCSV.getHeaderStats();
			String trialLocalName =  summaryStatsCSV.getTrialHeader();

			for (String summaryStatName : summaryStatsList){
				Term termSummaryStat = ontologyDataManager.findMethodByName(summaryStatName);
				if(termSummaryStat == null) {
					termSummaryStat = ontologyDataManager.addMethod(summaryStatName, summaryStatName + "  (system generated method)");
				}
			}


			LOG.info("prepare the summary stats project properties if necessary");
			int lastRank = trialDataSet.getVariableTypes().size();

			List<StandardVariable> list = new ArrayList<StandardVariable>();

			for (String summaryStatName : summaryStatsList){

				for(VariableType variate : variableTypeListVariates.getVariableTypes()) {

					if (nameToAliasMapping.containsValue(variate.getLocalName())){
						VariableType originalVariableType = null;
						VariableType summaryStatVariableType = null;	
						Term termSummaryStat = ontologyDataManager.findMethodByName(summaryStatName);

						//check if the summary stat trait is already existing
						String trait = variate.getLocalName();
						String localName = trait + "_" + summaryStatName;
						summaryStatVariableType = trialDataSet.findVariableTypeByLocalName(localName);
						//this means we need to append the traits in the dataset project properties
						if(summaryStatVariableType == null) {
							LOG.info(localName + " project property not found.. need to add "+localName);
							originalVariableType = variableTypeListVariates.findByLocalName(trait);
							summaryStatVariableType = cloner.deepClone(originalVariableType);
							summaryStatVariableType.setLocalName(localName);

							Integer stdVariableId = ontologyDataManager
									.getStandardVariableIdByPropertyScaleMethodRole(
											summaryStatVariableType.getStandardVariable().getProperty().getId(),
											summaryStatVariableType.getStandardVariable().getScale().getId(),
											termSummaryStat.getId(),
											PhenotypicType.VARIATE);

							if (stdVariableId == null){
								StandardVariable stdVariable = new StandardVariable();
								stdVariable = cloner.deepClone(summaryStatVariableType.getStandardVariable());
								stdVariable.setEnumerations(null);
								stdVariable.setConstraints(null);
								stdVariable.setId(0);
								stdVariable.setName(summaryStatVariableType.getLocalName());
								stdVariable.setMethod(termSummaryStat);

								//check if localname is already used
								Term existingStdVar = ontologyDataManager
										.findTermByName(stdVariable.getName(), CvId.VARIABLES);
								if (existingStdVar != null){
									//rename 
									stdVariable.setName(stdVariable.getName()+"_1");
								}

								list.add(stdVariable);
								summaryStatVariableType.setStandardVariable(stdVariable);
								LOG.info("added standard variable "+summaryStatVariableType
										.getStandardVariable().getName());
							}else{
								StandardVariable stdVar = ontologyDataManager
								.getStandardVariable(stdVariableId);
									if (stdVar.getEnumerations() != null){
										for (Enumeration enumeration : stdVar.getEnumerations()){
											ontologyDataManager.deleteStandardVariableEnumeration(stdVariableId, enumeration.getId());
										}
									}
								stdVar.setEnumerations(null);
								stdVar.setConstraints(null);
								ontologyDataManager.deleteStandardVariableLocalConstraints(stdVariableId);
								summaryStatVariableType.setStandardVariable(stdVar);
								LOG.info("reused standard variable "
										+ summaryStatVariableType.getStandardVariable().getName());	    	            	
							}

							summaryStatVariableType.setRank(++lastRank);
							variableTypeListSummaryStats.add(summaryStatVariableType);
							trialDataSet.getVariableTypes()
							.add(summaryStatVariableType);
						}
					}
				}
			}

			ontologyDataManager.addStandardVariable(list);

			Set<String> environments = summaryStatsData.keySet();
			summaryStatsExperimentValuesList = new ArrayList<ExperimentValues>();
			List<Integer> locationIds = new ArrayList<Integer>();

			for (String summaryStatName : summaryStatsList){

				VariableType summaryStatVariableType = null;

				for(String env : environments) {

					LOG.info("prepare experiment values per location, "+trialLocalName+"="+env);
					//--------- prepare experiment values per location ------------------------------------------------------//
					TrialEnvironment trialEnv = trialEnvironments.findOnlyOneByLocalName(trialLocalName, env.replace(";", ","));
					if (trialEnv == null) {
						trialEnv = trialEnvironments.findOnlyOneByLocalName(trialLocalName, env);
					}
					int ndLocationId = trialEnv.getId();
					LOG.info("ndLocationId ="+ndLocationId);
					locationIds.add(ndLocationId);
					List<Variable> traits = new ArrayList<Variable>();
					VariableList variableList = new VariableList();
					variableList.setVariables(traits);
					ExperimentValues e = new ExperimentValues();
					e.setVariableList(variableList);
					e.setLocationId(ndLocationId);
					summaryStatsExperimentValuesList.add(e);

					Map<String, ArrayList<String>> traitSummaryStats = summaryStatsData.get(env);
					for(Entry<String, ArrayList<String>> traitSummaryStat : traitSummaryStats.entrySet()) {
						String trait = traitSummaryStat.getKey();

						String summaryStatValue = traitSummaryStat.getValue().get(summaryStatsList.indexOf(summaryStatName));
						String localName = trait + "_" + summaryStatName;

						//get summary stat trait
						summaryStatVariableType = trialDataSet.findVariableTypeByLocalName(localName);

						//---------- prepare experiments -------------------------------------//
						if(summaryStatVariableType!=null) {
							Variable var = new Variable(summaryStatVariableType,summaryStatValue);
							e.getVariableList().getVariables().add(var);
							LOG.info("preparing experiment variable "+summaryStatVariableType.getLocalName()+ 
									" with value "+summaryStatValue);
						}
					}
				}



			}


			//------------ save project properties and experiments ----------------------------------//
			DmsProject project = new DmsProject();
			project.setProjectId(trialDatasetId);
			studyDataManager.saveTrialDatasetSummary(project,variableTypeListSummaryStats, summaryStatsExperimentValuesList, locationIds);


	}
	
	
	protected Map<String, String> getNameToAliasMapping() throws MiddlewareQueryException {
		
			Map<String, String> map = new HashMap<String, String>();
			
			String fileName = String.format("%s\\Temp\\%s", 
					workbenchDataManager.getWorkbenchSetting().getInstallationDirectory()
					, "mapping.ser" );
			
			map = new ObjectUtil<HashMap<String, String>>().deserializeFromFile(fileName);
			
			return map;
	}


	protected void setStudyDataManager(StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}


	protected void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}


	protected void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	protected CSVUtil getCsvUtil() {
			return new CSVUtil(nameToAliasMapping);
	}

	protected OutlierCSV getOutlierCSV(String outlierOutputFilePath) {
	
		return new OutlierCSV(outlierOutputFilePath, nameToAliasMapping); 
	}


	protected SummaryStatsCSV getSummaryStatsCSV(String summaryStatsOutputFilePath) {
		
		return new SummaryStatsCSV(summaryStatsOutputFilePath, nameToAliasMapping);
		 
	}
	
	protected void setNameToAliasMapping(Map<String, String> mapping){
		this.nameToAliasMapping = mapping;
	}

	protected Cloner getCloner() {
		return cloner;
	}


	protected void setCloner(Cloner cloner) {
		this.cloner = cloner;
	}


	protected VariableTypeList getMeansVariableTypeList() {
		return new VariableTypeList();
	}


	protected List<ExperimentValues> getExperimentValuesList() {
		return experimentValuesList;
	}
	
	protected List<ExperimentValues> getSummaryStatsExperimentValuesList() {
		return summaryStatsExperimentValuesList;
	}
	
	protected VariableTypeList getVariableTypeListSummaryStats() {
		return variableTypeListSummaryStats;
	}

}
