package org.generationcp.bms.resource;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.StudyDetails;
import org.generationcp.bms.domain.StudySummary;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/study")
public class StudyResource {

	private final StudyDataManager studyDataManager;
	
	private final SimpleDao simpleDao;
	
	@Autowired
	public StudyResource(StudyDataManager studyDataManager, SimpleDao simpleDao) {
		if(studyDataManager == null) {
			throw new IllegalArgumentException(StudyDataManager.class.getSimpleName() + " is required to instantiate " + StudyResource.class.getSimpleName());
		}
		if(simpleDao == null) {
			throw new IllegalArgumentException(SimpleDao.class.getSimpleName() + " is required to instantiate " + StudyResource.class.getSimpleName());
		}
		this.studyDataManager = studyDataManager;
		this.simpleDao = simpleDao;
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(HttpServletRequest request) throws MiddlewareQueryException {
		return "Please provide the id of the study you want to GET to. e.g. http://host:port/study/10010 where 10010 is the study id.";
	}

	@RequestMapping(value="/{id}/summary", method = RequestMethod.GET)
	public StudySummary getStudySummary(@PathVariable Integer id)
			throws MiddlewareQueryException {

		Study study = studyDataManager.getStudy(id);

		if (study == null) {
			throw new NotFoundException();
		}
	
		StudySummary studySummary = new StudySummary(study.getId());
		populateSummary(studySummary, study);
		return studySummary;

	}
	
	private void populateSummary(StudySummary studySummary, Study study) {
		studySummary.setName(study.getName());
		studySummary.setTitle(study.getTitle());
		studySummary.setObjective(study.getObjective());
		studySummary.setType(study.getType());
		studySummary.setStartDate(String.valueOf(study.getStartDate()));
		studySummary.setEndDate(String.valueOf(study.getEndDate()));
	}
	
	@RequestMapping(value="/{id}", method = RequestMethod.GET)
	public StudyDetails getStudyDetails(@PathVariable Integer id) throws MiddlewareQueryException {
		
        Study study = studyDataManager.getStudy(id);
        if (study == null) {
			throw new NotFoundException();
		}
        
        StudyDetails studyDetails = new StudyDetails(study.getId());
        populateSummary(studyDetails, study);
        
        //factors/metadaa/properties/information/conditions of the study        
        
        List<Variable> conditions = study.getConditions().getVariables();
        VariableTypeList factors = studyDataManager.getAllStudyFactors(Integer.valueOf(id));
        List<VariableType> factorDetails = factors.getVariableTypes();
        for(VariableType factorDetail : factorDetails){
            String value = null;           
            for(Variable condition : conditions){
                String conditionName = condition.getVariableType().getLocalName();
                if(factorDetail.getLocalName().equals(conditionName)){
                    value = condition.getDisplayValue();
                }
            }
            org.generationcp.bms.domain.Variable factor = new org.generationcp.bms.domain.Variable(factorDetail.getStandardVariable());
            factor.setValue(value);
            factor.setLocalName(factorDetail.getLocalName());
            factor.setLocalDescription(factorDetail.getLocalDescription());
            studyDetails.addFactor(factor);      
        }
        
        studyDetails.addMeasuredTraits(simpleDao.getMeasuredTraits(id));
        
        return studyDetails;    
	}
}
