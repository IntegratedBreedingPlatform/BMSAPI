package org.generationcp.ibpworkbench.service;

import au.com.bytecode.opencsv.CSVReader;
import com.rits.cloning.Cloner;
import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.model.TraitsAndMeans;
import org.generationcp.ibpworkbench.util.TraitsAndMeansCSVUtil;
import org.generationcp.ibpworkbench.util.TraitsAndMeansCSVUtil2;
import org.generationcp.middleware.v2.domain.*;
import org.generationcp.middleware.v2.manager.OntologyDataManagerImpl;
import org.generationcp.middleware.v2.manager.StudyDataManagerImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
    private StudyDataManagerImpl studyDataManagerV2;
    @Autowired
    private OntologyDataManagerImpl ontologyDataManagerV2;
    @Autowired
    private Cloner cloner;

    public void execute(Map<String, String> params, List<String> errors) throws Exception {
        String fileName = params.get(WebAPIConstants.FILENAME.getParamValue());
        String workbenchProjectId = params.get(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue());
        Map<String, ArrayList<String>> traitsAndMeans = traitsAndMeansCSVUtil2.csvToMap(fileName);
        System.out.println("Traits and Means: " + traitsAndMeans);

        int ndLocationId;
        int stockId;

        //call middleware api and save
        if(!traitsAndMeans.isEmpty()) {
            String[] header = traitsAndMeans.keySet().toArray(new String[0]);         //csv header
            int inputDatasetId = Integer.valueOf(params.get(WebAPIConstants.INPUT_DATASET_ID.getParamValue()));
            TrialEnvironments trialEnvironments = studyDataManagerV2.getTrialEnvironmentsInDataset(inputDatasetId);
            //environment, env value
            TrialEnvironment trialEnv = trialEnvironments.findOnlyOneByLocalName(header[0], traitsAndMeans.get(header[0]).get(0));
            ndLocationId = trialEnv.getId();
            Stocks stocks = studyDataManagerV2.getStocksInDataset(inputDatasetId);
            Stock stock = stocks.findOnlyOneByLocalName(header[1], traitsAndMeans.get(header[1]).get(0));    //???
            stockId = stock.getId();      //germPlasmId

            DataSet dataSet = studyDataManagerV2.getDataSet(inputDatasetId);
            VariableTypeList variableTypeList = dataSet.getVariableTypes();
            //get variates only
            VariableTypeList variableTypeListVariates = variableTypeList.getVariates();
            VariableType mat50VariableType = null;
            VariableType mat50UnitErrorsVariableType = null;
            for(int i = 2; i < header.length; i += 2) {   //means and errors are in pair, so just get the word before _
                String root = header[i] != null ? header[i].split("_")[0] : "";
                if(!"".equals(root)) {
                    mat50VariableType = variableTypeListVariates.findByLocalName(root);
                    VariableType mat50MeansVariableType = cloner.deepClone(mat50VariableType);
                    mat50MeansVariableType.setLocalName(root + "_Means");
                    //String definition = mat50MeansVariableType.getStandardVariable().getMethod().getDefinition();
                    //Term term = ontologyDataManagerV2.addMethod("LS MEAN", definition);
                    Term term = ontologyDataManagerV2.getTermById(16090);
                    mat50MeansVariableType.getStandardVariable().setMethod(term);
                    variableTypeList.add(mat50MeansVariableType);

                    mat50UnitErrorsVariableType = cloner.deepClone(mat50VariableType);
                    mat50UnitErrorsVariableType.setLocalName(root + "_UnitErrors");
                    //definition = mat50UnitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
                    //term = ontologyDataManagerV2.addMethod("ERROR ESTIMATE", definition);
                    term = ontologyDataManagerV2.getTermById(16095);
                    mat50UnitErrorsVariableType.getStandardVariable().setMethod(term);  //correct method for setting term?????
                    variableTypeList.add(mat50UnitErrorsVariableType);
                }
            }

            /*VariableType mat50VariableType = variableTypeListVariates.findByLocalName("MAT50");
            VariableType mat50MeansVariableType = cloner.deepClone(mat50VariableType);
            mat50MeansVariableType.setLocalName("MAT50_Means");
            String definition = mat50MeansVariableType.getStandardVariable().getMethod().getDefinition();
            Term term = ontologyDataManagerV2.addMethod("LS MEAN", definition);
            mat50MeansVariableType.getStandardVariable().setMethod(term);  //correct method for setting term?????
            variableTypeList.add(mat50MeansVariableType);

            VariableType mat50UnitErrorsVariableType = cloner.deepClone(mat50VariableType);
            mat50UnitErrorsVariableType.setLocalName("MAT50_UnitErrors");
            definition = mat50UnitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
            term = ontologyDataManagerV2.addMethod("ERROR ESTIMATE", definition);
            mat50UnitErrorsVariableType.getStandardVariable().setMethod(term);  //correct method for setting term?????
            variableTypeList.add(mat50UnitErrorsVariableType);

            VariableType podwtVariableType = variableTypeListVariates.findByLocalName("PODWT");
            VariableType podwtMeansVariableType = cloner.deepClone(podwtVariableType);
            podwtMeansVariableType.setLocalName("PODWT_Means");
            definition = podwtMeansVariableType.getStandardVariable().getMethod().getDefinition();
            term = ontologyDataManagerV2.addMethod("LS MEAN", definition);
            podwtMeansVariableType.getStandardVariable().setMethod(term);  //correct method for setting term?????
            variableTypeList.add(podwtMeansVariableType);

            VariableType podwtUnitErrorsVariableType = cloner.deepClone(podwtVariableType);
            podwtUnitErrorsVariableType.setLocalName("PODWT_UnitErrors");
            definition = podwtUnitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
            term = ontologyDataManagerV2.addMethod("ERROR ESTIMATE", definition);
            podwtUnitErrorsVariableType.getStandardVariable().setMethod(term);  //correct method for setting term?????
            variableTypeList.add(podwtUnitErrorsVariableType);

            VariableType seedwtVariableType = variableTypeListVariates.findByLocalName("SEEDWT");
            VariableType seedwtMeansVariableType = cloner.deepClone(seedwtVariableType);
            seedwtMeansVariableType.setLocalName("SEEDWT_Means");
            definition = seedwtMeansVariableType.getStandardVariable().getMethod().getDefinition();
            term = ontologyDataManagerV2.addMethod("LS MEAN", definition);
            seedwtMeansVariableType.getStandardVariable().setMethod(term);  //correct method for setting term?????
            variableTypeList.add(seedwtMeansVariableType);

            VariableType seedwtUnitErrorsVariableType = cloner.deepClone(seedwtVariableType);
            seedwtUnitErrorsVariableType.setLocalName("SEEDWT_UnitErrors");
            definition = seedwtUnitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
            term = ontologyDataManagerV2.addMethod("ERROR ESTIMATE", definition);
            seedwtUnitErrorsVariableType.getStandardVariable().setMethod(term);  //correct method for setting term?????
            variableTypeList.add(seedwtUnitErrorsVariableType);*/

            int studyId = Integer.valueOf(params.get(WebAPIConstants.STUDY_ID.getParamValue()));

            //please make sure that the study name is unique and does not exist in the db.
            VariableList variableList = new VariableList();
            Variable variable = createVariable(TermId.DATASET_NAME.getId(), fileName + "_" + workbenchProjectId + "_" + studyId , 1);
            variableTypeList.add(variable.getVariableType());
            updateVariableType(variable.getVariableType(), "DATASET_NAME", "Dataset name (local)");
            variableList.add(variable);

            variable = createVariable(TermId.DATASET_TITLE.getId(), "My Dataset Description", 2);
            variableTypeList.add(variable.getVariableType());
            updateVariableType(variable.getVariableType(), "DATASET_TITLE", "Dataset title (local)");
            variableList.add(variable);

            variable = createVariable(TermId.DATASET_TYPE.getId(), "10070", 3);
            variableTypeList.add(variable.getVariableType());
            updateVariableType(variable.getVariableType(), "DATASET_TYPE", "Dataset type (local)");
            variableList.add(variable);
            DatasetValues datasetValues = new DatasetValues();
            datasetValues.setVariables(variableList);
            DatasetReference datasetReference = studyDataManagerV2.addDataSet(studyId, variableTypeList, datasetValues);

            //save data
            //get dataset using new datasetid
            DataSet newDataset = studyDataManagerV2.getDataSet(datasetReference.getId());
            ExperimentValues experimentValues = null;
            ArrayList<String> environments = traitsAndMeans.get(header[0]);
            for(int i = 0; i < environments.size(); i++) {
                for(int j = 2; j < header.length; j++) {   //means and errors are in pair, so just get the word before _
                    if(header[j] != null) {
                        experimentValues = createExperimentValues(newDataset,
                                traitsAndMeans.get(header[j]).get(i), ndLocationId, stockId);
                        studyDataManagerV2.addExperiment(newDataset.getId(), ExperimentType.AVERAGE, experimentValues);
                    }
                }
                /*experimentValues = createExperimentValues(newDataset,
                        traitsAndMeans.get("MAT50_Means").get(i), ndLocationId, stockId);
                studyDataManagerV2.addExperiment(newDataset.getId(), ExperimentType.AVERAGE, experimentValues);

                experimentValues = createExperimentValues(newDataset,
                        traitsAndMeans.get("MAT50_UnitErrors").get(i), ndLocationId, stockId);
                studyDataManagerV2.addExperiment(newDataset.getId(), ExperimentType.AVERAGE, experimentValues);

                experimentValues = createExperimentValues(newDataset,
                        traitsAndMeans.get("PODWT_Means").get(i), ndLocationId, stockId);
                studyDataManagerV2.addExperiment(newDataset.getId(), ExperimentType.AVERAGE, experimentValues);

                experimentValues = createExperimentValues(newDataset,
                        traitsAndMeans.get("PODWT_UnitErrors").get(i), ndLocationId, stockId);
                studyDataManagerV2.addExperiment(newDataset.getId(), ExperimentType.AVERAGE, experimentValues);

                experimentValues = createExperimentValues(newDataset,
                        traitsAndMeans.get("SEEDWT_Means").get(i), ndLocationId, stockId);
                studyDataManagerV2.addExperiment(newDataset.getId(), ExperimentType.AVERAGE, experimentValues);

                experimentValues = createExperimentValues(newDataset,
                        traitsAndMeans.get("SEEDWT_UnitErrors").get(i), ndLocationId, stockId);
                studyDataManagerV2.addExperiment(newDataset.getId(), ExperimentType.AVERAGE, experimentValues);*/
            }

        } else {
            throw new Exception("Input data is empty. No data was processed.");
        }
    }

    private ExperimentValues createExperimentValues(DataSet newDataset, String cellValue, int locationId, int stockId) {
        ExperimentValues experimentValues = new ExperimentValues();
        experimentValues.setLocationId(locationId);
        experimentValues.setGermplasmId(stockId);
        VariableTypeList newVariableTypeList = newDataset.getVariableTypes();
        VariableType variableType = newVariableTypeList.findByLocalName("MAT50_Means");
        List<Variable> varList = new ArrayList<Variable>();
        Variable variable = new Variable(variableType, cellValue);
        varList.add(variable);
        VariableList list = new VariableList();
        list.setVariables(varList);
        experimentValues.setVariableList(list);
        return experimentValues;
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
}
