
package org.ibp.api.java.impl.middleware.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyFilters;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.domain.common.Command;
import org.ibp.api.domain.common.ValidationUtil;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Autowired
	org.generationcp.middleware.service.api.SampleService sampleService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private ObservationValidator observationValidator;

	@Autowired
	private ValidationUtil validationUtil;

	@Autowired
	private FieldMapService fieldMapService;

	@Override
	public List<StudySummary> search(final String programUniqueId, final String cropname, final String principalInvestigator,
		final String location, final String season) {
		final List<StudySummary> studySummaries = new ArrayList<>();
		try {
			final StudySearchParameters searchParameters = new StudySearchParameters();
			searchParameters.setProgramUniqueId(programUniqueId);
			searchParameters.setPrincipalInvestigator(principalInvestigator);
			searchParameters.setLocation(location);
			searchParameters.setSeason(season);
			final List<org.generationcp.middleware.service.api.study.StudySummary> mwStudySummaries =
				this.middlewareStudyService.search(searchParameters);

			for (final org.generationcp.middleware.service.api.study.StudySummary mwStudySummary : mwStudySummaries) {
				if (!this.securityService.isAccessible(mwStudySummary, cropname)) {
					continue;
				}

				final StudySummary summary = new StudySummary(String.valueOf(mwStudySummary.getId()));
				summary.setName(mwStudySummary.getName());
				summary.setTitle(mwStudySummary.getTitle());
				summary.setObjective(mwStudySummary.getObjective());
				summary.setStartDate(mwStudySummary.getStartDate());
				summary.setEndDate(mwStudySummary.getEndDate());
				summary.setType(mwStudySummary.getType()
					.getName());
				summary.setPrincipalInvestigator(mwStudySummary.getPrincipalInvestigator());
				summary.setLocation(mwStudySummary.getLocation());
				summary.setSeason(mwStudySummary.getSeason());
				studySummaries.add(summary);
			}
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("Error! Caused by: " + e.getMessage(), e);
		}
		return studySummaries;
	}

	@Override
	public List<Observation> getObservations(final Integer studyId, final int instanceId, final int pageNumber, final int pageSize,
		final String sortBy, final String sortOrder) {
		final List<ObservationDto> studyMeasurements =
			this.middlewareStudyService.getObservations(studyId, instanceId, pageNumber, pageSize, sortBy, sortOrder);
		final List<Observation> observations = new ArrayList<>();
		for (final ObservationDto measurement : studyMeasurements) {
			observations.add(this.mapObservationDtoToObservation(measurement));
		}
		return observations;
	}

	@Override
	public Observation getSingleObservation(final Integer studyId, final Integer obeservationId) {
		final List<ObservationDto> singleObservation = this.middlewareStudyService.getSingleObservation(studyId, obeservationId);
		if (!singleObservation.isEmpty()) {
			return this.mapObservationDtoToObservation(singleObservation.get(0));
		}
		return new Observation();
	}

	private Observation mapObservationDtoToObservation(final ObservationDto measurement) {
		return StudyMapper.getInstance()
			.map(measurement, Observation.class);
	}

	@Override
	public Observation updateObservation(final Integer studyIdentifier, final Observation observation) {
		this.validationUtil.invokeValidation("StudyServiceImpl", new Command() {

			@Override
			public void execute(final Errors errors) {
				StudyServiceImpl.this.observationValidator.validate(observation, errors);
			}
		});
		return this.mapAndUpdateObservation(studyIdentifier, observation);
	}

	/**
	 * Translates to the middleware pojo. Updates the database and then translates back the results.
	 *
	 * @param studyIdentifier
	 * @param observation
	 * @return
	 */
	private Observation mapAndUpdateObservation(final Integer studyIdentifier, final Observation observation) {
		this.validateMeasurementSubmitted(studyIdentifier, observation);

		final List<Measurement> measurements = observation.getMeasurements();

		final List<MeasurementDto> traits = new ArrayList<>();
		for (final Measurement measurement : measurements) {
			traits.add(new MeasurementDto(new MeasurementVariableDto(measurement.getMeasurementIdentifier()
				.getTrait()
				.getTraitId(), measurement
				.getMeasurementIdentifier()
				.getTrait()
				.getTraitName()), measurement.getMeasurementIdentifier()
				.getMeasurementId(),
				measurement.getMeasurementValue(), measurement.getValueStatus()));
		}
		final ObservationDto middlewareMeasurement =
			new ObservationDto(observation.getUniqueIdentifier(), observation.getEnvironmentNumber(), observation.getEntryType(),
				observation.getGermplasmId(), observation.getGermplasmDesignation(), observation.getEntryNumber(),
				observation.getEntryCode(), observation.getReplicationNumber(), observation.getPlotNumber(),
				observation.getBlockNumber(), traits);

		return this.mapObservationDtoToObservation(this.middlewareStudyService.updataObservation(studyIdentifier, middlewareMeasurement));
	}

	@Override
	public List<Observation> updateObservations(final Integer studyIdentifier, final List<Observation> observations) {
		final List<Observation> returnList = new ArrayList<>();

		this.validationUtil.invokeValidation("StudyServiceImpl", new Command() {

			@Override
			public void execute(final Errors errors) {
				int counter = 0;
				for (final Observation observation : observations) {
					errors.pushNestedPath("Observation[" + counter++ + "]");
					StudyServiceImpl.this.observationValidator.validate(observation, errors);
					returnList.add(StudyServiceImpl.this.mapAndUpdateObservation(studyIdentifier, observation));
					errors.popNestedPath();
				}
			}
		});
		return returnList;
	}

	/**
	 * Essentially makes sure that the underlying observation has not changed
	 *
	 * @param studyIdentifier the study in which the observation is being updated
	 * @param observation     the actual observation update.
	 */
	private void validateMeasurementSubmitted(final Integer studyIdentifier, final Observation observation) {
		// If null do something
		final Observation existingObservation = this.getSingleObservation(studyIdentifier, observation.getUniqueIdentifier());
		final List<ObjectError> errors = new ArrayList<>();
		if (existingObservation == null || existingObservation.getUniqueIdentifier() == null) {
			this.validateExistingObservation(studyIdentifier, observation, errors);
		} else {
			this.validateMeasurementHasNotBeenCreated(observation, existingObservation, errors);
		}
		if (!errors.isEmpty()) {
			throw new ApiRequestValidationException(errors);
		}

	}

	private void validateMeasurementHasNotBeenCreated(final Observation observation, final Observation existingObservation,
		final List<ObjectError> errors) {
		final List<Measurement> measurements = observation.getMeasurements();
		int counter = 0;
		for (final Measurement measurement : measurements) {
			// Relies on the hash coded generated in the MeasurementIdentifier object
			final Measurement existingMeasurement = existingObservation.getMeasurement(measurement.getMeasurementIdentifier());
			if (existingMeasurement == null) {
				final String[] errorMessage = {"measurement.already.inserted"};
				final List<String> object = new ArrayList<>();
				final ObjectMapper objectMapper = new ObjectMapper();
				try {
					object.add(objectMapper.writeValueAsString(measurement));
				} catch (final JsonProcessingException e) {
					throw new ApiRuntimeException("Error mapping measurement to JSON", e);
				}
				final FieldError objectError =
					new FieldError("Observation", "Measurements [" + counter + "]", null, false, errorMessage, object.toArray(),
						"Error processing measurement");
				errors.add(objectError);
				counter++;
			}
		}
	}

	private void validateExistingObservation(final Integer studyIdentifier, final Observation observation, final List<ObjectError> errors) {
		final String[] errorKey = {"no.observation.found"};
		final Object[] erroyKeyArguments = {studyIdentifier, observation.getUniqueIdentifier()};
		final FieldError observationIdentifierError =
			new FieldError("Observation", "uniqueIdentifier", null, false, errorKey, erroyKeyArguments,
				"Error retrieving observation");
		errors.add(observationIdentifierError);
	}

	@Override
	public List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer) {
		final ModelMapper modelMapper = StudyMapper.getInstance();
		final List<StudyGermplasm> destination = new ArrayList<>();
		final List<StudyGermplasmDto> studyGermplasmList = this.middlewareStudyService.getStudyGermplasmList(studyIdentifer);
		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmList) {
			final StudyGermplasm mappedValue = modelMapper.map(studyGermplasmDto, StudyGermplasm.class);
			destination.add(mappedValue);
		}
		return destination;
	}

	@Override
	public Map<Integer, FieldMap> getFieldMap(final String studyId) {
		return this.fieldMapService.getFieldMap(studyId);
	}

	void setMiddlewareStudyService(final org.generationcp.middleware.service.api.study.StudyService middlewareStudyService) {
		this.middlewareStudyService = middlewareStudyService;
	}

	void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	void setSecurityService(final SecurityService securityService) {
		this.securityService = securityService;
	}

	void setValidationUtil(final ValidationUtil validationUtil) {
		this.validationUtil = validationUtil;
	}

	void setObservationValidator(final ObservationValidator observationValidator) {
		this.observationValidator = observationValidator;
	}

	void setStudyValidator(final StudyValidator studyValidator) {
		this.studyValidator = studyValidator;
	}

	@Override
	public String getProgramUUID(final Integer studyIdentifier) {
		return this.middlewareStudyService.getProgramUUID(studyIdentifier);
	}

	public TrialObservationTable getTrialObservationTable(final int studyIdentifier) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier);
	}

	@Override
	public TrialObservationTable getTrialObservationTable(final int studyIdentifier, final Integer studyDbId) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier, studyDbId);
	}

	@Override
	public StudyDetailsDto getStudyDetailsForGeolocation(final Integer geolocationId) {
		return this.middlewareStudyService.getStudyDetailsForGeolocation(geolocationId);
	}

	@Override
	public Long countStudies(final Map<StudyFilters, String> filters) {
		return this.studyDataManager.countAllStudies(filters);
	}

	@Override
	public List<PhenotypeSearchDTO> searchPhenotypes(final Integer pageSize, final Integer pageNumber,
		final PhenotypeSearchRequestDTO requestDTO) {
		return this.middlewareStudyService.searchPhenotypes(pageSize, pageNumber, requestDTO);
	}

	@Override
	public long countPhenotypes(final PhenotypeSearchRequestDTO requestDTO) {
		return this.middlewareStudyService.countPhenotypes(requestDTO);
	}

	@Override
	public List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(final Map<StudyFilters, String> filters,
		final Integer pageSize, final Integer pageNumber) {
		final List<org.generationcp.middleware.domain.dms.StudySummary> studySummaryList =
			this.studyDataManager.findPagedProjects(filters, pageSize, pageNumber);

		for (final org.generationcp.middleware.domain.dms.StudySummary studySummary : studySummaryList) {
			final Project project = this.workbenchDataManager.getProjectByUuid(studySummary.getProgramDbId());
			if (project != null) {
				studySummary.setProgramName(project.getProjectName());
			}
		}

		return studySummaryList;
	}

	@Override
	public Boolean isSampled(final Integer studyId) {
		try {
			this.studyValidator.validate(studyId, false);
			return this.sampleService.studyHasSamples(studyId);
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public List<StudyTypeDto> getStudyTypes() {
		try {
			return this.studyDataManager.getAllVisibleStudyTypes();
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public StudyReference getStudyReference(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.studyDataManager.getStudyReference(studyId);
	}

	@Override
	public void updateStudy(final Study study) {
		final int studyId = study.getId();
		this.studyValidator.validate(studyId, false);
		this.studyDataManager.updateStudyLockedStatus(studyId, study.isLocked());
	}

	@Override
	public long countVariablesByDatasetId(final int studyDbId, final List<Integer> variableTypes) {
		return this.middlewareStudyService.countVariablesByDatasetId(studyDbId, variableTypes);
	}

	@Override
	public List<VariableDTO> getVariablesByDatasetId(final int pageSize, final int pageNumber, final int studyDbId,
		final String cropname, final List<Integer> variableTypes) {
		return this.middlewareStudyService.getVariablesByDatasetId(pageSize, pageNumber, studyDbId, variableTypes, cropname);
	}

	@Override
	public long countAllVariables(final List<Integer> variableTypes) {
		return this.middlewareStudyService.countAllVariables(variableTypes);
	}

	@Override
	public List<VariableDTO> getAllVariables(final int pageSize, final int pageNumber,
		final String cropname, final List<Integer> variableTypes) {
		return this.middlewareStudyService.getAllVariables(pageSize, pageNumber, variableTypes, cropname);
	}
}
