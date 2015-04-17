package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareException;
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
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Override
	public List<StudySummary> listAllStudies() {
		List<StudySummary> studySummaries = new ArrayList<StudySummary>();
		try {
			List<org.generationcp.middleware.service.api.study.StudySummary> mwStudySummaries = middlewareStudyService.listAllStudies();
			
			for (org.generationcp.middleware.service.api.study.StudySummary mwStudySummary : mwStudySummaries) {
				StudySummary summary = new StudySummary(mwStudySummary.getId());
				summary.setName(mwStudySummary.getName());
				summary.setTitle(mwStudySummary.getTitle());
				summary.setObjective(mwStudySummary.getObjective());
				summary.setStartDate(mwStudySummary.getStartDate());
				summary.setEndDate(mwStudySummary.getEndDate());
				summary.setType(mwStudySummary.getType().getName());
				studySummaries.add(summary);
			}
		} catch (MiddlewareException e) {
			// can I do much about this? what can I do?
		}
		return studySummaries;
	}
	
	@Override
	public List<Observation> getObservations(Integer studyId) {
		//Environment 1, Replicate 1
		Observation obs111 = createMockMeasurements(800000006, "Zhoumai18",       1, "Test Entry", 1, "Unknown", 1, 1, "100GW_g", "101", "GRNCOL", "Light Brown 1");
		Observation obs112 = createMockMeasurements(800000008, "Chang4738",       2, "Test Entry", 2, "Unknown", 1, 1, "100GW_g", "102", "GRNCOL", "Light Brown 2");
		Observation obs113 = createMockMeasurements(800000010, "Sokoll (CIMMYT)", 3, "Test Entry", 3, "Unknown", 1, 1, "100GW_g", "103", "GRNCOL", "Light Brown 3");
		
		//Environment 1, Replicate 2
		Observation obs121 = createMockMeasurements(800000006, "Zhoumai18",       1, "Test Entry", 1, "Unknown", 2, 1, "100GW_g", "104", "GRNCOL", "Light Brown 4");
		Observation obs122 = createMockMeasurements(800000008, "Chang4738",       2, "Test Entry", 2, "Unknown", 2, 1, "100GW_g", "105", "GRNCOL", "Light Brown 5");
		Observation obs123 = createMockMeasurements(800000010, "Sokoll (CIMMYT)", 3, "Test Entry", 3, "Unknown", 2, 1, "100GW_g", "106", "GRNCOL", "Light Brown 6");
	
		//Environment 2, Replicate 1
		Observation obs211 = createMockMeasurements(800000006, "Zhoumai18",       1, "Test Entry", 1, "Unknown", 1, 2, "100GW_g", "201", "GRNCOL", "Dark Brown 1");
		Observation obs212 = createMockMeasurements(800000008, "Chang4738",       2, "Test Entry", 2, "Unknown", 1, 2, "100GW_g", "202", "GRNCOL", "Dark Brown 2");
		Observation obs213 = createMockMeasurements(800000010, "Sokoll (CIMMYT)", 3, "Test Entry", 3, "Unknown", 1, 2, "100GW_g", "203", "GRNCOL", "Dark Brown 3");
		
		//Environment 2, Replicate 2
		Observation obs221 = createMockMeasurements(800000006, "Zhoumai18",       1, "Test Entry", 1, "Unknown", 2, 2, "100GW_g", "204", "GRNCOL", "Dark Brown 4");
		Observation obs222 = createMockMeasurements(800000008, "Chang4738",       2, "Test Entry", 2, "Unknown", 2, 2, "100GW_g", "205", "GRNCOL", "Dark Brown 5");
		Observation obs223 = createMockMeasurements(800000010, "Sokoll (CIMMYT)", 3, "Test Entry", 3, "Unknown", 2, 2, "100GW_g", "206", "GRNCOL", "Dark Brown 6");

		return Lists.newArrayList(obs111, obs112, obs113, obs121, obs122, obs123, obs211, obs212, obs213, obs221, obs222, obs223);
	}

	private Observation createMockMeasurements(Integer germplasmId, String germplasmDesignation, Integer enrtyNumber, String entryType, Integer plotNumber, String parentage,
			Integer replicationNumber, Integer environmentNumber, String trait1Name, String trait1Value, String trait2Name, String trait2Value) {
		
		Observation obs = new Observation();
		obs.setGermplasmId(germplasmId);
		obs.setGermplasmDesignation(germplasmDesignation);
		obs.setEnrtyNumber(enrtyNumber);
		obs.setEntryType(entryType);
		obs.setReplicationNumber(replicationNumber);
		obs.setPlotNumber(plotNumber);
		obs.setEnvironmentNumber(environmentNumber);
		obs.setParentage(parentage);

		Measurement measurement1 = new Measurement(1, trait1Name, trait1Value);
		Measurement measurement2 = new Measurement(2, trait2Name, trait2Value);
		obs.setMeasurements(Lists.newArrayList(measurement1, measurement2));
		return obs;
	}

}
