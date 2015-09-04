
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
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
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.study.DatasetSummary;
import org.ibp.api.domain.study.Environment;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyAttribute;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.StudyWorkbook;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
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
	private ConversionService converter;

	@Override
	public List<StudySummary> listAllStudies(final String programUniqueId) {
		List<StudySummary> studySummaries = new ArrayList<StudySummary>();
		try {
			List<org.generationcp.middleware.service.api.study.StudySummary> mwStudySummaries =
					this.middlewareStudyService.listAllStudies(programUniqueId);

			for (org.generationcp.middleware.service.api.study.StudySummary mwStudySummary : mwStudySummaries) {
				StudySummary summary = new StudySummary(String.valueOf(mwStudySummary.getId()));
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
		final List<ObservationDto> singleObservation = this.middlewareStudyService.getSingleObservation(studyId, obeservationId);
		if (!singleObservation.isEmpty()) {
			return this.mapObservationDtoToObservation(singleObservation.get(0));
		}
		return new Observation();
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

	@Override
	public StudyDetails getStudyDetails(String studyId) {
		try {
			Integer studyIdentifier = Integer.valueOf(studyId);
			Study study = this.studyDataManager.getStudy(studyIdentifier);
			if (study == null) {
				throw new ApiRuntimeException("No study identified by the supplied studyId [" + studyId + "] was found.");
			}

			// Basic Info.
			StudyDetails studyDetails = new StudyDetails();
			studyDetails.setId(String.valueOf(study.getId()));
			studyDetails.setName(study.getName());
			studyDetails.setTitle(study.getTitle());
			studyDetails.setObjective(study.getObjective());
			studyDetails.setType(study.getType());
			studyDetails.setStartDate(String.valueOf(study.getStartDate()));
			studyDetails.setEndDate(String.valueOf(study.getEndDate()));

			// Factors, Settings tab.
			List<Variable> conditions = study.getConditions().getVariables();
			VariableTypeList factors = this.studyDataManager.getAllStudyFactors(studyIdentifier);
			List<VariableType> factorDetails = factors.getVariableTypes();
			for (VariableType factorDetail : factorDetails) {
				String value = null;
				for (Variable condition : conditions) {
					String conditionName = condition.getVariableType().getLocalName();
					if (factorDetail.getLocalName().equals(conditionName)) {
						value = condition.getDisplayValue();
					}
				}

				// Only add the attribute if there is a value associated.
				if (value != null) {
					StudyAttribute attr = new StudyAttribute();
					attr.setId(String.valueOf(factorDetail.getId()));
					attr.setName(factorDetail.getLocalName());
					attr.setDescription(factorDetail.getLocalDescription());
					attr.setValue(value);
					studyDetails.addGeneralInfo(attr);
				}
			}

			// Variates - Measurements tab.
			VariableTypeList variates = this.studyDataManager.getAllStudyVariates(studyIdentifier);
			List<VariableType> variateDetails = variates.getVariableTypes();
			for (VariableType variateDetail : variateDetails) {
				TermSummary trait = new TermSummary();
				trait.setId(String.valueOf(variateDetail.getId()));
				trait.setName(variateDetail.getStandardVariable().getName());
				trait.setDescription(variateDetail.getStandardVariable().getDescription());
				studyDetails.addTrait(trait);
			}

			// Datasets
			List<DatasetReference> datasetReferences = this.studyDataManager.getDatasetReferences(studyIdentifier);
			if (datasetReferences != null && !datasetReferences.isEmpty()) {
				for (DatasetReference dsRef : datasetReferences) {
					DatasetSummary dsSummary = new DatasetSummary();
					dsSummary.setId(dsRef.getId().toString());
					dsSummary.setName(dsRef.getName());
					dsSummary.setDescription(dsRef.getDescription());
					studyDetails.addDataSet(dsSummary);

					// FIXME : Is there a cleaner way to tell whether a DataSet is an Environment dataset?
					if (dsRef.getName().endsWith("-ENVIRONMENT")) {
						// Logic derived from by RepresentationDataSetQuery.loadItems(int, int) method of the GermplasmStudyBrowser,
						// which is used to show dataset tables in the study browser UI.
						List<Experiment> experiments = this.studyDataManager.getExperiments(dsRef.getId(), 0, Integer.MAX_VALUE);
						for (Experiment experiment : experiments) {
							List<Variable> variables = new ArrayList<Variable>();
							VariableList fac = experiment.getFactors();
							if (fac != null) {
								variables.addAll(fac.getVariables());
							}
							VariableList var = experiment.getVariates();
							if (var != null) {
								variables.addAll(var.getVariables());
							}

							Environment env = new Environment();
							for (Variable variable : variables) {
								StudyAttribute attr = new StudyAttribute();
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
		} catch (NumberFormatException nfe) {
			throw new ApiRuntimeException("Supplied study identifier [" + studyId + "] is not valid, it must be a numeric value.");
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error! Caused by: " + e.getMessage(), e);
		}
	}

	@Override
	public Map<Integer, FieldMap> getFieldMap(String studyId) {
		FieldMapService fieldMapService = new FieldMapService(this.studyDataManager);
		return fieldMapService.getFieldMap(studyId);
	}

	@Override
	public Integer addNewStudy(StudyWorkbook studyWorkbook, String programUUID) {
		try {
			
			//TODO convert factors, variates, constants, etc, to complete WORKBOOK before saving it
			// do it in converter's logic
			Workbook workbook = converter.convert(studyWorkbook, Workbook.class);
			workbook.getStudyDetails().setProgramUUID(programUUID);
			
			List<ListDataProject> listDataProjects = convert(studyWorkbook.getGermplasms(), ListDataProject.class);
			GermplasmListType listType = extractGermListType(studyWorkbook);

			// save list in DMS and Chado
			Integer userId = 1;
			Integer listId;
			Integer nurseryId = 0;
			GermplasmList germplasmList;

			germplasmList = converter.convert(studyWorkbook, GermplasmList.class);
			
			//add study meta
			nurseryId = middlewareStudyService.addNewStudy(workbook, programUUID);
			//add list meta
			listId = germplasmListManager.addGermplasmList(germplasmList);

			List<GermplasmListData> germplasmListDatas = convert(studyWorkbook.getGermplasms(), GermplasmListData.class);
			for(GermplasmListData germData : germplasmListDatas){
				germData.setList(germplasmList);
			}
			//add list of entries
			germplasmListManager.addGermplasmListData(germplasmListDatas);

			//add list of entries in project tables
			fieldbookService.saveOrUpdateListDataProject( nurseryId,
					listType,
					listId,
					listDataProjects,
					userId );

			return nurseryId;
			
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error caused by: " + e.getMessage(), e);
		}
	}

	/**
	 * Infers the List type for a list, based on the  study type of a given {@link StudyWorkbook}
	 * @param studyWorkbook
	 * @return the corresponding germplasm list type for a study workbook, or null if no valid type is found. 
	 */
	private final GermplasmListType extractGermListType(StudyWorkbook studyWorkbook) {
		StudyType studyType = StudyType.valueOf(studyWorkbook.getStudyType());
		GermplasmListType listType;
		
		switch( studyType ) {
			case N :  
				listType = GermplasmListType.NURSERY;
				break;
			case T :  
				listType = GermplasmListType.TRIAL;
				break;
			default: listType = null;
		}
		
		return listType;
	}
	
	private final <T,S>List<T> convert(List<S> beanList, Class<T> clazz){
        if(null == beanList) return null;
        
        List<T> convertedList = new ArrayList<>();
        for(S s : beanList){
                convertedList.add(converter.convert(s, clazz));
        }
        return convertedList;
	}
	
}
