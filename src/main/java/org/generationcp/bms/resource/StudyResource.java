package org.generationcp.bms.resource;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.DatasetDetails;
import org.generationcp.bms.domain.DatasetSummary;
import org.generationcp.bms.domain.StudyDetails;
import org.generationcp.bms.domain.StudySummary;
import org.generationcp.bms.domain.Trait;
import org.generationcp.bms.domain.TraitObservation;
import org.generationcp.bms.domain.TraitObservationDetails;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.bms.web.UrlComposer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Controller
@RequestMapping("/study")
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

	@RequestMapping(value = "/", method = RequestMethod.PUT)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created")})
	public ResponseEntity<StudySummary> createStudy(@RequestBody StudySummary studySummary) throws MiddlewareQueryException {
		LOGGER.info(studySummary.toString());
		
		Workbook workbook = new Workbook();
    	// Basic Details
    	org.generationcp.middleware.domain.etl.StudyDetails studyDetails = new org.generationcp.middleware.domain.etl.StudyDetails();
    	studyDetails.setStudyType(StudyType.N);
    	studyDetails.setStudyName(studySummary.getName());
    	studyDetails.setObjective(studySummary.getObjective());
    	studyDetails.setTitle(studySummary.getTitle());
    	studyDetails.setStartDate(studySummary.getStartDate());
    	studyDetails.setEndDate(studySummary.getEndDate());
    	studyDetails.setParentFolderId(1);    	
    	workbook.setStudyDetails(studyDetails);
    	
    	int studyId = dataImportService.saveDataset(workbook);    	
    	studySummary.setId(studyId);    	
		return new ResponseEntity<StudySummary>(studySummary, HttpStatus.CREATED);
	}

	private void populateStudySummary(StudySummary studySummary, Study study) throws MiddlewareQueryException {
		
		studySummary.setName(study.getName());
		studySummary.setTitle(study.getTitle());
		studySummary.setObjective(study.getObjective());
		studySummary.setType(study.getType());
		studySummary.setStartDate(String.valueOf(study.getStartDate()));
		studySummary.setEndDate(String.valueOf(study.getEndDate()));
		studySummary.setStudyDetailsUrl(this.urlComposer.getStudyDetailsUrl(study.getId()));	
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
		studyDetails.addMeasuredTraits(simpleDao.getMeasuredTraitsForStudy(studyId));
		setTraitObservationDetailsUrl(studyDetails.getId(), studyDetails.getMeasuredTraits());
		return studyDetails;
	}

	private void setTraitObservationDetailsUrl(Integer studyId, List<Trait> traits) {
		for (Trait trait : traits) {
			trait.setObservationDetailsUrl(this.urlComposer.getObservationDetailsUrl(studyId, trait.getId()));
		}
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
		setTraitObservationDetailsUrl(dataSet.getStudyId(), details.getMeasuredTraits());
		return details;
	}

	@RequestMapping(value = "/{studyId}/trait/{traitId}", method = RequestMethod.GET)
	@ResponseBody
	public TraitObservationDetails getTraitObservationDetails(@PathVariable Integer studyId,
			@PathVariable Integer traitId) {

		List<TraitObservation> traitObservations = simpleDao.getTraitObservations(studyId, traitId);
		if (!traitObservations.isEmpty()) {
			TraitObservationDetails details = new TraitObservationDetails(traitId, studyId);
			details.addObservations(traitObservations);
			return details;
		}
		throw new NotFoundException();
	}
}
