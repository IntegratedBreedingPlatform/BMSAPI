package org.generationcp.bms.resource;

import org.generationcp.bms.domain.StudySummary;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.dms.Study;
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

	@Autowired
	private StudyDataManager studyDataManager;

	@RequestMapping(value="/summary/{id}", method = RequestMethod.GET)
	public StudySummary getStudySummary(@PathVariable Integer id)
			throws MiddlewareQueryException {

		Study study = studyDataManager.getStudy(id);

		if (study == null) {
			throw new NotFoundException();
		}
	
		StudySummary studySummary = new StudySummary(study.getId());

		studySummary.setName(study.getName());
		studySummary.setTitle(study.getTitle());
		studySummary.setObjective(study.getObjective());
		studySummary.setType(study.getType());
		studySummary.setStartDate(String.valueOf(study.getStartDate()));
		studySummary.setEndDate(String.valueOf(study.getEndDate()));

		return studySummary;

	}
}
