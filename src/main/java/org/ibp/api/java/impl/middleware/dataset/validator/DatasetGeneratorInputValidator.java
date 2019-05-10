package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DatasetType;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DatasetGeneratorInputValidator {

	@Autowired
	private VariableService variableService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private Environment environment;

	private Integer maxAllowedSubobservationUnits;

	private Integer maxAllowedDatasetsPerParent;

	private final VariableType observationUnitVariableType;

	static final String DATASET_NAME_REGEX = "^[a-zA-Z0-9\\s(\\\\/:*?\\\"\"<>|.)]*$";

	static final Pattern DATASET_NAME_PATTERN = Pattern.compile(DatasetGeneratorInputValidator.DATASET_NAME_REGEX);



	DatasetGeneratorInputValidator() {
		this.observationUnitVariableType =
				new VariableType(org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
						org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
						org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());
	}

	@PostConstruct
	public void init() {
		this.maxAllowedSubobservationUnits = Integer.parseInt(this.environment.getProperty("maximum.number.of.sub.observation.parent.unit"));
		this.maxAllowedDatasetsPerParent = Integer.parseInt(this.environment.getProperty("maximum.number.of.sub.observation.sets"));
	}

	public void validateBasicData(final String crop, final Integer studyId, final Integer parentId, final DatasetGeneratorInput datasetInputGenerator, final Errors errors) {

		final DatasetDTO dataset = this.studyDatasetService.getDataset(parentId);

		final DatasetType datasetType = this.ontologyDataManager.getDatasetTypeById(datasetInputGenerator.getDatasetTypeId());
		if (datasetType == null) {
			errors.reject("dataset.type.id.not.exist", new String[] {String.valueOf(datasetInputGenerator.getDatasetTypeId())}, "");
			return;
		}

		if (!datasetType.isObservationType()) {
			errors.reject("dataset.parent.not.allowed");
			return;
		}

		// Validate that the parent dataset does not have more than X children
		if (this.studyDatasetService.getNumberOfChildren(parentId).equals(this.maxAllowedDatasetsPerParent)) {
			errors.reject("dataset.creation.not.allowed", new String[] {String.valueOf(this.maxAllowedDatasetsPerParent)}, "");
			return;
		}
		
		if (datasetInputGenerator.getDatasetName() != null && datasetInputGenerator.getDatasetName().length() > 100) {
			errors.reject("dataset.name.exceed.length");
		}

		if (datasetInputGenerator.getDatasetName() != null && datasetInputGenerator.getDatasetName().isEmpty()) {
			errors.reject("dataset.name.empty.name");
		}
		
		if(!DatasetGeneratorInputValidator.DATASET_NAME_PATTERN.matcher(datasetInputGenerator.getDatasetName()).matches()){
			errors.reject("dataset.name.invalid", new String[] {}, "");
		}

		final List<StudyInstance> studyInstances = dataset.getInstances();

		final Function<StudyInstance, Integer> studyInstancesToIds = new Function<StudyInstance, Integer>() {

			public Integer apply(final StudyInstance i) {
				return i.getInstanceDbId();
			}
		};

		final List<Integer> studyInstanceIds = Lists.transform(studyInstances, studyInstancesToIds);

		if (datasetInputGenerator.getInstanceIds().length == 0 || !studyInstanceIds.containsAll(Arrays.asList(datasetInputGenerator.getInstanceIds()))) {
			errors.reject("dataset.invalid.instances");
		}

		final Study study = this.studyDataManager.getStudy(studyId);
		try {
			final VariableDetails variableDetails =
				this.variableService.getVariableById(crop, study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId()));
			if (variableDetails == null || !variableDetails.getVariableTypes().contains(this.observationUnitVariableType)) {
				errors.reject("dataset.invalid.obs.unit.variable", new String[] {String.valueOf(datasetInputGenerator.getSequenceVariableId())}, "");
			}

		} catch (final ApiRequestValidationException e) {
			errors.reject("dataset.invalid.obs.unit.variable", new String[] {String.valueOf(datasetInputGenerator.getSequenceVariableId())}, "");
		}

		if (datasetInputGenerator.getNumberOfSubObservationUnits() > this.maxAllowedSubobservationUnits
			|| datasetInputGenerator.getNumberOfSubObservationUnits() <= 0) {
			errors.reject("dataset.invalid.number.subobs.units", new String[] {String.valueOf(this.maxAllowedSubobservationUnits)}, "");
		}
	}

	public void validateDataConflicts(final Integer studyId, final DatasetGeneratorInput o, final Errors errors) {
		final Study study = this.studyDataManager.getStudy(studyId);
		if (!this.studyDatasetService.isDatasetNameAvailable(o.getDatasetName(), study.getId())) {
			errors.reject("dataset.name.not.available", new String[] {o.getDatasetName()} , "");
		}
	}

	public void validateDatasetTypeIsImplemented(final Integer datasetTypeId, final Errors errors) {
		final DatasetType datasetType = this.ontologyDataManager.getDatasetTypeById(datasetTypeId);
		if (!datasetType.isSubObservationType()){
			errors.reject("dataset.operation.not.implemented", new String[] {String.valueOf(datasetTypeId)}, "");
		}
	}

	public Environment getEnvironment() {
		return this.environment;
	}

	public void setEnvironment(final Environment environment) {
		this.environment = environment;
	}
}
