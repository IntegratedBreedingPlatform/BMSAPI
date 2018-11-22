package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.dataset.ObservationUnitImportResult;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.ObservationValue;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetGeneratorInputValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationUnitsTableBuilder;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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
	private ObservationValidator observationValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private MeasurementVariableTransformer measurementVariableTransformer;

	@Autowired
	private DatasetGeneratorInputValidator datasetGeneratorInputValidator;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private MeasurementVariableService measurementVariableService;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Resource
	private OntologyDataManager ontologyDataManager;

	private static final String DATA_TYPE_NUMERIC = "Numeric";

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
	public long countPhenotypesByInstance(final Integer studyId, final Integer datasetId, final Integer instanceId) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.instanceValidator.validate(datasetId, instanceId);
		return this.middlewareDatasetService.countPhenotypesByInstance(datasetId, instanceId);
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
	public void removeVariables(final Integer studyId, final Integer datasetId, final List<Integer> variableIds) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, true, variableIds);
		this.middlewareDatasetService.removeVariables(datasetId, variableIds);
	}

	@Override
	public ObservationDto addObservation(Integer studyId, Integer datasetId, Integer observationUnitId, ObservationDto observation) {

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, true, Arrays.asList(observation.getVariableId()));
		this.observationValidator.validateObservationUnit(datasetId, observationUnitId);
		return this.middlewareDatasetService.addPhenotype(observation);

	}

	@Override
	public ObservationDto updateObservation(
		final Integer studyId, final Integer datasetId, final Integer observationId, final Integer observationUnitId,
		final ObservationValue observationValue) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, true);
		this.observationValidator.validateObservation(datasetId, observationUnitId, observationId);
		return this.middlewareDatasetService
			.updatePhenotype(observationUnitId, observationId, observationValue.getCategoricalValueId(), observationValue.getValue());

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
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Study study = this.studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(studyId, datasetId);

		if (datasetDTO == null) {
			errors.reject("dataset.does.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
		final List<StudyInstance> instances = new ArrayList();
		for (final org.generationcp.middleware.service.impl.study.StudyInstance instance : datasetDTO.getInstances()) {
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
		this.datasetValidator.validateDataset(studyId, datasetId, true);
		this.instanceValidator.validate(datasetId, instanceId);
		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows =
			this.middlewareDatasetService.getObservationUnitRows(studyId, datasetId, instanceId, pageNumber, pageSize, sortBy, sortOrder);
		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<ObservationUnitRow> list = new ArrayList<>();
		for (final org.generationcp.middleware.service.api.dataset.ObservationUnitRow dto : observationUnitRows) {
			final Map<String, ObservationUnitData> datas = new HashMap<>();
			for (final String data : dto.getVariables().keySet()) {
				datas.put(data, observationUnitRowMapper.map(dto.getVariables().get(data), ObservationUnitData.class));
			}
			final ObservationUnitRow observationUnitRow = observationUnitRowMapper.map(dto, ObservationUnitRow.class);
			observationUnitRow.setVariables(datas);
			list.add(observationUnitRow);
		}
		return list;
	}


	@Override
	public DatasetDTO generateSubObservationDataset(final String cropName, final Integer studyId, final Integer parentId, final DatasetGeneratorInput datasetGeneratorInput) {

		// checks that study exists and it is not locked
		this.studyValidator.validate(studyId, true);

		// checks input matches validation rules
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());


		this.datasetValidator.validateDatasetBelongsToStudy(studyId, parentId);

		this.datasetGeneratorInputValidator.validateBasicData(cropName, studyId, parentId, datasetGeneratorInput, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		// not implemented yet
		this.datasetGeneratorInputValidator.validateDatasetTypeIsImplemented(datasetGeneratorInput.getDatasetTypeId(), bindingResult);
		if (bindingResult.hasErrors()) {
			throw new NotSupportedException(bindingResult.getAllErrors().get(0));
		}

		// conflict
		this.datasetGeneratorInputValidator.validateDataConflicts(studyId, datasetGeneratorInput, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ConflictException(bindingResult.getAllErrors());
		}

		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService
			.generateSubObservationDataset(studyId, datasetGeneratorInput.getDatasetName(), datasetGeneratorInput.getDatasetTypeId(),
				Arrays.asList(datasetGeneratorInput.getInstanceIds()), datasetGeneratorInput.getSequenceVariableId(),
				datasetGeneratorInput.getNumberOfSubObservationUnits(), parentId);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return mapper.map(datasetDTO, DatasetDTO.class);
	}

	@Override
	public void deleteObservation(final Integer studyId, final Integer datasetId, final Integer observationUnitId, final Integer observationId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, true);
		this.observationValidator.validateObservation(datasetId, observationUnitId, observationId);
		this.middlewareDatasetService.deletePhenotype(observationId);
	}

	@Override
	public ObservationUnitImportResult importObservations(final Integer studyId, final Integer datasetId, final List<List<String>> data) {

		final BindingResult
				errors = new MapBindingResult(new HashMap<String, String>(), data.getClass().getName());

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, true);

		final String programUUID = studyDataManager.getStudy(studyId).getProgramUUID();

		// get dataset variables that can be measured
		final List<MeasurementVariableDto> measurementVariables = this.measurementVariableService.getVariablesForDataset(datasetId,
				VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId());

		if (measurementVariables.isEmpty()) {
			errors.reject("no.variables.dataset", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final ObservationUnitsTableBuilder observationUnitsTableBuilder = new ObservationUnitsTableBuilder();

		final Table<String, String, String> table = observationUnitsTableBuilder.build(data, measurementVariables);

		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> currentData =
				middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, new ArrayList<String>(table.rowKeySet()));

		if (currentData.isEmpty()) {
			errors.reject("none.obs.unit.id.matches", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final int rowsNotBelongingToDataset = table.rowKeySet().size() - currentData.size();

		if (rowsNotBelongingToDataset != 0) {
			// remove elements that does not belong to the dataset
			final List<String> obsUnitIdsList = new ArrayList<>(table.rowKeySet());

			obsUnitIdsList.removeAll(currentData.keySet());

			for (final String obsUnitId: obsUnitIdsList) {
				table.row(obsUnitId).clear();
			}
		}

		// Check for data issues
		for (final String observationUnitId : table.rowKeySet()) {
			final org.generationcp.middleware.service.api.dataset.ObservationUnitRow storedObservations =
					currentData.get(observationUnitId);

			for (final String variableName : table.columnKeySet()) {

				final org.generationcp.middleware.service.api.dataset.ObservationUnitData observation =
						storedObservations.getVariables().get(variableName);

				final StandardVariable
						standardVariable = this.ontologyDataManager.getStandardVariable(observation.getVariableId(), programUUID);
				final MeasurementVariable measurementVariable = this.measurementVariableTransformer.transform(standardVariable, false);
				if (!this.isValidValue(measurementVariable, table.get(observationUnitId, variableName))) {
					//The numeric variableName {0} contains an invalid value {1} containing characters. Please check the data file and try again.
					errors.reject("warning.import.save.invalidCellValue", new String[] {variableName, table.get(observationUnitId, variableName)}, null);
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
			}
		}

		// Building warnings
		final List<String> warnings = new ArrayList<>();
		if (observationUnitsTableBuilder.areDuplicatedValues()) {
			warnings.add(resourceBundleMessageSource.getMessage("duplicated.obs.unit.id", null, LocaleContextHolder.getLocale()));
		}

		if (rowsNotBelongingToDataset != 0) {
			warnings.add(resourceBundleMessageSource
					.getMessage("some.obs.unit.id.matches", new String[] {String.valueOf(rowsNotBelongingToDataset)},
							LocaleContextHolder.getLocale()));
		}

		boolean overwrittingData = false;

		externalLoop:
		for (final String observationUnitId : table.rowKeySet()) {

			final org.generationcp.middleware.service.api.dataset.ObservationUnitRow storedObservations =
					currentData.get(observationUnitId);

			for (final String variableName : table.columnKeySet()) {

				final org.generationcp.middleware.service.api.dataset.ObservationUnitData observation =
						storedObservations.getVariables().get(variableName);

				if (observation != null && observation.getValue() != null && !observation.getValue()
						.equalsIgnoreCase(table.get(observationUnitId, variableName))) {
					overwrittingData = true;

					break externalLoop;
				}
			}

		}

		if (overwrittingData) {
			warnings.add(this.resourceBundleMessageSource.getMessage("warning.import.overwrite.data", null, LocaleContextHolder.getLocale()));
		}


		return null;
	}


	private boolean isValidValue(
			final MeasurementVariable var, final String value) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (var.getMinRange() != null && var.getMaxRange() != null) {
			return this.validateIfValueIsMissingOrNumber(value.trim());
		} else if (var != null && var.getDataTypeId() != null && var.getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			return Util.isValidDate(value);
		} else if (StringUtils.isNotBlank(var.getDataType()) && var.getDataType().equalsIgnoreCase(DATA_TYPE_NUMERIC)) {
			return this.validateIfValueIsMissingOrNumber(value.trim());
		}
		return true;
	}

	private boolean validateIfValueIsMissingOrNumber(final String value) {
		if (MeasurementData.MISSING_VALUE.equals(value.trim())) {
			return true;
		}
		return NumberUtils.isNumber(value);
	}

}
