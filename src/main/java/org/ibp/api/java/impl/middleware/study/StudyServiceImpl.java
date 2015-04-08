
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyServiceImpl implements StudyService {

	@Autowired
	private StudyDataManager studyDataManager;

	@Override
	public List<StudySummary> listAllStudies() {
		List<StudySummary> studySummaries = new ArrayList<StudySummary>();
		List<StudyDetails> allStudies = new ArrayList<StudyDetails>();
		try {
			for (StudyType studyType : StudyType.values()) {
				allStudies.addAll(this.studyDataManager.getAllStudyDetails(studyType, null));
			}
			for (org.generationcp.middleware.domain.etl.StudyDetails studyDetails : allStudies) {
				StudySummary summary = new StudySummary(studyDetails.getId());
				summary.setName(studyDetails.getStudyName());
				summary.setTitle(studyDetails.getTitle());
				summary.setObjective(studyDetails.getObjective());
				summary.setStartDate(studyDetails.getStartDate());
				summary.setEndDate(studyDetails.getEndDate());
				summary.setType(studyDetails.getStudyType().getName());
				studySummaries.add(summary);
			}
		} catch (MiddlewareQueryException e) {
			// can I do much about this? what can I do?
		}
		return studySummaries;
	}

}
