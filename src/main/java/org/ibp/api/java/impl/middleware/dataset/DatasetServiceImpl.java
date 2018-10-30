package org.ibp.api.java.impl.middleware.dataset;

import java.util.List;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.ibp.api.domain.dataset.DatasetTrait;
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
	public MeasurementVariable addDatasetTrait(final Integer studyId, final Integer datasetId, final DatasetTrait datasetTrait) {
		this.studyValidator.validate(studyId, true);
		final Integer traitId = datasetTrait.getTraitId();
		final StandardVariable traitVariable = this.datasetValidator.validateDatasetTrait(studyId, datasetId, true, traitId, false);

		final String alias = datasetTrait.getStudyAlias();
		this.middlewareDatasetService.addTrait(datasetId, traitId, alias);
		final MeasurementVariable measurementVariable = this.measurementVariableTransformer.transform(traitVariable, false);
		measurementVariable.setName(alias);
		measurementVariable.setVariableType(VariableType.TRAIT);
		measurementVariable.setRequired(false);
		return measurementVariable;
	}

}
