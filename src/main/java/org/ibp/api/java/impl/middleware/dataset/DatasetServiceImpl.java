package org.ibp.api.java.impl.middleware.dataset;

import java.text.SimpleDateFormat;
import java.util.List;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.Observation;
import org.ibp.api.domain.dataset.ObservationValue;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private MeasurementVariableTransformer measurementVariableTransformer;

	@Override
	public long countPhenotypes(final Integer studyId, final Integer datasetId, final List<Integer> traitIds) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		return this.middlewareDatasetService.countPhenotypes(datasetId, traitIds);
	}

	@Override
	public MeasurementVariable addDatasetVariable(final Integer studyId, final Integer datasetId, final DatasetVariable datasetVariable) {
		this.studyValidator.validate(studyId, true);
		final Integer variableId = datasetVariable.getVariableId();
		final StandardVariable traitVariable =
			this.datasetValidator.validateDatasetVariable(studyId, datasetId, true, datasetVariable, false);

		final String alias = datasetVariable.getStudyAlias();
		final VariableType type = VariableType.getById(datasetVariable.getVariableTypeId());
		this.middlewareDatasetService.addVariable(datasetId, variableId, type, alias);
		final MeasurementVariable measurementVariable = this.measurementVariableTransformer.transform(traitVariable, false);
		measurementVariable.setName(alias);
		measurementVariable.setVariableType(type);
		measurementVariable.setRequired(false);
		return measurementVariable;
	}

	@Override
	public Observation updatePhenotype(final Integer observationUnitId, final Integer observationId, final ObservationValue observationValue) {

		final Phenotype phenotype = this.middlewareDatasetService
			.updatePhenotype(
				observationUnitId, observationId, observationValue.getCategoricalValueId(), observationValue.getValue(),
				observationValue.getStatus());

		final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMDD HH:MM:SS");
		final Observation observation = new Observation();
		observation.setObservationId(phenotype.getPhenotypeId());
		observation.setCategoricalValueId(phenotype.getcValueId());
		observation.setStatus(phenotype.getValueStatus() != null ? phenotype.getValueStatus().getName() : null);
		observation.setUpdatedDate(dateFormat.format(phenotype.getUpdatedDate()));
		observation.setCreatedDate(dateFormat.format(phenotype.getCreatedDate()));
		observation.setValue(phenotype.getValue());
		observation.setObservationUnitId(phenotype.getExperiment().getNdExperimentId());
		observation.setVariableId(phenotype.getObservableId());

		return observation;
	}

	@Override
	public void removeVariables(Integer studyId, Integer datasetId, List<Integer> variableIds) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, true, variableIds);
		this.middlewareDatasetService.removeVariables(datasetId, variableIds);
	}

}
