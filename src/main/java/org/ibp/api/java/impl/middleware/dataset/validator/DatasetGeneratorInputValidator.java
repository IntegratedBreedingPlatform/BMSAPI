package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

	private final VariableType observationUnitVariableType;

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
	public void validateBasicData(final String crop, final Integer studyId, final Integer parentId, final DatasetGeneratorInput o, final Errors errors) {

		final List<DatasetDTO> allChildren = studyDatasetService.getDatasets(studyId, new HashSet<Integer>());
		boolean found = false;
		for (final DatasetDTO datasetDTO: allChildren) {
			if (datasetDTO.getDatasetId().equals(parentId)) {
				found = true;
				break;
			}
		}
		if (!found) {
			errors.reject("dataset.do.not.belong.to.study", new String[] {String.valueOf(parentId), String.valueOf(studyId)}, "");
			return;
		}

		//FIXME check that parentId has types PLOT, CUSTOM, PLANT, QUADRAT, TIMESERIE
		// Check using DataSetType.isObservationDatasetType(type)
		if (false) {
			errors.reject("dataset.parent.not.allowed");
			return;
		}

		if (!(dataset.getDatasetTypeId().equals(DataSetType.PLOT_DATA.getId()) || dataset.getDatasetTypeId()
			.equals(DataSetType.CUSTOM_SUBOBSERVATIONS.getId())
			|| dataset.getDatasetTypeId().equals(DataSetType.QUADRAT_SUBOBSERVATIONS.getId()) || dataset.getDatasetTypeId()
			.equals(DataSetType.TIME_SERIES_SUBOBSERVATIONS.getId()))) {
			errors.reject("dataset.parent.type.id.not.exist");
		}
		// Validate that the parent dataset does not have more than X children
		if (studyDatasetService.getNumberOfChildren(parentId).equals(maxAllowedDatasetsPerParent)) {
			errors.reject("dataset.creation.not.allowed", new String[] {String.valueOf(maxAllowedDatasetsPerParent)}, "");
			return;
		}

		if (DataSetType.findById(datasetInputGenerator.getDatasetTypeId()) == null) {
			errors.reject("dataset.type.id.not.exist", new String[] {String.valueOf(datasetInputGenerator.getDatasetTypeId())}, "");
		}

		if (datasetInputGenerator.getDatasetName() != null && datasetInputGenerator.getDatasetName().length() > 100) {
			errors.reject("dataset.name.exceed.length");
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

		if (datasetInputGenerator.getNumberOfSubObservationUnits() > this.maxAllowedSubobservationUnits) {
			errors.reject("dataset.invalid.number.subobs.units", new String[] {String.valueOf(this.maxAllowedSubobservationUnits)}, "");
		}
	}

	public void validateDataConflicts(final Integer studyId, final DatasetGeneratorInput o, final Errors errors) {
		final Study study = this.studyDataManager.getStudy(studyId);
		if (!this.studyDatasetService.isDatasetNameAvailable(o.getDatasetName(), study.getProgramUUID())) {
			errors.reject("dataset.name.not.available", new String[] {o.getDatasetName()} , "");
		}
	}

	public void validateDatasetTypeIsImplemented(final Integer datasetTypeId, final Errors errors) {
		final DataSetType type = DataSetType.findById(datasetTypeId);
		if (!DataSetType.isSubObservationDatasetType(type)){
			errors.reject("dataset.operation.not.implemented", new String[] {String.valueOf(datasetTypeId)}, "");
		}
	}

	public void validateParentBelongsToStudy(final Integer studyId, final Integer parentId, final Errors errors) {
		final List<DatasetDTO> datasets = this.studyDatasetService.getDatasets(studyId, new HashSet<Integer>());

		final List<Integer> parentDatasetIds = (List<Integer>) CollectionUtils.collect(datasets, new Transformer() {
			@Override
			public Integer transform(final Object input) {
				final DatasetDTO variable = (DatasetDTO) input;
				return variable.getParentDatasetId();
			}
		});

		if (!parentDatasetIds.contains(parentId)) {
			errors.reject("dataset.parent.does.not.belong.to.study");
		}
	}
}
