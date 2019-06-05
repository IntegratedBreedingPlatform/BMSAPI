
package org.ibp.api.java.impl.middleware.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
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
import org.ibp.api.domain.common.Command;
import org.ibp.api.domain.common.ValidationUtil;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.study.DatasetSummary;
import org.ibp.api.domain.study.Environment;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyAttribute;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyFolder;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Collections;
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
	private FieldbookService fieldbookService;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private ConversionService conversionService;

	@Autowired
	private DataImportService dataImportService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ObservationValidator observationValidator;

	@Autowired
	private ValidationUtil validationUtil;

	@Autowired
	private FieldMapService fieldMapService;

	@Override
	public List<StudySummary> search(final String programUniqueId, final String cropname, final String principalInvestigator, final String location, final String season) {
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
	public int countTotalObservationUnits(final int studyIdentifier, final int instanceId) {
		return this.middlewareStudyService.countTotalObservationUnits(studyIdentifier, instanceId);
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
				final String []errorMessage = {"measurement.already.inserted"};
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
		final String []errorKey = {"no.observation.found"};
		final Object []erroyKeyArguments = {studyIdentifier, observation.getUniqueIdentifier()};
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
	public StudyDetails getStudyDetails(final String studyId) {
		try {
			final Integer studyIdentifier = Integer.valueOf(studyId);
			final Study study = this.studyDataManager.getStudy(studyIdentifier);
			if (study == null) {
				throw new ApiRuntimeException("No study identified by the supplied studyId [" + studyId + "] was found.");
			}

			// Basic Info.
			final StudyDetails studyDetails = new StudyDetails();
			studyDetails.setId(String.valueOf(study.getId()));
			studyDetails.setName(study.getName());
			studyDetails.setTitle(study.getDescription());
			studyDetails.setObjective(study.getObjective());
			studyDetails.setType(study.getType()
					.getName());
			studyDetails.setStartDate(String.valueOf(study.getStartDate()));
			studyDetails.setEndDate(String.valueOf(study.getEndDate()));

			// Factors, Settings tab.
			final List<Variable> conditions = study.getConditions()
					.getVariables();
			final VariableTypeList factors = this.studyDataManager.getAllStudyFactors(studyIdentifier);
			final List<DMSVariableType> factorDetails = factors.getVariableTypes();
			for (final DMSVariableType factorDetail : factorDetails) {
				String value = null;
				for (final Variable condition : conditions) {
					final String conditionName = condition.getVariableType()
							.getLocalName();
					if (factorDetail.getLocalName()
							.equals(conditionName)) {
						value = condition.getDisplayValue();
					}
				}

				// Only add the attribute if there is a value associated.
				if (value != null) {
					final StudyAttribute attr = new StudyAttribute();
					attr.setId(String.valueOf(factorDetail.getId()));
					attr.setName(factorDetail.getLocalName());
					attr.setDescription(factorDetail.getLocalDescription());
					attr.setValue(value);
					studyDetails.addGeneralInfo(attr);
				}
			}

			// Variates - Measurements tab.
			final VariableTypeList variates = this.studyDataManager.getAllStudyVariates(studyIdentifier);
			final List<DMSVariableType> variateDetails = variates.getVariableTypes();
			for (final DMSVariableType variateDetail : variateDetails) {
				final TermSummary trait = new TermSummary();
				trait.setId(String.valueOf(variateDetail.getId()));
				trait.setName(variateDetail.getStandardVariable()
						.getName());
				trait.setDescription(variateDetail.getStandardVariable()
						.getDescription());
				studyDetails.addTrait(trait);
			}

			// Datasets
			final DataSet trialDataset =
				this.studyDataManager.findOneDataSetByType(studyIdentifier, DatasetTypeEnum.SUMMARY_DATA.getId());
			final List<DatasetReference> datasetReferences = this.studyDataManager.getDatasetReferences(studyIdentifier);
			if (datasetReferences != null && !datasetReferences.isEmpty()) {
				for (final DatasetReference dsRef : datasetReferences) {
					final DatasetSummary dsSummary = new DatasetSummary();
					dsSummary.setId(dsRef.getId()
							.toString());
					dsSummary.setName(dsRef.getName());
					dsSummary.setDescription(dsRef.getDescription());
					studyDetails.addDataSet(dsSummary);

					if (dsRef.getId().equals(trialDataset.getId())) {
						// Logic derived from by RepresentationDataSetQuery.loadItems(int, int) method of the GermplasmStudyBrowser,
						// which is used to show dataset tables in the study browser UI.
						final List<Experiment> experiments = this.studyDataManager.getExperiments(dsRef.getId(), 0, Integer.MAX_VALUE);
						for (final Experiment experiment : experiments) {
							final List<Variable> variables = new ArrayList<>();
							final VariableList fac = experiment.getFactors();
							if (fac != null) {
								variables.addAll(fac.getVariables());
							}
							final VariableList var = experiment.getVariates();
							if (var != null) {
								variables.addAll(var.getVariables());
							}

							final Environment env = new Environment();
							for (final Variable variable : variables) {
								final StudyAttribute attr = new StudyAttribute();
								attr.setId(String.valueOf(variable.getVariableType()
										.getId()));
								attr.setName(variable.getVariableType()
										.getLocalName());
								attr.setDescription(variable.getVariableType()
										.getLocalDescription());
								attr.setValue(variable.getDisplayValue());
								env.addEnvironmentDetail(attr);
							}
							studyDetails.addEnvironment(env);
						}
					}
				}
			}

			// Germplasm
			studyDetails.getGermplasm()
					.addAll(this.getStudyGermplasmList(studyIdentifier));
			return studyDetails;
		} catch (final NumberFormatException nfe) {
			throw new ApiRuntimeException("Supplied study identifier [" + studyId + "] is not valid, it must be a numeric value.");
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("Error! Caused by: " + e.getMessage(), e);
		}
	}

	@Override
	public Map<Integer, FieldMap> getFieldMap(final String studyId) {
		return fieldMapService.getFieldMap(studyId);
	}


	private final <T, S> List<T> convert(final List<S> beanList, final Class<T> clazz) {
		if (null == beanList) {
			return Collections.emptyList();
		}

		final List<T> convertedList = new ArrayList<>();
		for (final S s : beanList) {
			convertedList.add(this.conversionService.convert(s, clazz));
		}
		return convertedList;
	}

	void setMiddlewareStudyService(final org.generationcp.middleware.service.api.study.StudyService middlewareStudyService) {
		this.middlewareStudyService = middlewareStudyService;
	}

	void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	void setFieldbookService(final FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	void setDataImportService(final DataImportService dataImportService) {
		this.dataImportService = dataImportService;
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


	@Override
	public List<StudyFolder> getAllStudyFolders() {
		final List<StudyFolder> studyFolders = new ArrayList<>();
		final List<FolderReference> middlewareFolders = this.studyDataManager.getAllFolders();

		for (final FolderReference folderRef : middlewareFolders) {
			studyFolders.add(new StudyFolder(folderRef.getId(), folderRef.getName(), folderRef.getDescription(), folderRef
					.getParentFolderId()));
		}

		return studyFolders;
	}

	@Override
	public String getProgramUUID(final Integer studyIdentifier) {
		return this.middlewareStudyService.getProgramUUID(studyIdentifier);
	}

	@Override
	public List<StudyInstance> getStudyInstances(final int studyId) {
		final List<org.generationcp.middleware.service.impl.study.StudyInstance> studyInstancesMW =
				this.middlewareStudyService.getStudyInstances(studyId);

		final Function<org.generationcp.middleware.service.impl.study.StudyInstance, StudyInstance> transformer =
				new Function<org.generationcp.middleware.service.impl.study.StudyInstance, StudyInstance>() {

			@Override
			public StudyInstance apply(final org.generationcp.middleware.service.impl.study.StudyInstance input) {
				return new StudyInstance(input.getInstanceDbId(), input.getLocationName(), input.getLocationAbbreviation(),
						input.getInstanceNumber(),input.getCustomLocationAbbreviation(), input.isHasFieldmap());
			}
		};
		return Lists.transform(studyInstancesMW, transformer);
	}

	public TrialObservationTable getTrialObservationTable(final int studyIdentifier) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier);
	}

	@Override
	public TrialObservationTable getTrialObservationTable(final int studyIdentifier, final Integer studyDbId) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier, studyDbId);
	}

	@Override
	public StudyDetailsDto getStudyDetailsDto(final Integer studyId) {
		return this.middlewareStudyService.getStudyDetails(studyId);
	}

	@Override
	public Long countStudies(final Map<StudyFilters, String> filters) {
		return this.studyDataManager.countAllStudies(filters);
	}

	@Override
	public List<PhenotypeSearchDTO> searchPhenotypes(final Integer pageSize, final Integer pageNumber, final PhenotypeSearchRequestDTO requestDTO) {
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
			return this.sampleService.studyHasSamples(studyId);
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public List<StudyTypeDto> getStudyTypes(){
		try{
			return this.studyDataManager.getAllVisibleStudyTypes();
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public StudyReference getStudyReference(Integer studyId) {
		return this.studyDataManager.getStudyReference(studyId);
	}

	@Override
	public void updateStudy(final Study study) {
		this.studyDataManager.updateStudyLockedStatus(study.getId(), study.isLocked());
	}
}
