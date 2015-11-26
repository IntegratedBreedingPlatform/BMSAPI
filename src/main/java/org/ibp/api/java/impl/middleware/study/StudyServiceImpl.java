
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.api.study.TraitDto;
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
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Autowired
	private StudyDataManager studyDataManager;

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

	@Override
	public List<StudySummary> listAllStudies(final String programUniqueId) {
		final List<StudySummary> studySummaries = new ArrayList<StudySummary>();
		try {
			StudySearchParameters searchParameters = new StudySearchParameters();
			searchParameters.setProgramUniqueId(programUniqueId);
			final List<org.generationcp.middleware.service.api.study.StudySummary> mwStudySummaries =
					this.middlewareStudyService.listAllStudies(searchParameters);

			for (final org.generationcp.middleware.service.api.study.StudySummary mwStudySummary : mwStudySummaries) {
				if (!this.securityService.isAccessible(mwStudySummary)) {
					continue;
				}

				final StudySummary summary = new StudySummary(String.valueOf(mwStudySummary.getId()));
				summary.setName(mwStudySummary.getName());
				summary.setTitle(mwStudySummary.getTitle());
				summary.setObjective(mwStudySummary.getObjective());
				summary.setStartDate(mwStudySummary.getStartDate());
				summary.setEndDate(mwStudySummary.getEndDate());
				summary.setType(mwStudySummary.getType().getName());
				studySummaries.add(summary);
			}
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("Error! Caused by: " + e.getMessage(), e);
		}
		return studySummaries;
	}

	@Override
	public List<Observation> getObservations(final Integer studyId) {
		final List<ObservationDto> studyMeasurements = this.middlewareStudyService.getObservations(studyId);
		final List<Observation> observations = new ArrayList<Observation>();
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
		return StudyMapper.getInstance().map(measurement, Observation.class);
	}

	@Override
	public Observation updateObsevation(final Integer studyIdentifier, final Observation observation) {

		this.validateMeasurementSubmitted(studyIdentifier, observation);

		final List<Measurement> measurements = observation.getMeasurements();

		final List<MeasurementDto> traits = new ArrayList<MeasurementDto>();
		for (final Measurement measurement : measurements) {
			traits.add(new MeasurementDto(new TraitDto(measurement.getMeasurementIdentifier().getTrait().getTraitId(), measurement
					.getMeasurementIdentifier().getTrait().getTraitName()), measurement.getMeasurementIdentifier().getMeasurementId(),
					measurement.getMeasurementValue()));
		}
		final ObservationDto middlewareMeasurement =
				new ObservationDto(observation.getUniqueIdentifier(), observation.getEnvironmentNumber(), observation.getEntryType(),
						observation.getGermplasmId(), observation.getGermplasmDesignation(), observation.getEntryNumber(),
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
				final ObjectMapper objectMapper = new ObjectMapper();
				try {
					object.add(objectMapper.writeValueAsString(measurement));
				} catch (final JsonProcessingException e) {
					throw new ApiRuntimeException("Error mapping measurement to JSON", e);
				}
				final FieldError objectError =
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

	@Override
	public List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer) {
		final ModelMapper modelMapper = StudyMapper.getInstance();
		final List<StudyGermplasm> destination = new ArrayList<StudyGermplasm>();
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
			studyDetails.setTitle(study.getTitle());
			studyDetails.setObjective(study.getObjective());
			studyDetails.setType(study.getType());
			studyDetails.setStartDate(String.valueOf(study.getStartDate()));
			studyDetails.setEndDate(String.valueOf(study.getEndDate()));

			// Factors, Settings tab.
			final List<Variable> conditions = study.getConditions().getVariables();
			final VariableTypeList factors = this.studyDataManager.getAllStudyFactors(studyIdentifier);
			final List<DMSVariableType> factorDetails = factors.getVariableTypes();
			for (final DMSVariableType factorDetail : factorDetails) {
				String value = null;
				for (final Variable condition : conditions) {
					final String conditionName = condition.getVariableType().getLocalName();
					if (factorDetail.getLocalName().equals(conditionName)) {
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
				trait.setName(variateDetail.getStandardVariable().getName());
				trait.setDescription(variateDetail.getStandardVariable().getDescription());
				studyDetails.addTrait(trait);
			}

			// Datasets
			final List<DatasetReference> datasetReferences = this.studyDataManager.getDatasetReferences(studyIdentifier);
			if (datasetReferences != null && !datasetReferences.isEmpty()) {
				for (final DatasetReference dsRef : datasetReferences) {
					final DatasetSummary dsSummary = new DatasetSummary();
					dsSummary.setId(dsRef.getId().toString());
					dsSummary.setName(dsRef.getName());
					dsSummary.setDescription(dsRef.getDescription());
					studyDetails.addDataSet(dsSummary);

					// FIXME : Is there a cleaner way to tell whether a DataSet is an Environment dataset?
					if (dsRef.getName().endsWith("-ENVIRONMENT")) {
						// Logic derived from by RepresentationDataSetQuery.loadItems(int, int) method of the GermplasmStudyBrowser,
						// which is used to show dataset tables in the study browser UI.
						final List<Experiment> experiments = this.studyDataManager.getExperiments(dsRef.getId(), 0, Integer.MAX_VALUE);
						for (final Experiment experiment : experiments) {
							final List<Variable> variables = new ArrayList<Variable>();
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
								attr.setId(String.valueOf(variable.getVariableType().getId()));
								attr.setName(variable.getVariableType().getLocalName());
								attr.setDescription(variable.getVariableType().getLocalDescription());
								attr.setValue(variable.getDisplayValue());
								env.addEnvironmentDetail(attr);
							}
							studyDetails.addEnvironment(env);
						}
					}
				}
			}

			// Germplasm
			studyDetails.getGermplasm().addAll(this.getStudyGermplasmList(studyIdentifier));
			return studyDetails;
		} catch (final NumberFormatException nfe) {
			throw new ApiRuntimeException("Supplied study identifier [" + studyId + "] is not valid, it must be a numeric value.");
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("Error! Caused by: " + e.getMessage(), e);
		}
	}

	@Override
	public Map<Integer, FieldMap> getFieldMap(final String studyId) {
		final FieldMapService fieldMapService = new FieldMapService(this.studyDataManager);
		return fieldMapService.getFieldMap(studyId);
	}

	@Transactional
	@Override
	public Integer importStudy(final StudyImportDTO studyImportDTO, final String programUUID) {
		try {

			final Workbook workbook = this.conversionService.convert(studyImportDTO, Workbook.class);
			workbook.getStudyDetails().setProgramUUID(programUUID);

			// Save the study
			final Integer studyId = this.dataImportService.saveDataset(workbook, true, false, programUUID);

			// Create germplasm list
			final GermplasmList germplasmList = this.conversionService.convert(studyImportDTO, GermplasmList.class);
			final Integer listId = this.germplasmListManager.addGermplasmList(germplasmList);

			final List<GermplasmListData> germplasmListDatas = this.convert(studyImportDTO.getGermplasm(), GermplasmListData.class);
			for (final GermplasmListData germData : germplasmListDatas) {
				germData.setList(germplasmList);
			}
			this.germplasmListManager.addGermplasmListData(germplasmListDatas);

			// Create the study's snapshot of the Germplasm list (ListDataProject)
			final List<ListDataProject> listDataProjects = this.convert(studyImportDTO.getGermplasm(), ListDataProject.class);
			final GermplasmListType listType = this.extractGermListType(studyImportDTO);
			this.fieldbookService.saveOrUpdateListDataProject(studyId, listType, listId, listDataProjects, studyImportDTO.getUserId());

			return studyId;

		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error caused by: " + e.getMessage(), e);
		}
	}

	/**
	 * Infers the List type for a list, based on the study type of a given {@link StudyImportDTO}
	 *
	 * @param studyImportDTO
	 * @return the corresponding germplasm list type for a study workbook, or null if no valid type is found.
	 */
	private final GermplasmListType extractGermListType(final StudyImportDTO studyImportDTO) {
		final StudyType studyType = StudyType.valueOf(studyImportDTO.getStudyType());
		GermplasmListType listType;

		switch (studyType) {
			case N:
				listType = GermplasmListType.NURSERY;
				break;
			case T:
				listType = GermplasmListType.TRIAL;
				break;
			default:
				listType = null;
		}

		return listType;
	}

	private final <T, S> List<T> convert(final List<S> beanList, final Class<T> clazz) {
		if (null == beanList) {
			return null;
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

	void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
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

}
