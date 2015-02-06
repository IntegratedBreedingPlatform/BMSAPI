package org.generationcp.bms.resource;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.*;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.bms.web.UrlComposer;
import org.generationcp.middleware.domain.dms.*;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study")
@SuppressWarnings("unused")
public class StudyResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(StudyResource.class);
	
	private StudyDataManager studyDataManager;
	private SimpleDao simpleDao;
	private FieldbookService fieldbookService;
	
	private DataImportService dataImportService;
	private UrlComposer urlComposer;

	@Autowired
	public StudyResource(StudyDataManager studyDataManager, SimpleDao simpleDao,
			FieldbookService fieldbookService, DataImportService dataImportService, UrlComposer urlComposer) {

		this.studyDataManager = studyDataManager;
		this.fieldbookService = fieldbookService;
		this.dataImportService = dataImportService;
		this.simpleDao = simpleDao;
		this.urlComposer = urlComposer;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "redirect:/api-docs/default/study-resource";
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public List<StudySummary> listStudySummaries() throws MiddlewareQueryException {

		List<StudySummary> studySummaries = new ArrayList<StudySummary>();
		List<org.generationcp.middleware.domain.etl.StudyDetails> allStudies = new ArrayList<org.generationcp.middleware.domain.etl.StudyDetails>();
		for(StudyType studyType : StudyType.values()) {
			allStudies.addAll(this.studyDataManager.getAllStudyDetails(studyType));
		}

		for (org.generationcp.middleware.domain.etl.StudyDetails studyDetails : allStudies) {
			StudySummary summary = new StudySummary(studyDetails.getId());
			summary.setName(studyDetails.getStudyName());
			summary.setTitle(studyDetails.getTitle());
			summary.setObjective(studyDetails.getObjective());
			summary.setStartDate(studyDetails.getStartDate());
			summary.setEndDate(studyDetails.getEndDate());
			summary.setType(studyDetails.getStudyType().getName());
			summary.setStudyDetailsUrl(this.urlComposer.getStudyDetailsUrl(studyDetails.getId()));
			populateDatasetSummary(summary, studyDetails.getId());
			studySummaries.add(summary);
		}
		return studySummaries;
	}

	private void populateStudySummary(StudySummary studySummary, Study study) throws MiddlewareQueryException {
		
		studySummary.setName(study.getName());
		studySummary.setTitle(study.getTitle());
		studySummary.setObjective(study.getObjective());
		studySummary.setType(study.getType());
		studySummary.setStartDate(String.valueOf(study.getStartDate()));
		studySummary.setEndDate(String.valueOf(study.getEndDate()));
		studySummary.setStudyDetailsUrl(this.urlComposer.getStudyDetailsUrl(study.getId()));
		studySummary.setObservationDetailsUrl(this.urlComposer.getObservationDetailsUrl(study.getId()));
	}

	private void populateDatasetSummary(StudySummary studySummary, int studyId) throws MiddlewareQueryException {

		List<DatasetReference> datasetReferences = this.studyDataManager.getDatasetReferences(studyId);
		if (datasetReferences != null && !datasetReferences.isEmpty()) {
			for (DatasetReference dsRef : datasetReferences) {
				DatasetSummary dsSummary = new DatasetSummary();
				dsSummary.setId(dsRef.getId());
				dsSummary.setName(dsRef.getName());
				dsSummary.setDescription(dsRef.getDescription());
				dsSummary.setDatasetDetailUrl(this.urlComposer.getDataSetDetailsUrl(dsRef.getId()));
				studySummary.addDatasetSummary(dsSummary);
			}
		}
	}

	@RequestMapping(value = "/{studyId}", method = RequestMethod.GET)
	@ResponseBody
	public StudyDetails getStudyDetails(@PathVariable Integer studyId)
			throws MiddlewareQueryException {

		Study study = studyDataManager.getStudy(studyId);
		if (study == null) {
			throw new NotFoundException();
		}

		StudyDetails studyDetails = new StudyDetails(study.getId());
		populateStudySummary(studyDetails, study);
		populateDatasetSummary(studyDetails, study.getId());

		// factors/metadaa/properties/information/conditions of the study

		List<Variable> conditions = study.getConditions().getVariables();
		VariableTypeList factors = studyDataManager.getAllStudyFactors(Integer.valueOf(studyId));
		List<VariableType> factorDetails = factors.getVariableTypes();
		for (VariableType factorDetail : factorDetails) {
			String value = null;
			for (Variable condition : conditions) {
				String conditionName = condition.getVariableType().getLocalName();
				if (factorDetail.getLocalName().equals(conditionName)) {
					value = condition.getDisplayValue();
				}
			}
			org.generationcp.bms.domain.Variable factor = new org.generationcp.bms.domain.Variable(
					factorDetail.getStandardVariable());
			factor.setValue(value);
			factor.setLocalName(factorDetail.getLocalName());
			factor.setLocalDescription(factorDetail.getLocalDescription());
			studyDetails.addFactor(factor);
		}
		
        //Variates - What was measured?
        VariableTypeList variates = studyDataManager.getAllStudyVariates(Integer.valueOf(studyId));
        List<VariableType> variateDetails = variates.getVariableTypes(); 
        
        List<Trait> measuredTraits = new ArrayList<Trait>();
        for(VariableType variateDetail : variateDetails){
            String name = variateDetail.getLocalName();
            String description = variateDetail.getStandardVariable().getDescription();
            if(variateDetail.getLocalDescription() != null && variateDetail.getLocalDescription().length() != 0){
                description = variateDetail.getLocalDescription().trim();
            }
            String propertyName = variateDetail.getStandardVariable().getProperty().getName();
            String scaleName = variateDetail.getStandardVariable().getScale().getName();
            String methodName = variateDetail.getStandardVariable().getMethod().getName();
            String dataType = variateDetail.getStandardVariable().getDataType().getName();
            
            //studyDetails.addVariate(propertyName + " (" + name + ")", value + " (" + scaleName + ")");
            Trait trait = new Trait(variateDetail.getId());
            trait.setName(name);
            trait.setDescription(description);
            trait.setProperty(propertyName);
            trait.setMethod(methodName);
            trait.setScale(scaleName);
            trait.setType(dataType);
            measuredTraits.add(trait);           
        }
		studyDetails.addMeasuredTraits(measuredTraits);
		
		return studyDetails;
	}

	@RequestMapping(value = "/dataset/{dataSetId}", method = RequestMethod.GET)
	@ResponseBody
	public DatasetDetails getDatasetDetails(@PathVariable Integer dataSetId)
			throws MiddlewareQueryException {

		DataSet dataSet = studyDataManager.getDataSet(dataSetId);
		if (dataSet == null) {
			throw new NotFoundException();
		}
		DatasetDetails details = new DatasetDetails();
		details.setId(dataSet.getId());
		details.setName(dataSet.getName());
		details.setDescription(dataSet.getDescription());
		details.setStudyDetailsUrl(this.urlComposer.getStudyDetailsUrl(dataSet.getStudyId()));
		details.addMeasuredTraits(this.simpleDao.getMeasuredTraitsForDataset(dataSetId));
		details.setDatasetDetailUrl(this.urlComposer.getDataSetDetailsUrl(dataSet.getId()));
		return details;
	}

	@RequestMapping(value = "/{studyId}/observations", method = RequestMethod.GET)
	@ResponseBody
	public List<TraitObservation> getTraitObservationDetails(@PathVariable Integer studyId) throws MiddlewareQueryException {
		
		StudyType studyType = studyDataManager.getStudyType(studyId);
		Workbook workbook = null;
		if(studyType == StudyType.N) {
			workbook = fieldbookService.getNurseryDataSet(studyId);
		} else {
			workbook = fieldbookService.getTrialDataSet(studyId);
		}
		
		List<TraitObservation> observations = new ArrayList<TraitObservation>();
		List<MeasurementRow> measurementRows = workbook.getObservations();		

		for(MeasurementRow row : measurementRows) {
			TraitObservation obs = new TraitObservation();
			for(MeasurementData data : row.getDataList()) {
				obs.setTraitId(String.valueOf(data.getMeasurementVariable().getTermId()));
				if(data.getLabel().equals("ENTRY_NO")) {
					obs.setEntryNumber(data.getValue());
				}
				if(data.getLabel().equals("DESIGNATION")) {
					obs.setDesignation(data.getValue());
				}
				if(data.getLabel().equals("GID")) {
					obs.setGid(data.getValue());
				}
				if(data.getPhenotypeId() != null) {
					obs.setTraitName(data.getLabel());
					obs.setValue(data.getValue());
				}
			}
			observations.add(obs);
		}		
		return observations;		
	}
}
