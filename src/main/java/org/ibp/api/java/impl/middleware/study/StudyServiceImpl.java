package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

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
	
	@Override
	public List<Observation> getObservations(Integer studyId) {
		Observation obs1 = createMockMeasurements(800000110, "ICARDA_GCP H2", "11", "12");
		Observation obs2 = createMockMeasurements(3829816, "WAXWING*2/CIRCUS", "21", "22");
		return Lists.newArrayList(obs1, obs2);
	}

	private Observation createMockMeasurements(Integer gid, String germplasm, String value1, String value2) {
		Observation obs = new Observation();
		obs.setGermplasmId(gid);
		obs.setGermplasmDesignation(germplasm);
		obs.setEnrtyNumber(95);
		obs.setEntryType("Test Entry");
		obs.setReplicationNumber(1);
		obs.setPlotNumber(1);
		obs.setEnvironmentNumber(1);
		obs.setParentage("ICARDA_GCP H0/ICARDA_GCP H1");
		
		Measurement measurement1 = new Measurement(1, "AGRSCR_0_9", value1);
		Measurement measurement2 = new Measurement(2, "Plant_height", value2);
		obs.setMeasurements(Lists.newArrayList(measurement1, measurement2));
		return obs;
	}

}
