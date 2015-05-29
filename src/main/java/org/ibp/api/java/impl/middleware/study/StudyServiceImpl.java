
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ontology.OntologyMapper;
import org.ibp.api.java.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StudyServiceImpl implements StudyService {

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Override
	public List<StudySummary> listAllStudies(final String programUniqueId) {
		List<StudySummary> studySummaries = new ArrayList<StudySummary>();
		try {
			List<org.generationcp.middleware.service.api.study.StudySummary> mwStudySummaries =
					this.middlewareStudyService.listAllStudies(programUniqueId);

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
		final List<ObservationDto> studyMeasurements = this.middlewareStudyService.getObservations(studyId);
		final List<Observation> observations = new ArrayList<Observation>();
		for (ObservationDto measurement : studyMeasurements) {
			observations.add(this.mapObservationDtoToObservation(measurement));
		}
		return observations;
	}

	@Override
	public Observation getSingleObservation(Integer studyId, Integer obeservationId) {
		return this.mapObservationDtoToObservation(this.middlewareStudyService.getSingleObservation(studyId, obeservationId).get(0));
	}

	private Observation mapObservationDtoToObservation(ObservationDto measurement) {
		return StudyMapper.getInstance().map(measurement, Observation.class);
	}

	@Override
	public Observation updateObsevation(final Integer studyIdentifier, final Observation observation) {

		this.validateMeasurementSubmitted(studyIdentifier, observation);

		final List<Measurement> measurements = observation.getMeasurements();

		final List<MeasurementDto> traits = new ArrayList<MeasurementDto>();
		for (Measurement measurement : measurements) {
			traits.add(new MeasurementDto(new TraitDto(measurement.getMeasurementIdentifier().getTrait().getTraitId(), measurement
					.getMeasurementIdentifier().getTrait().getTraitName()), measurement.getMeasurementIdentifier().getMeasurementId(),
					measurement.getMeasurementValue()));
		}
		final ObservationDto middlewareMeasurement =
				new ObservationDto(observation.getUniqueIdentifier(), observation.getEnvironmentNumber(), observation.getEntryType(),
						observation.getGermplasmId(), observation.getGermplasmDesignation(), observation.getEnrtyNumber(),
						observation.getSeedSource(), observation.getReplicationNumber(), observation.getPlotNumber(), traits);

		return this.mapObservationDtoToObservation(this.middlewareStudyService.updataObservation(studyIdentifier, middlewareMeasurement));
	}

	private void validateMeasurementSubmitted(final Integer studyIdentifier, final Observation observation) {
		final Observation existingObservation = this.getSingleObservation(studyIdentifier, observation.getUniqueIdentifier());
		final List<Measurement> measurements = observation.getMeasurements();
		final List<ObjectError> errors = new ArrayList<ObjectError>();
		int counter = 0;
		for (final Measurement measurement : measurements) {
			final Measurement existingMeasurement = existingObservation.getMeasurement(measurement.getMeasurementIdentifier());
			if (existingMeasurement == null) {
				final String array[] = {"program.already.inserted"};
				final List<String> object = new ArrayList<String>();
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					object.add(objectMapper.writeValueAsString(measurement));
				} catch (JsonProcessingException e) {
					throw new ApiRuntimeException("Error mapping measurement to JSON", e);
				}
				FieldError objectError =
						new FieldError("Measurements [" + counter + "]", "Measurement", null, false, array, object.toArray(),
								"Error processing measurement");
				errors.add(objectError);
				counter++;
			}
		}
		if (!errors.isEmpty()) {
			throw new ApiRequestValidationException(errors);
		}
	}

	protected void setMiddlewareStudyService(org.generationcp.middleware.service.api.study.StudyService middlewareStudyService) {
		this.middlewareStudyService = middlewareStudyService;
	}

	@Override
	public List<StudyGermplasm> getStudyGermplasmList(Integer studyIdentifer) {
		final ModelMapper modelMapper = StudyMapper.getInstance();
		final List<StudyGermplasm> destination = new ArrayList<StudyGermplasm>();
		final List<StudyGermplasmDto> studyGermplasmList = this.middlewareStudyService.getStudyGermplasmList(studyIdentifer);
		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmList) {
			final StudyGermplasm mappedValue = modelMapper.map(studyGermplasmDto, StudyGermplasm.class);
			destination.add(mappedValue);
		}
		return destination;
	}
}
