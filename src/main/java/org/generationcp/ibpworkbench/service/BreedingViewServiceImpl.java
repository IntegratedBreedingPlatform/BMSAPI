package org.generationcp.ibpworkbench.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.util.HeritabilityCSVUtil;
import org.generationcp.ibpworkbench.util.TraitsAndMeansCSVUtil2;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.DatasetValues;
import org.generationcp.middleware.domain.dms.ExperimentType;
import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Stock;
import org.generationcp.middleware.domain.dms.Stocks;
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
import org.generationcp.middleware.manager.OntologyDataManagerImpl;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rits.cloning.Cloner;

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

public class BreedingViewServiceImpl implements BreedingViewService {
    @Autowired
    private TraitsAndMeansCSVUtil2 traitsAndMeansCSVUtil2;
    @Autowired
    private HeritabilityCSVUtil heritabilityCSVUtil;
    @Autowired
    private StudyDataManagerImpl studyDataManagerV2;
    @Autowired
    private OntologyDataManagerImpl ontologyDataManagerV2;
    @Autowired
    private Cloner cloner;
    
    private boolean meansDataSetExists = false;
    
    private final static Logger log = LoggerFactory.getLogger(BreedingViewServiceImpl.class);

    public void execute(Map<String, String> params, List<String> errors) throws Exception {
        String mainOutputFilePath = params.get(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue());
        String heritabilityOutputFilePath = params.get(WebAPIConstants.HERITABILITY_OUTPUT_FILE_PATH.getParamValue());
        String workbenchProjectId = params.get(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue());
        Map<String, ArrayList<String>> traitsAndMeans = traitsAndMeansCSVUtil2.csvToMap(mainOutputFilePath);
        log.info("Traits and Means: " + traitsAndMeans);
        
        int ndLocationId;

        //call middleware api and save
        if(!traitsAndMeans.isEmpty()) {
        	DataSet meansDataSet = null;
            String[] csvHeader = traitsAndMeans.keySet().toArray(new String[0]) ;  //csv header
            int studyId = Integer.valueOf(params.get(WebAPIConstants.STUDY_ID.getParamValue()));
            int outputDataSetId = Integer.valueOf(params.get(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue()));
            int inputDatasetId = Integer.valueOf(params.get(WebAPIConstants.INPUT_DATASET_ID.getParamValue()));
            
            	
        	List<DataSet> ds = studyDataManagerV2.getDataSetsByType(studyId, DataSetType.MEANS_DATA);
        	if (ds != null){
        		if (ds.size() > 0){
        			meansDataSet = ds.get(0);
        		}else if (outputDataSetId != 0){
                	meansDataSet = studyDataManagerV2.getDataSet(outputDataSetId);
                }
        		
        		if (meansDataSet != null) {
            		
        			meansDataSet = additionalVariableTypes(csvHeader ,studyDataManagerV2.getDataSet(inputDatasetId), meansDataSet );
        			meansDataSetExists = true;
            		
            	}
        	}
            	
            
            TrialEnvironments trialEnvironments = studyDataManagerV2.getTrialEnvironmentsInDataset(inputDatasetId);
            //environment, env value
            TrialEnvironment trialEnv = trialEnvironments.findOnlyOneByLocalName(csvHeader[0], traitsAndMeans.get(csvHeader[0]).get(0));
            ndLocationId = trialEnv.getId();
            Stocks stocks = studyDataManagerV2.getStocksInDataset(inputDatasetId);

            DataSet dataSet = studyDataManagerV2.getDataSet(inputDatasetId);
            VariableTypeList variableTypeList = new VariableTypeList();
            
            //Get only the trial environment and germplasm factors
            
            for (VariableType factorFromDataSet : dataSet.getVariableTypes().getFactors().getVariableTypes()){
            	if (factorFromDataSet.getStandardVariable().getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT
            			|| factorFromDataSet.getStandardVariable().getPhenotypicType() == PhenotypicType.GERMPLASM) {
            		variableTypeList.makeRoom(1);
            		factorFromDataSet.setRank(1);
            		variableTypeList.add(factorFromDataSet);
            	}
            }
            //get variates only
            VariableTypeList variableTypeListVariates = dataSet.getVariableTypes().getVariates();
            VariableType originalVariableType = null;
            VariableType meansVariableType = null;
            VariableType unitErrorsVariableType = null;
            
            Integer numOfFactorsAndVariates = variableTypeList.getFactors().getVariableTypes().size()+variableTypeList.getVariates().getVariableTypes().size()+1;
            for(int i = 3; i < csvHeader.length; i += 2) {   //means and errors are in pair, so just get the word before _
                String root = csvHeader[i] != null ? csvHeader[i].substring(0, csvHeader[i].lastIndexOf("_")) : "";
                if(!"".equals(root)) {
                    //Means
                    originalVariableType = variableTypeListVariates.findByLocalName(root);
                    meansVariableType = cloner.deepClone(originalVariableType);
                    meansVariableType.setLocalName(root + "_Means");
                    Term termLSMean = ontologyDataManagerV2.findMethodByName("LS MEAN");
                    if(termLSMean == null) {
                        String definitionMeans = meansVariableType.getStandardVariable().getMethod().getDefinition();
                        termLSMean = ontologyDataManagerV2.addMethod("LS MEAN", definitionMeans);
                    }
                    
                    Integer stdVariableId = ontologyDataManagerV2.getStandardVariableIdByPropertyScaleMethod(
                            meansVariableType.getStandardVariable().getProperty().getId()
                    		,meansVariableType.getStandardVariable().getScale().getId()
                    		,termLSMean.getId()
                    		);
                    
                    if (stdVariableId == null){
                    	StandardVariable stdVariable = new StandardVariable();
                        stdVariable = cloner.deepClone(meansVariableType.getStandardVariable());
                        stdVariable.setId(0);
                        stdVariable.setName(meansVariableType.getLocalName());
                        stdVariable.setMethod(termLSMean);
                        
                        ontologyDataManagerV2.addStandardVariable(stdVariable);
                        meansVariableType.setStandardVariable(stdVariable);
                    	
                    }else{
                        meansVariableType.setStandardVariable(ontologyDataManagerV2.getStandardVariable(stdVariableId));
                    }
                    
                    variableTypeList.makeRoom(numOfFactorsAndVariates);
                    meansVariableType.setRank(numOfFactorsAndVariates);
                    variableTypeList.add(meansVariableType);
                    
                    stdVariableId = null;
                    //Unit Errors
                    unitErrorsVariableType = cloner.deepClone(originalVariableType);
                    unitErrorsVariableType.setLocalName(root + "_UnitErrors");
                    Term termErrorEstimate = ontologyDataManagerV2.findMethodByName("ERROR ESTIMATE");
                    if(termErrorEstimate == null) {
                        String definitionUErrors = unitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
                        termErrorEstimate = ontologyDataManagerV2.addMethod("ERROR ESTIMATE", definitionUErrors);
                    }
                    
                     stdVariableId = ontologyDataManagerV2.getStandardVariableIdByPropertyScaleMethod(
                             unitErrorsVariableType.getStandardVariable().getProperty().getId()
                    		,unitErrorsVariableType.getStandardVariable().getScale().getId()
                    		,termErrorEstimate.getId()
                    		);
                    
                    if (stdVariableId == null){
                    	StandardVariable stdVariable = new StandardVariable();
                        stdVariable = cloner.deepClone(unitErrorsVariableType.getStandardVariable());
                        stdVariable.setId(0);
                        stdVariable.setName(unitErrorsVariableType.getLocalName());
                        stdVariable.setMethod(termErrorEstimate);
                        
                        ontologyDataManagerV2.addStandardVariable(stdVariable);
                        unitErrorsVariableType.setStandardVariable(stdVariable);
                    	
                    }else{
                        unitErrorsVariableType.setStandardVariable(ontologyDataManagerV2.getStandardVariable(stdVariableId));
                    }
                   
                    variableTypeList.makeRoom(numOfFactorsAndVariates);
                    unitErrorsVariableType.setRank(numOfFactorsAndVariates);
                    variableTypeList.add(unitErrorsVariableType);
                }
            }

            

            //please make sure that the study name is unique and does not exist in the db.
            VariableList variableList = new VariableList();
            Variable variable = createVariable(TermId.DATASET_NAME.getId(), "RESULTS_TRAIT_MEANS" + "_" + workbenchProjectId + "_" + studyId , 1);
            variableTypeList.makeRoom(1);
            variable.getVariableType().setRank(1);
            variableTypeList.add(variable.getVariableType());
            
            //name of dataset Results_trait_means_<wproject_id>_<study_id>
            updateVariableType(variable.getVariableType(), "RESULTS_TRAIT_MEANS" + "_" + workbenchProjectId + "_" + studyId, "Dataset name (local)");
            variableList.add(variable);

            variable = createVariable(TermId.DATASET_TITLE.getId(), "My Dataset Description", 2);
            variableTypeList.makeRoom(1);
            variable.getVariableType().setRank(1);
            variableTypeList.add(variable.getVariableType());
            updateVariableType(variable.getVariableType(), "DATASET_TITLE", "Dataset title (local)");
            variableList.add(variable);

            variable = createVariable(TermId.DATASET_TYPE.getId(), "10070", 3);
            variableTypeList.makeRoom(1);
            variable.getVariableType().setRank(1);
            variableTypeList.add(variable.getVariableType());
            updateVariableType(variable.getVariableType(), "DATASET_TYPE", "Dataset type (local)");
            variableList.add(variable);
            DatasetValues datasetValues = new DatasetValues();
            datasetValues.setVariables(variableList);
            
       
            
            DatasetReference datasetReference = null;
            if (meansDataSet == null){
            	//save data
                //get dataset using new datasetid
            	datasetReference = studyDataManagerV2.addDataSet(studyId, variableTypeList, datasetValues);
            	meansDataSet = studyDataManagerV2.getDataSet(datasetReference.getId());
            }
            
            
            if (meansDataSetExists){
            	//TrialEnvironment env = studyDataManagerV2.getTrialEnvironmentsInDataset(meansDataSet.getId()).findOnlyOneByLocalName(csvHeader[0], traitsAndMeans.get(csvHeader[0]).get(0));
            	if (studyDataManagerV2.getDataSet(meansDataSet.getId()).getLocationIds().contains(ndLocationId)){
                		studyDataManagerV2.deleteExperimentsByLocation(meansDataSet.getId(), ndLocationId);
            	}
            }
            
   
            ArrayList<String> environments = traitsAndMeans.get(csvHeader[0]);
            for(int i = 0; i < environments.size(); i++) {
               
            	Stock stock = stocks.findOnlyOneByLocalName(csvHeader[1], traitsAndMeans.get(csvHeader[1]).get(i));
            	if (stock != null){
	            	ExperimentValues experimentRow = new ExperimentValues();
	            	experimentRow.setGermplasmId(stock.getId());
	            	experimentRow.setLocationId(ndLocationId);//TODO - i believe this should be changed as ssa will now support multiple environments
	            		
		            	List<Variable> list = new ArrayList<Variable>();
		            	Boolean variateHasValue = false;
		                for(int j = 3; j < csvHeader.length; j++) {
		                	if (meansDataSetExists){
		                		if (meansDataSet.getVariableTypes().getVariates().findByLocalName(csvHeader[j]) == null) continue;
		                	}
		                	
		                	String variableValue = traitsAndMeans.get(csvHeader[j]).get(i).trim();
		                	if (!variableValue.trim().isEmpty()) {
		                		variateHasValue = true;
		                		Variable var = new Variable( meansDataSet.getVariableTypes().findByLocalName(csvHeader[j]), variableValue);
			                	list.add(var);
		                	}
		                	
		                }
		            VariableList variableList1 = new VariableList();
		            variableList1.setVariables(list);
	                experimentRow.setVariableList(variableList1);
	                if (variateHasValue) studyDataManagerV2.addExperiment(meansDataSet.getId(), ExperimentType.AVERAGE, experimentRow);
	               
            	}
            }
            
            //GCP-6209
            if(heritabilityOutputFilePath!=null && !heritabilityOutputFilePath.equals("")) {
            	uploadAndSaveHeritabilitiesToDB(heritabilityOutputFilePath, studyId, trialEnvironments, dataSet);
            }
            

        } else {
            throw new Exception("Input data is empty. No data was processed.");
        }
    }


    

