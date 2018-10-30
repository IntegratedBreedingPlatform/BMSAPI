package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.springframework.validation.Errors;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
public class DatasetGeneratorInputValidator {

	@Autowired
	private StudyService studyService;

	@Autowired
	private VariableService variableService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private Environment environment;

	private Integer maxAllowedSubobservationUnits;

	private Integer maxAllowedDatasetsPerParent;

	private VariableType observationUnitVariableType;

	DatasetGeneratorInputValidator() {
		observationUnitVariableType =
				new VariableType(org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
						org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
						org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());
	}

	@PostConstruct
	public void init() {
		maxAllowedSubobservationUnits = Integer.parseInt(environment.getProperty("maximum.number.of.sub.observation.parent.unit"));
		maxAllowedDatasetsPerParent = Integer.parseInt(environment.getProperty("maximum.number.of.sub.observation.sets"));
	}

	public void validateBasicData(final String crop, final Integer studyId, final Integer parentId, final DatasetGeneratorInput o, final Errors errors) {

		//FIXME check that parentId has types PLOT, CUSTOM, PLANT, QUADRAT, TIMESERIE

		// Validate that the parent dataset does not have more than X children

		if (DataSetType.findById(o.getDatasetTypeId()) == null) {
			errors.reject("dataset.type.invalid", new String[] {String.valueOf(o.getDatasetTypeId())}, "");
		}

		if (o.getDatasetName() != null && o.getDatasetName().length() > 100) {
			errors.reject("dataset.name.exceed.length");
		}

		//FIXME needs to be replaced by getInstances of the parentId
		final List<StudyInstance> studyInstances = this.studyService.getStudyInstances(studyId);

		Function<StudyInstance, Integer> studyInstancesToIds = new Function<StudyInstance, Integer>() {

			public Integer apply(StudyInstance i) {
				return i.getInstanceDbId();
			}
		};

		List<Integer> studyInstanceIds = Lists.transform(studyInstances, studyInstancesToIds);

		if (o.getInstanceIds().length == 0 || !studyInstanceIds.containsAll(Arrays.asList(o.getInstanceIds()))) {
			errors.reject("dataset.invalid.instances");
		}

		final Study study = studyDataManager.getStudy(studyId);
		try {
			final VariableDetails variableDetails =
					variableService.getVariableById(crop, study.getProgramUUID(), String.valueOf(o.getSequenceVariableId()));
			if (variableDetails == null || !variableDetails.getVariableTypes().contains(observationUnitVariableType)) {
				errors.reject("dataset.invalid.obs.unit.variable", new String[] {String.valueOf(o.getSequenceVariableId())}, "");
			}

		} catch (ApiRequestValidationException e) {
			errors.reject("dataset.invalid.obs.unit.variable", new String[] {String.valueOf(o.getSequenceVariableId())}, "");
		}

		if (o.getNumberOfSubObservationUnits() > maxAllowedSubobservationUnits) {
			errors.reject("dataset.invalid.number.subobs.units", new String[] {String.valueOf(maxAllowedSubobservationUnits)}, "");
		}
	}

	public void validateDataConflicts(final Integer studyId, final DatasetGeneratorInput o, final Errors errors) {
		final Study study = studyDataManager.getStudy(studyId);
		if (!studyDatasetService.isDatasetNameAvailable(o.getDatasetName(), study.getProgramUUID())) {
			errors.reject("dataset.name.not.available", new String[] {o.getDatasetName()} , "");
		}
	}

	public void validateDatasetTypeIsImplemented(final Integer datasetTypeId, final Errors errors) {
		final DataSetType type = DataSetType.findById(datasetTypeId);
		if (!DataSetType.CUSTOM_SUBOBSERVATIONS.equals(type) && !DataSetType.PLANT_SUBOBSERVATIONS.equals(type)
				&& !DataSetType.QUADRAT_SUBOBSERVATIONS.equals(type) && !DataSetType.TIME_SERIES_SUBOBSERVATIONS.equals(type)) {
			errors.reject("dataset.operation.not.implemented", new String[] {String.valueOf(datasetTypeId)}, "");
		}
	}
}
