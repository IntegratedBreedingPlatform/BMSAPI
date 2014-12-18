package org.generationcp.ibpworkbench.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.exceptions.IBPWebServiceException;
import org.generationcp.ibpworkbench.util.CSVUtil;
import org.generationcp.ibpworkbench.util.OutlierCSV;
import org.generationcp.ibpworkbench.util.SummaryStatsCSV;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.DatasetValues;
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
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rits.cloning.Cloner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BreedingViewServiceTest {
	
	private static final String AD_NUM_VALUES = "AD_NumValues";
	private static final String AD_HERITABILITY = "AD_Heritability";
	private static final int NUM_VALUES_TERMID = 6354;
	private static final int HERITABILITY_TERMID = 7652;
	private static final int DATASET_TITLE_STANDARD_VAR_ID = 8155;
	private static final int DATASET_STANDARD_VAR_ID = 8160;
	private static final int STUDY_STANDARD_VAR_ID = 8150;
	private static final String LS_MEAN = "LS MEAN";
	private static final int LS_MEAN_TERMID = 33321;
	private static final String ENV_NAME_CIMMYT_HARARE = "CIMMYT, Harare";
	private static final String SUMMARY_VALUE_2 = "66.11";
	private static final String SUMMARY_VALUE_1 = "98.44";
	private static final String OUTPUT_DATASET_ID = "4";
	private static final String INPUT_DATASET_ID = "3";
	private static final String WORKBENCH_PROJECT_ID = "1";
	private static final String EMPTY_VALUE = "";
	private static final String AD = "AD";
	private static final String NUM_VALUES = "NumValues";
	private static final String HERITABILITY = "Heritability";
	private static final String TRAIT = "Trait";
	private static final String AD_UNIT_ERRORS = "AD_UnitErrors";
	private static final String AD_MEANS = "AD_Means";
	private static final String PH_UNIT_ERRORS = "PH_UnitErrors";
	private static final String PH_MEANS = "PH_Means";
	private static final String DESIG = "DESIG";
	private static final String ENTRY = "ENTRY";
	private static final String SITE = "SITE";
	private static final String AD_UNITERRORS_VALUE = "44.99";
	private static final String AD_MEANS_VALUE = "23.33";
	private static final String PH_UNITERRORS_VALUE = "66.44";
	private static final String PH_MEANS_VALUE = "99.99";
	private static final String DESIG_VALUE = "MYDESIGNATION-ABCDDD";
	private static final String ENTRY_VALUE = "2";
	private static final String SITE_VALUE = "CIMMYT; Harare";
	
	@Mock StudyDataManager studyDataManager;
	@Mock OntologyDataManager ontologyDataManager;
	@Mock WorkbenchDataManager workbenchDataManager;
	@Mock Map<String, String> nameToAliasMapping;
	@Mock Map<String, String> params;
	@Mock CSVUtil csvUtil;
	@Mock SummaryStatsCSV summaryStatsCSV;
	@Mock OutlierCSV outlierCSV;
	 
	@Mock DataSet inputDataSet;
	@Mock DataSet meansDataSet;
	@Mock DataSet trialDataSet;
	@Mock TrialEnvironments trialEnvironments;
	@Mock TrialEnvironment trialEnvironment;
	
	@Mock Study study;
	@Mock Stocks stocks;

	Map<String,ArrayList<String>> meansInput;
	Map<String,Map<String,ArrayList<String>>> summaryStatisticsInput;
	BreedingViewServiceImpl service;
	
	List<VariableType> factorVariableTypes = new ArrayList<VariableType>();
	List<VariableType> variateVariableTypes = new ArrayList<VariableType>();
	
	VariableType variate_AD;
	VariableType heritability_AD;
	VariableType numValues_AD;
	
	Term lsMean = new Term();
	Term heritability = new Term();
	Term numValues = new Term();
	
	@Before
	public void setUp() throws Exception {
		
		MockitoAnnotations.initMocks(this);
		service = spy(new BreedingViewServiceImpl());
		
		doReturn(csvUtil).when(service).getCsvUtil();
		doReturn(summaryStatsCSV).when(service).getSummaryStatsCSV(anyString());
		doReturn(outlierCSV).when(service).getOutlierCSV(anyString());
		doReturn(nameToAliasMapping).when(service).getNameToAliasMapping();
		
		service.setWorkbenchDataManager(workbenchDataManager);
		service.setStudyDataManager(studyDataManager);
		service.setOntologyDataManager(ontologyDataManager);
		service.setNameToAliasMapping(nameToAliasMapping);
		service.setCloner(new Cloner());
		
		when(params.get(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue())).thenReturn(EMPTY_VALUE);
		when(params.get(WebAPIConstants.SUMMARY_OUTPUT_FILE_PATH.getParamValue())).thenReturn(EMPTY_VALUE);
		when(params.get(WebAPIConstants.OUTLIER_OUTPUT_FILE_PATH.getParamValue())).thenReturn(EMPTY_VALUE);
		when(params.get(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue())).thenReturn(WORKBENCH_PROJECT_ID);
		when(params.get(WebAPIConstants.STUDY_ID.getParamValue())).thenReturn(ENTRY_VALUE);
		when(params.get(WebAPIConstants.INPUT_DATASET_ID.getParamValue())).thenReturn(INPUT_DATASET_ID);
		when(params.get(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue())).thenReturn(OUTPUT_DATASET_ID);
		
		when(study.getId()).thenReturn(2);
		when(study.getName()).thenReturn("STUDY-2");
		
		meansInput = new LinkedHashMap<String,ArrayList<String>>();
		
		ArrayList<String> val1 = new ArrayList<String>();
		val1.add(SITE_VALUE);
		ArrayList<String> val2 = new ArrayList<String>();
		val2.add(ENTRY_VALUE);
		ArrayList<String> val3 = new ArrayList<String>();
		val3.add(DESIG_VALUE);
		ArrayList<String> val4 = new ArrayList<String>();
		val4.add(AD_MEANS_VALUE);
		ArrayList<String> val5 = new ArrayList<String>();
		val5.add(AD_UNITERRORS_VALUE);
		
		meansInput.put(SITE, val1);
		meansInput.put(ENTRY, val2);
		meansInput.put(DESIG, val3);
		meansInput.put(AD_MEANS, val4);
		meansInput.put(AD_UNIT_ERRORS, val5);
		
		when(csvUtil.csvToMap(anyString())).thenReturn(meansInput);
		
		
		summaryStatisticsInput =  new LinkedHashMap<String,Map<String,ArrayList<String>>>();
		Map<String, ArrayList<String>> data = new LinkedHashMap<String, ArrayList<String>>();
		
		

		ArrayList<String> summaryValues = new ArrayList<String>();
		summaryValues.add(SUMMARY_VALUE_1);
		summaryValues.add(SUMMARY_VALUE_2);
		
		data.put(AD,summaryValues);
		summaryStatisticsInput.put(SITE_VALUE, data);
		
		
		List<String> header = new ArrayList<String>();
		header.add(SITE);
		header.add(TRAIT);
		header.add(HERITABILITY);
		header.add(NUM_VALUES);
		
		List<String> headerStats = new ArrayList<String>();
		headerStats.add(HERITABILITY);
		headerStats.add(NUM_VALUES);
		
		when(summaryStatsCSV.getData()).thenReturn(summaryStatisticsInput);
		when(summaryStatsCSV.getTrialHeader()).thenReturn(SITE);
		when(summaryStatsCSV.getHeader()).thenReturn(header);
		when(summaryStatsCSV.getHeaderStats()).thenReturn(headerStats);
		
		
		variate_AD = createVariateVariableType(AD);
		heritability_AD = createVariateVariableType(AD_HERITABILITY);
		numValues_AD = createVariateVariableType(AD_NUM_VALUES);
		
		lsMean.setId(LS_MEAN_TERMID);
		lsMean.setName(LS_MEAN);
		
		heritability.setId(HERITABILITY_TERMID);
		heritability.setName(HERITABILITY);
		
		numValues.setId(NUM_VALUES_TERMID);
		numValues.setName(NUM_VALUES);
		
		
		factorVariableTypes.add(createSiteFactorVariableType());
		factorVariableTypes.add(createGermplasmFactorVariableType(ENTRY));
		factorVariableTypes.add(createGermplasmFactorVariableType(DESIG));
		variateVariableTypes.add(variate_AD);
	
	}
	
	
	@Test
	public void testExecute_SaveTheMeansDataWithCommaInEnvironments() throws Exception {
		
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Set<TrialEnvironment> trialEnvironmentList = new HashSet<TrialEnvironment>();
		trialEnvironmentList.add(trialEnvironment);
		
		StandardVariable studyStdVar = new StandardVariable();
		studyStdVar.setId(STUDY_STANDARD_VAR_ID);
		studyStdVar.setPhenotypicType(PhenotypicType.STUDY);
		
		StandardVariable dataSetStdVar = new StandardVariable();
		dataSetStdVar.setId(DATASET_STANDARD_VAR_ID);
		dataSetStdVar.setPhenotypicType(PhenotypicType.DATASET);
		
		StandardVariable titleStdVar = new StandardVariable();
		titleStdVar.setId(DATASET_TITLE_STANDARD_VAR_ID);
		
		VariableTypeList meansVariableTypeList = new VariableTypeList();

		doReturn(meansVariableTypeList).when(service).getMeansVariableTypeList();
		
		when(studyDataManager.getDataSetsByType(anyInt(), (DataSetType)anyObject())).thenReturn(dataSets);
		when(studyDataManager.getTrialEnvironmentsInDataset(anyInt())).thenReturn(trialEnvironments);
		when(studyDataManager.getDataSet(3)).thenReturn(createInputDataSet());
		when(studyDataManager.getDataSet(4)).thenReturn(null).thenReturn(meansDataSet);
		when(studyDataManager.getStudy(anyInt())).thenReturn(study);
		when(studyDataManager.addDataSet(anyInt(), (VariableTypeList) anyObject(), (DatasetValues) anyObject(), anyString())).thenReturn(new DatasetReference(4, EMPTY_VALUE));
		when(studyDataManager.getStocksInDataset(anyInt())).thenReturn(stocks);
		
		when(ontologyDataManager.addMethod(anyString(), anyString())).thenReturn(lsMean);
		when(ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(anyInt(),anyInt(),anyInt(),(PhenotypicType) anyObject())).thenReturn(null);
		when(ontologyDataManager.getStandardVariable(8150)).thenReturn(studyStdVar);
		when(ontologyDataManager.getStandardVariable(8155)).thenReturn(titleStdVar);
		when(ontologyDataManager.getStandardVariable(8160)).thenReturn(dataSetStdVar);
	
		
		when(meansDataSet.getVariableTypes()).thenReturn(meansVariableTypeList);
		
		when(trialEnvironments.getTrialEnvironments()).thenReturn(trialEnvironmentList);
		
		when(trialEnvironment.getId()).thenReturn(1);
		when(trialEnvironment.getVariables()).thenReturn(mock(VariableList.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString())).thenReturn(mock(Variable.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString()).getValue()).thenReturn(ENV_NAME_CIMMYT_HARARE);
		
		when(stocks.findOnlyOneByLocalName(anyString(),anyString())).thenReturn(mock(Stock.class));
		when(stocks.findOnlyOneByLocalName(anyString(),anyString()).getId()).thenReturn(1);
		
	
		List<String> errors = new ArrayList<String>();
		
		try {
			
			service.execute(params, errors);
			
			List<ExperimentValues> experimentValues = service.getExperimentValuesList();
			
			assertFalse("ExperimentValues list should not be empty", experimentValues.isEmpty());
			
			ExperimentValues values = experimentValues.get(0);
			
			assertEquals("The location id of CIMMYT, Harare environment should be 1", 1, values.getLocationId().intValue());
			assertEquals("The number of new variates in experiment should match the input", 3, values.getVariableList().getVariables().size());
			
			
		} catch (IBPWebServiceException e) {
			
			fail("Failed to execute the Web Service");
		}
		
	}
	
	@Test
	public void testExecute_SaveTheMeansDataWithExistingMeansDataSet() throws Exception {
		
		ArrayList<String> val1 = new ArrayList<String>();
		val1.add(PH_MEANS_VALUE);
		ArrayList<String> val2 = new ArrayList<String>();
		val2.add(PH_UNITERRORS_VALUE);
	
		meansInput.put(PH_MEANS, val1);
		meansInput.put(PH_UNIT_ERRORS, val2);
		
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Set<TrialEnvironment> trialEnvironmentList = new HashSet<TrialEnvironment>();
		trialEnvironmentList.add(trialEnvironment);
		
		StandardVariable studyStdVar = new StandardVariable();
		studyStdVar.setId(STUDY_STANDARD_VAR_ID);
		studyStdVar.setPhenotypicType(PhenotypicType.STUDY);
		
		StandardVariable dataSetStdVar = new StandardVariable();
		dataSetStdVar.setId(DATASET_STANDARD_VAR_ID);
		dataSetStdVar.setPhenotypicType(PhenotypicType.DATASET);
		
		StandardVariable titleStdVar = new StandardVariable();
		titleStdVar.setId(DATASET_TITLE_STANDARD_VAR_ID);
		
		
		DataSet trialDataSet = createInputDataSet();
		trialDataSet.getVariableTypes().add(this.createVariateVariableType("PH"));
		DataSet existingMeansDataSet = createMeansDataSet();

		//doReturn(meansVariableTypeList).when(service).getMeansVariableTypeList();
		
		when(studyDataManager.getDataSetsByType(anyInt(), (DataSetType)anyObject())).thenReturn(dataSets);
		when(studyDataManager.getTrialEnvironmentsInDataset(anyInt())).thenReturn(trialEnvironments);
		when(studyDataManager.getDataSet(3)).thenReturn(trialDataSet);
		when(studyDataManager.getDataSet(4)).thenReturn(existingMeansDataSet);
		when(studyDataManager.getStudy(anyInt())).thenReturn(study);
		when(studyDataManager.addDataSet(anyInt(), (VariableTypeList) anyObject(), (DatasetValues) anyObject())).thenReturn(new DatasetReference(4, EMPTY_VALUE));
		when(studyDataManager.getStocksInDataset(anyInt())).thenReturn(stocks);
		
		when(ontologyDataManager.addMethod(anyString(), anyString())).thenReturn(lsMean);
		when(ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(anyInt(),anyInt(),anyInt(),(PhenotypicType) anyObject())).thenReturn(null);
		when(ontologyDataManager.getStandardVariable(8150)).thenReturn(studyStdVar);
		when(ontologyDataManager.getStandardVariable(8155)).thenReturn(titleStdVar);
		when(ontologyDataManager.getStandardVariable(8160)).thenReturn(dataSetStdVar);
		
		when(trialEnvironments.getTrialEnvironments()).thenReturn(trialEnvironmentList);
		
		when(trialEnvironment.getId()).thenReturn(1);
		when(trialEnvironment.getVariables()).thenReturn(mock(VariableList.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString())).thenReturn(mock(Variable.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString()).getValue()).thenReturn(ENV_NAME_CIMMYT_HARARE);
		
		when(stocks.findOnlyOneByLocalName(anyString(),anyString())).thenReturn(mock(Stock.class));
		when(stocks.findOnlyOneByLocalName(anyString(),anyString()).getId()).thenReturn(1);
		
	
		List<String> errors = new ArrayList<String>();
		
		try {
			
			service.execute(params, errors);
			
			List<ExperimentValues> experimentValues = service.getExperimentValuesList();
			
			assertFalse("ExperimentValues list should not be empty", experimentValues.isEmpty());
			
			ExperimentValues values = experimentValues.get(0);
			
			assertEquals("The location id of CIMMYT, Harare environment should be 1", 1, values.getLocationId().intValue());
			assertEquals("The number of new variates in experiment should match the input", 2, values.getVariableList().getVariables().size());
			
			
		} catch (IBPWebServiceException e) {
			
			fail("Failed to execute the Web Service");
		}
		
	}
	
	@Test
	public void testExecute_SaveTheSummaryStatisticsData_TraitsAreSelected() throws Exception {
		
		when(trialEnvironments.findOnlyOneByLocalName(SITE, ENV_NAME_CIMMYT_HARARE)).thenReturn(trialEnvironment);
		when(trialEnvironment.getId()).thenReturn(1);
		when(trialEnvironment.getVariables()).thenReturn(mock(VariableList.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString())).thenReturn(mock(Variable.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString()).getValue()).thenReturn(ENV_NAME_CIMMYT_HARARE);
		
		when(ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(anyInt(),anyInt(),anyInt(),(PhenotypicType) anyObject())).thenReturn(null);
		when(ontologyDataManager.findMethodByName(HERITABILITY)).thenReturn(heritability);
		when(ontologyDataManager.findMethodByName(NUM_VALUES)).thenReturn(numValues);
		
		when(studyDataManager.getDataSet(3)).thenReturn(inputDataSet);
		when(studyDataManager.getDataSet(1)).thenReturn(trialDataSet);
		
		when(inputDataSet.getVariableTypes()).thenReturn(mock(VariableTypeList.class));
		when(inputDataSet.getVariableTypes().getFactors()).thenReturn(mock(VariableTypeList.class));
		when(inputDataSet.getVariableTypes().getFactors().getVariableTypes()).thenReturn(factorVariableTypes);
		when(inputDataSet.getVariableTypes().getVariates()).thenReturn(mock(VariableTypeList.class));
		when(inputDataSet.getVariableTypes().getVariates().findByLocalName(AD)).thenReturn(variate_AD);
		when(inputDataSet.getVariableTypes().getVariates().getVariableTypes()).thenReturn(variateVariableTypes);
		
		when(nameToAliasMapping.containsValue(AD)).thenReturn(true);
		
		VariableTypeList list = new VariableTypeList();
		list.getVariableTypes().addAll(factorVariableTypes);
		when(trialDataSet.getVariableTypes()).thenReturn(list);
		when(trialDataSet.findVariableTypeByLocalName(AD_HERITABILITY)).thenReturn(null).thenReturn(heritability_AD);
		when(trialDataSet.findVariableTypeByLocalName(AD_NUM_VALUES)).thenReturn(null).thenReturn(numValues_AD);
		
		try {
			
			service.uploadAndSaveSummaryStatsToDB("", 2, trialEnvironments, inputDataSet);
			
			List<ExperimentValues> experimentValues = service.getSummaryStatsExperimentValuesList();
			VariableTypeList statsList = service.getVariableTypeListSummaryStats();
			
			assertFalse("The summary stats list must not be empty", statsList.getVariableTypes().isEmpty());
			assertFalse("ExperimentValues list should not be empty", experimentValues.isEmpty());
			assertEquals("The number of summary stats to save should match the input", 2 , experimentValues.size());			

			
		} catch (Exception e) {
			
			fail("Failed to execute uploadAndSaveSummaryStatsToDB");
		}
		
	}
	
	@Test
	public void testExecute_SaveTheSummaryStatisticsData_TraitsAreNotSelected() throws Exception {
		
		when(trialEnvironments.findOnlyOneByLocalName(SITE, ENV_NAME_CIMMYT_HARARE)).thenReturn(trialEnvironment);
		when(trialEnvironment.getId()).thenReturn(1);
		when(trialEnvironment.getVariables()).thenReturn(mock(VariableList.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString())).thenReturn(mock(Variable.class));
		when(trialEnvironment.getVariables().findByLocalName(anyString()).getValue()).thenReturn(ENV_NAME_CIMMYT_HARARE);
		
		when(ontologyDataManager.getStandardVariableIdByPropertyScaleMethodRole(anyInt(),anyInt(),anyInt(),(PhenotypicType) anyObject())).thenReturn(null);
		when(ontologyDataManager.findMethodByName(HERITABILITY)).thenReturn(heritability);
		when(ontologyDataManager.findMethodByName(NUM_VALUES)).thenReturn(numValues);
		
		when(studyDataManager.getDataSet(3)).thenReturn(inputDataSet);
		when(studyDataManager.getDataSet(1)).thenReturn(trialDataSet);
		
		when(inputDataSet.getVariableTypes()).thenReturn(mock(VariableTypeList.class));
		when(inputDataSet.getVariableTypes().getFactors()).thenReturn(mock(VariableTypeList.class));
		when(inputDataSet.getVariableTypes().getFactors().getVariableTypes()).thenReturn(factorVariableTypes);
		when(inputDataSet.getVariableTypes().getVariates()).thenReturn(mock(VariableTypeList.class));
		when(inputDataSet.getVariableTypes().getVariates().findByLocalName(AD)).thenReturn(variate_AD);
		when(inputDataSet.getVariableTypes().getVariates().getVariableTypes()).thenReturn(variateVariableTypes);
		
		when(nameToAliasMapping.containsValue(AD)).thenReturn(false);
		
		VariableTypeList list = new VariableTypeList();
		list.getVariableTypes().addAll(factorVariableTypes);
		when(trialDataSet.getVariableTypes()).thenReturn(list);
		when(trialDataSet.findVariableTypeByLocalName(AD_HERITABILITY)).thenReturn(heritability_AD);
		when(trialDataSet.findVariableTypeByLocalName(AD_NUM_VALUES)).thenReturn(numValues_AD);
		
		try {
			
			service.uploadAndSaveSummaryStatsToDB("", 2, trialEnvironments, inputDataSet);
			
			VariableTypeList statsList = service.getVariableTypeListSummaryStats();
			assertTrue("The summary stats list must be empty", statsList.getVariableTypes().isEmpty());
			
			
		} catch (Exception e) {
			
			fail("Failed to execute uploadAndSaveSummaryStatsToDB");
		}
		
	}

	
	private VariableType createVariateVariableType(String localName){
		VariableType variate = new VariableType();
		StandardVariable variateStandardVar = new StandardVariable();
		variateStandardVar.setPhenotypicType(PhenotypicType.VARIATE);
		
		Term storedIn = new Term();
		storedIn.setId(TermId.OBSERVATION_VARIATE.getId());
		
		Term dataType = new Term();
		dataType.setId(TermId.NUMERIC_VARIABLE.getId());
		
		Term method = new Term();
		method.setId(1111);
		method.setDefinition(EMPTY_VALUE);
		
		Term scale = new Term();
		scale.setDefinition(EMPTY_VALUE);
		scale.setId(22222);
		
		Term property = new Term();
		scale.setDefinition(EMPTY_VALUE);
		scale.setId(33333);
		
		variateStandardVar.setId(1234);
		variateStandardVar.setProperty(property);
		variateStandardVar.setScale(scale);
		variateStandardVar.setMethod(method);
		variateStandardVar.setStoredIn(storedIn);
		variateStandardVar.setDataType(dataType);
		variate.setLocalName(localName);
		variate.setStandardVariable(variateStandardVar);
		
		return variate;
	}
	
	private VariableType createSiteFactorVariableType(){
		
		VariableType factor = new VariableType();
		StandardVariable factorStandardVar = new StandardVariable();
		Term storedInLoc = new Term();
		storedInLoc.setId(TermId.LOCATION_ID.getId());
		factorStandardVar.setStoredIn(storedInLoc);
		factorStandardVar.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);
		factor.setLocalName(SITE);
		factor.setStandardVariable(factorStandardVar);
		
		return factor;
	}
	
	private VariableType createGermplasmFactorVariableType(String localName){
		VariableType factor = new VariableType();
		StandardVariable factorStandardVar = new StandardVariable();
		factorStandardVar.setPhenotypicType(PhenotypicType.GERMPLASM);
		
		Term storedIn = new Term();
		storedIn.setId(TermId.GERMPLASM_ENTRY_STORAGE.getId());
		
		Term dataType = new Term();
		dataType.setId(TermId.NUMERIC_DBID_VARIABLE.getId());
		
		Term method = new Term();
		method.setId(1111);
		method.setDefinition(EMPTY_VALUE);
		
		Term scale = new Term();
		scale.setDefinition(EMPTY_VALUE);
		scale.setId(22222);
		
		Term property = new Term();
		scale.setDefinition(EMPTY_VALUE);
		scale.setId(33333);
		
		factorStandardVar.setId(1234);
		factorStandardVar.setProperty(property);
		factorStandardVar.setScale(scale);
		factorStandardVar.setMethod(method);
		factorStandardVar.setStoredIn(storedIn);
		factorStandardVar.setDataType(dataType);
		factor.setLocalName(localName);
		factor.setStandardVariable(factorStandardVar);
		
		return factor;
	}
	
	private DataSet createInputDataSet(){
		
		DataSet dataSet = new DataSet();
		VariableTypeList variableTypes = new VariableTypeList();
		
		dataSet.setVariableTypes(variableTypes);
		for(VariableType factor : factorVariableTypes){
			dataSet.getVariableTypes().add(factor);
		}
		for(VariableType variate : variateVariableTypes){
			dataSet.getVariableTypes().add(variate);
		}
		
		return dataSet;
	}
	
	private DataSet createMeansDataSet(){

		DataSet dataSet = new DataSet();
		VariableTypeList variableTypes = new VariableTypeList();
		
		dataSet.setVariableTypes(variableTypes);
		for(VariableType factor : factorVariableTypes){
			dataSet.getVariableTypes().add(factor);
		}
		
		VariableType adMeans = createVariateVariableType(AD_MEANS);
		adMeans.getStandardVariable().getMethod().setName("LS MEAN");
		dataSet.getVariableTypes().add(adMeans);
		
		VariableType adUnitError = createVariateVariableType(AD_UNIT_ERRORS);
		adUnitError.getStandardVariable().getMethod().setName("ERROR ESTIMATE");
		dataSet.getVariableTypes().add(adUnitError);
		
		return dataSet;
	}

}
