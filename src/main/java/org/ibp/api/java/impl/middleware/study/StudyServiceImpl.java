package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.study.Trait;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyServiceImpl implements StudyService {

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Override
	public List<StudySummary> listAllStudies(final String programUniqueId) {
		List<StudySummary> studySummaries = new ArrayList<StudySummary>();
		try {
			List<org.generationcp.middleware.service.api.study.StudySummary> mwStudySummaries = middlewareStudyService.listAllStudies(programUniqueId);
			
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
			throw new ApiRuntimeException("Error! Caused by: " + e.getMessage(), e);
		}
		return studySummaries;
	}
	
	@Override
	public List<Observation> getObservations(Integer studyId) {
		List<org.generationcp.middleware.service.api.study.Measurement> studyMeasurements = middlewareStudyService.getMeasurements(studyId);
		List<Observation> observations = new ArrayList<Observation>();
		for (org.generationcp.middleware.service.api.study.Measurement measurement: studyMeasurements) {
			
			Observation observation = new Observation();
			observation.setUniqueIdentifier(measurement.getMeasurementId().toString());
			observation.setEnrtyNumber(measurement.getEntryNo());
			observation.setEntryType(measurement.getEntryType());
			observation.setEnvironmentNumber(measurement.getTrialInstance());
			observation.setGermplasmDesignation(measurement.getDesignation());
			observation.setGermplasmId(measurement.getGid());
			observation.setPlotNumber(measurement.getPlotNumber());
			observation.setReplicationNumber(measurement.getRepitionNumber());
			
			final List<Trait> traits = measurement.getTraits();
			final List<Measurement> measurements = new ArrayList<Measurement>();
			for (final Trait trait : traits) {
				measurements.add(new Measurement(trait.getTraitId(), trait.getTraitName(), trait.getTriatValue()));
			}
			
			observation.setMeasurements(measurements);
			observations.add(observation);
		}
		return observations;
	}



}
