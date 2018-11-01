package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

	@Autowired
	private StudyDataManager studyDataManager;

	@Override
	public List<MeasurementVariable> getSubObservationSetColumns(final Integer studyId, final Integer subObservationSetId) {
		this.studyValidator.validate(studyId, false);

		// TODO generalize to any obs dataset (plot/subobs), make 3rd param false
		this.datasetValidator.validateDataset(studyId, subObservationSetId, true);

		return this.middlewareDatasetService.getSubObservationSetColumns(subObservationSetId);
	}

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
		final StandardVariable traitVariable = this.datasetValidator.validateDatasetVariable(studyId, datasetId, true, datasetVariable, false);

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
	public List<DatasetDTO> getDatasets(final Integer studyId, final Set<Integer> datasetTypeIds) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Set<Integer> datasetTypeIdList = new TreeSet<>();
		final Study study = this.studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		if (datasetTypeIds != null) {
			for (final Integer dataSetTypeId : datasetTypeIds) {
				final DataSetType dataSetType = DataSetType.findById(dataSetTypeId);
				if (dataSetType == null) {
					errors.reject("dataset.type.id.not.exist", new Object[] {dataSetTypeId}, "");
					throw new ResourceNotFoundException(errors.getAllErrors().get(0));
				} else {
					datasetTypeIdList.add(dataSetTypeId);
				}
			}
		}

		final List<org.generationcp.middleware.domain.dms.DatasetDTO> datasetDTOS =
			this.middlewareDatasetService.getDatasets(studyId, datasetTypeIdList);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<DatasetDTO> datasetDTOs = new ArrayList();
		for (final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO : datasetDTOS) {
			final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
			datasetDTOs.add(datasetDto);
		}
		return datasetDTOs;
	}

	@Override
	public DatasetDTO getDataset(final String crop, final Integer studyId, final Integer datasetId) {
		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(datasetId);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
		final List<StudyInstance> instances = new ArrayList();
		for (org.generationcp.middleware.service.impl.study.StudyInstance instance : datasetDTO.getInstances()) {
			final StudyInstance datasetInstance = mapper.map(instance, StudyInstance.class);
			instances.add(datasetInstance);
		}
		datasetDto.setInstances(instances);
		datasetDto.setStudyId(studyId);
		datasetDto.setCropName(crop);

		return datasetDto;
	}

	@Override
	public int countTotalObservationUnitsForDataset(final int datasetId, final int instanceId) {
		return this.middlewareDatasetService.countTotalObservationUnitsForDataset(datasetId, instanceId);
	}

	@Override
	public List<ObservationUnitRow> getObservationUnitRows(final int studyId, final int datasetId, final int instanceId,
		final int pageNumber, final int pageSize, final String sortBy, final String sortOrder) {
		this.studyValidator.validate(studyId, false);
		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows =
			this.middlewareDatasetService.getObservationUnitRows(studyId, datasetId, instanceId, pageNumber, pageSize, sortBy, sortOrder);
		final ModelMapper observationUnitRowMapper = new ModelMapper();
		final ModelMapper observationUnitDataMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		observationUnitDataMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<ObservationUnitRow> list = new ArrayList<>();
		for (final org.generationcp.middleware.service.api.dataset.ObservationUnitRow dto : observationUnitRows) {
			final Map<String, ObservationUnitData> datas = new HashMap<>();
			for (final String data : dto.getVariables().keySet()) {
				datas.put(data, observationUnitDataMapper.map(dto.getVariables().get(data), ObservationUnitData.class));
			}
			final ObservationUnitRow observationUnitRow = observationUnitRowMapper.map(dto, ObservationUnitRow.class);
			observationUnitRow.setVariables(datas);
			list.add(observationUnitRow);
		}
		return list;
	}
}
