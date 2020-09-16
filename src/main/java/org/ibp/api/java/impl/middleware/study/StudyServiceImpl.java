
package org.ibp.api.java.impl.middleware.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyDto;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyFilters;
import org.generationcp.middleware.service.api.study.StudyGermplasmService;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.domain.common.Command;
import org.ibp.api.domain.common.ValidationUtil;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {

	@Autowired
	private DatasetService middlewareDatasetService;

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

	@Autowired
	private StudyGermplasmService studyGermplasmService;

	@Autowired
	private DatasetService datasetService;

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

		return this.mapObservationDtoToObservation(this.middlewareStudyService.updateObservation(studyIdentifier, middlewareMeasurement));
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
	public StudyDetailsDto getStudyDetailsByGeolocation(final Integer geolocationId) {
		return this.middlewareStudyService.getStudyDetailsByInstance(geolocationId);
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
	public long countStudies(final StudySearchFilter studySearchFilter) {
		return this.middlewareStudyService.countStudies(studySearchFilter);
	}

	@Override
	public List<StudyDto> getStudies(final StudySearchFilter studySearchFilter, final Pageable pageable) {
		return this.middlewareStudyService.getStudies(studySearchFilter, pageable);
	}

	@Override
	public List<TreeNode> getStudyTree(final String parentKey, final String programUUID) {
		List<TreeNode> nodes = new ArrayList<>();
		if (StringUtils.isBlank(parentKey)) {
			final TreeNode rootNode = new TreeNode(AppConstants.STUDIES.name(), AppConstants.STUDIES.getString(), true, null);
			nodes.add(rootNode);
		} else if (parentKey.equals(AppConstants.STUDIES.name())) {
			final List<Reference> children = this.studyDataManager.getRootFolders(programUUID);
			nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(children, true);
		} else if (NumberUtils.isNumber(parentKey)) {
			final List<Reference> folders = this.studyDataManager.getChildrenOfFolder(Integer.valueOf(parentKey), programUUID);
			nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true);
		}
		return nodes;
	}

	@Override
	public boolean studyHasGivenDatasetType(final Integer studyId, final Integer datasetTypeId) {
		return this.middlewareStudyService.studyHasGivenDatasetType(studyId, datasetTypeId);
	}

	@Override
	public boolean hasCrossesOrSelections(final int studyId) {
		return this.middlewareStudyService.hasCrossesOrSelections(studyId);
	}

	@Override
	public Integer getEnvironmentDatasetId(final Integer studyId) {
		final List<DatasetDTO> datasets =
			this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!CollectionUtils.isEmpty(datasets)) {
			return datasets.get(0).getDatasetId();
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}
	}

	@Override
	public List<StudyEntryDto> getStudyEntries(final Integer studyId, final StudyEntrySearchDto.Filter filter, final Pageable pageable) {
		this.studyValidator.validate(studyId, false);
		return this.studyGermplasmService.getStudyEntries(studyId, filter, pageable);
	}

	@Override
	public long countAllStudyEntries(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.studyGermplasmService.countStudyEntries(studyId);
	}

	@Override
	public List<MeasurementVariable> getEntryDescriptorColumns(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		final Integer plotDatasetId = datasetService.getDatasets( studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))).get(0).getDatasetId();

		final List<MeasurementVariable> entryDescriptors =
			this.datasetService.getObservationSetVariables(plotDatasetId, Lists
				.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId()));

		//Remove OBS_UNIT_ID column and STOCKID if present
		for (Iterator<MeasurementVariable> i = entryDescriptors.iterator(); i.hasNext();) {
			final MeasurementVariable measurementVariable = i.next();
			if (measurementVariable.getTermId() == TermId.OBS_UNIT_ID.getId() || measurementVariable.getTermId() == TermId.STOCKID.getId()) {
				i.remove();
			}
		}

		//Add Inventory related columns
		entryDescriptors.add(this.buildVirtualColumn("LOTS", TermId.GID_ACTIVE_LOTS_COUNT));
		entryDescriptors.add(this.buildVirtualColumn("AVAILABLE", TermId.GID_AVAILABLE_BALANCE));
		entryDescriptors.add(this.buildVirtualColumn("UNIT", TermId.GID_UNIT));

		return entryDescriptors;
	}

	private MeasurementVariable buildVirtualColumn(final String name, final TermId termId) {
		final MeasurementVariable sampleColumn = new MeasurementVariable();
		sampleColumn.setName(name);
		sampleColumn.setAlias(name);
		sampleColumn.setTermId(termId.getId());
		sampleColumn.setFactor(true);
		return sampleColumn;
	}

}