	private Variable createVariable(int termId, String value, int rank) throws Exception {
        StandardVariable stVar = ontologyDataManagerV2.getStandardVariable(termId);

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
    
 private DataSet additionalVariableTypes(String[] csvHeader,DataSet inputDataSet,DataSet meansDataSet) throws MiddlewareQueryException{
    	
    	List<Integer> numericTypes = new ArrayList<Integer>();
        numericTypes.add(TermId.NUMERIC_VARIABLE.getId());
        numericTypes.add(TermId.MIN_VALUE.getId());
        numericTypes.add(TermId.MAX_VALUE.getId());
        numericTypes.add(TermId.DATE_VARIABLE.getId());
        numericTypes.add(TermId.NUMERIC_DBID_VARIABLE.getId());
    	
        int rank = meansDataSet.getVariableTypes().getVariableTypes().get(meansDataSet.getVariableTypes().getVariableTypes().size()-1).getRank()+1;
        
    	List<String> inputDataSetVariateNames = new ArrayList<String>( Arrays.asList(Arrays.copyOfRange(csvHeader, 3, csvHeader.length)));
    	List<String> meansDataSetVariateNames = new ArrayList<String>();
    	
    	Iterator<String> iterator = inputDataSetVariateNames.iterator();
    	while (iterator.hasNext()){
    		 if (iterator.next().contains("_UnitErrors")) iterator.remove();
    	}
    	
    	for (VariableType var : meansDataSet.getVariableTypes().getVariates().getVariableTypes()){
    		if (!var.getStandardVariable().getMethod().getName().equalsIgnoreCase("error estimate"))
    			meansDataSetVariateNames.add(var.getLocalName().trim());
    	}
    	
    	if (meansDataSetVariateNames.size() < inputDataSetVariateNames.size()){
    		
    		inputDataSetVariateNames.removeAll(meansDataSetVariateNames);
    		
    		for (String variateName : inputDataSetVariateNames){
    			 String root = variateName.substring(0, variateName.lastIndexOf("_"));
                 if(!"".equals(root)) {
                     
                     VariableType meansVariableType = cloner.deepClone(inputDataSet.getVariableTypes().findByLocalName(root));
                     meansVariableType.setLocalName(root + "_Means");
                     Term termLSMean = ontologyDataManagerV2.findMethodByName("LS MEAN");
                     if(termLSMean == null) {
                         String definitionMeans = meansVariableType.getStandardVariable().getMethod().getDefinition();
                         termLSMean = ontologyDataManagerV2.addMethod("LS MEAN", definitionMeans);
                     }
                     
                     Integer stdVariableId = ontologyDataManagerV2.getStandardVariableIdByPropertyScaleMethod(
                             meansVariableType.getStandardVariable().getProperty().getId()
                     		,meansVariableType.getStandardVariable().getScale().getId()
                     		,termLSMean.getId()
                     		);
                     
                     if (stdVariableId == null){
                     	StandardVariable stdVariable = new StandardVariable();
                         stdVariable = cloner.deepClone(meansVariableType.getStandardVariable());
                         stdVariable.setId(0);
                         stdVariable.setName(meansVariableType.getLocalName());
                         stdVariable.setMethod(termLSMean);
                         
                         ontologyDataManagerV2.addStandardVariable(stdVariable);
                         meansVariableType.setStandardVariable(stdVariable);
                     	
                     }else{
                         meansVariableType.setStandardVariable(ontologyDataManagerV2.getStandardVariable(stdVariableId));
                     }
                     
                    
                     meansVariableType.setRank(rank);
                     try{ studyDataManagerV2.addDataSetVariableType(meansDataSet.getId(), meansVariableType); rank++;}
                     	catch(MiddlewareQueryException e ) {  }
                    
                     
                     stdVariableId = null;
                     //Unit Errors
                     VariableType unitErrorsVariableType = cloner.deepClone(inputDataSet.getVariableTypes().findByLocalName(root));
                     unitErrorsVariableType.setLocalName(root + "_UnitErrors");
                     Term termErrorEstimate = ontologyDataManagerV2.findMethodByName("ERROR ESTIMATE");
                     if(termErrorEstimate == null) {
                         String definitionUErrors = unitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
                         termErrorEstimate = ontologyDataManagerV2.addMethod("ERROR ESTIMATE", definitionUErrors);
                     }
                     
                      stdVariableId = ontologyDataManagerV2.getStandardVariableIdByPropertyScaleMethod(
                              unitErrorsVariableType.getStandardVariable().getProperty().getId()
                     		,unitErrorsVariableType.getStandardVariable().getScale().getId()
                     		,termErrorEstimate.getId()
                     		);
                     
                     if (stdVariableId == null){
                     	StandardVariable stdVariable = new StandardVariable();
                         stdVariable = cloner.deepClone(unitErrorsVariableType.getStandardVariable());
                         stdVariable.setId(0);
                         stdVariable.setName(unitErrorsVariableType.getLocalName());
                         stdVariable.setMethod(termErrorEstimate);
                         
                         ontologyDataManagerV2.addStandardVariable(stdVariable);
                         unitErrorsVariableType.setStandardVariable(stdVariable);
                     	
                     }else{
                         unitErrorsVariableType.setStandardVariable(ontologyDataManagerV2.getStandardVariable(stdVariableId));
                     }
                    
                   
                     unitErrorsVariableType.setRank(rank);
                     try{ studyDataManagerV2.addDataSetVariableType(meansDataSet.getId(), unitErrorsVariableType); rank++;}
                  		catch(MiddlewareQueryException e ) {  }
                     
                 }
    			
    		}//end of for
    		
    		return studyDataManagerV2.getDataSet(meansDataSet.getId());
    	}
    	
    	return meansDataSet;
    	
    	
    }

	
	public void deleteDataSet(Integer dataSetId) throws Exception {
		// TODO Auto-generated method stub
		studyDataManagerV2.deleteDataSet(dataSetId);
	}
	
	private void uploadAndSaveHeritabilitiesToDB(
			String heritabilityOutputFilePath, int studyId, 
			TrialEnvironments trialEnvironments, DataSet dataSet) throws Exception {
    	
		try {
        	Map<String, ArrayList<Map<String,String>>> environmentAndHeritability = heritabilityCSVUtil.csvToMap(heritabilityOutputFilePath);
    	
	    	int trialDatasetId = studyId-1;//assumption: the id of the trial dataset is always lower by 1 
	    	DataSet trialDataSet = studyDataManagerV2.getDataSet(trialDatasetId);
	    	
	 
	        VariableTypeList variableTypeListVariates = dataSet.getVariableTypes().getVariates();//used in getting the new project properties
	        VariableType originalVariableType = null;
	        VariableType heritabilityVariableType = null;
	        Term termHeritability = ontologyDataManagerV2.findMethodByName("Heritability");
	        if(termHeritability == null) {
	            throw new Exception("Heritability Method does not exist.");
	        }
	        
	        Set<String> environments = environmentAndHeritability.keySet();
	        List<ExperimentValues> experimentValues = new ArrayList<ExperimentValues>();
	        VariableTypeList variableTypeList = new VariableTypeList();//list that will contain all heritability project properties
	        List<Integer> locationIds = new ArrayList<Integer>();
	        int i = 0;
	    	for(String env : environments) {
	    		
	            String[] siteAndTrialInstance = env.split("\\|");
	        	String site = siteAndTrialInstance[0];
	        	String trial = siteAndTrialInstance[1];
	        	
	        	log.info("prepare experiment values per location, "+site+"="+trial);
	        	//--------- prepare experiment values per location ------------------------------------------------------//
	            TrialEnvironment trialEnv = trialEnvironments.findOnlyOneByLocalName(site, trial);
	            int ndLocationId = trialEnv.getId();
	            log.info("ndLocationId ="+ndLocationId);
	            locationIds.add(ndLocationId);
	            List<Variable> traits = new ArrayList<Variable>();
	            VariableList variableList = new VariableList();
	            variableList.setVariables(traits);
	            ExperimentValues e = new ExperimentValues();
	            e.setVariableList(variableList);
	            e.setLocationId(ndLocationId);
	            experimentValues.add(e);
	            
	            log.info("prepare the heritability project properties if necessary");
	        	//---------- prepare the heritability project properties if necessary-------------------------------------//
	            List<Map<String,String>> heritabilityTraitList = environmentAndHeritability.get(env);
	            int lastRank = trialDataSet.getVariableTypes().size();
	            for(Map<String,String> traitHeritability : heritabilityTraitList) {
	            	String trait = traitHeritability.keySet().iterator().next();
	            	String heritability = traitHeritability.get(trait);
	            	String localName = trait + "_Heritability";
	            	
	            	//check if the heritablity trait is already existing 
	            	heritabilityVariableType = trialDataSet.findVariableTypeByLocalName(localName);
	            	
	            	if(i == 0 && heritabilityVariableType==null) {//this means we need to append the traits in the dataset project properties
	            		log.info("heritability project property not found.. need to add "+localName);
	        			originalVariableType = variableTypeListVariates.findByLocalName(trait);
	    	            heritabilityVariableType = cloner.deepClone(originalVariableType);
	    	            heritabilityVariableType.setLocalName(localName);
	    	            
	    	            
	    	            
	    	            Integer stdVariableId = ontologyDataManagerV2.getStandardVariableIdByPropertyScaleMethod(
	    	            		heritabilityVariableType.getStandardVariable().getProperty().getId(),
	    	            		heritabilityVariableType.getStandardVariable().getScale().getId(),
	    	            		termHeritability.getId());
	    	            
	    	            if (stdVariableId == null){
	    	            	StandardVariable stdVariable = new StandardVariable();
	    	                stdVariable = cloner.deepClone(heritabilityVariableType.getStandardVariable());
	    	                stdVariable.setId(0);
	    	                stdVariable.setName(heritabilityVariableType.getLocalName());
	    	                stdVariable.setMethod(termHeritability);
	    	                
	    	                //check if localname is already used
	    	                Term existingStdVar = ontologyDataManagerV2.findTermByName(stdVariable.getName(), CvId.VARIABLES);
	    	                if (existingStdVar != null){
	    	                	//rename 
	    	                	stdVariable.setName(heritabilityVariableType.getLocalName()+"_1");
	    	                }
	    	                ontologyDataManagerV2.addStandardVariable(stdVariable);
	    	                heritabilityVariableType.setStandardVariable(stdVariable);
	    	                log.info("added standard variable "+heritabilityVariableType.getStandardVariable().getName());
	    	            }else{
	    	            	heritabilityVariableType.setStandardVariable(ontologyDataManagerV2.getStandardVariable(stdVariableId));
	    	            	log.info("reused standard variable "+heritabilityVariableType.getStandardVariable().getName());	    	            	
	    	            }
	    	            
	    	            heritabilityVariableType.setRank(++lastRank);
	    	            variableTypeList.add(heritabilityVariableType);
	    	            trialDataSet.getVariableTypes().add(heritabilityVariableType);//this will add the newly added variable
	            	}
	            	
	            	//---------- prepare experiments -------------------------------------//
	            	if(heritabilityVariableType!=null) {
	            		Variable var = new Variable(heritabilityVariableType,heritability);
	            		e.getVariableList().getVariables().add(var);
	            		log.info("preparing experiment variable "+heritabilityVariableType.getLocalName()+ 
	            				" with value "+heritability);
	            	}
	            }
	            
	            i++;
	        }
	    	
	    	//------------ save project properties and experiments ----------------------------------//
	        DmsProject project = new DmsProject();
	        project.setProjectId(trialDatasetId);
	        studyDataManagerV2.saveTrialDatasetSummary(project,variableTypeList, experimentValues, locationIds);
	        
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
        
	}
	
	

}
