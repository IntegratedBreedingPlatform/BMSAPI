package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.api.brapi.v1.observation.ObservationDTO;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dataset.PlotDatasetPropertiesDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.dataset.FilteredPhenotypesInstancesCountDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitEntryReplaceRequest;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsParamDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.PreconditionFailedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetGeneratorInputValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyBookTableValidator;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.java.impl.middleware.name.validator.GermplasmNameTypeValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.VariableValidator;
import org.ibp.api.java.impl.middleware.study.ObservationUnitsMetadata;
import org.ibp.api.java.impl.middleware.study.StudyEntryServiceImpl;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.ibp.api.rest.dataset.EnvironmentVariableValuesPutRequestInput;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	static final String LOCATION_ID_VARIABLE_NAME = "LOCATION";
	static final String LOCATION_ABBR_VARIABLE_NAME = "LOCATION ABBREVIATION";
	private static final List<Integer> PROTECTED_VARIABLE_IDS =
		Arrays.asList(TermId.TRIAL_INSTANCE_FACTOR.getId(), TermId.LOCATION_ID.getId(), TermId.ENTRY_NO.getId(), TermId.ENTRY_TYPE.getId());
	public static final String MISSING_VALUE = "missing";
	public static final String NOT_AVAILABLE_VALUE = "NA";
	public static final String PARAM_NULL = "param.null";

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private StudyEntryValidator studyEntryValidator;

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
	private StudyBookTableValidator studyBookTableValidator;

	@Autowired
	private DatasetTypeService datasetTypeService;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private StudyTransactionsService studyTransactionsService;

	@Autowired
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Autowired
	private VariableValidator variableValidator;

	@Autowired
	private GermplasmNameTypeValidator germplasmNameTypeValidator;

	static final String PLOT_DATASET_NAME = "Observations";

	@Override
	public List<MeasurementVariable> getObservationSetColumns(
		final Integer studyId, final Integer datasetId, final Boolean draftMode) {

		this.studyValidator.validate(studyId, false);

		this.datasetValidator.validateDataset(studyId, datasetId);

		return this.removePedigreeRelatedVariablesIfNecessary(this.middlewareDatasetService.getObservationSetColumns(studyId, datasetId, draftMode));
	}

	@Override
	public List<MeasurementVariable> getSubObservationSetVariables(
		final Integer studyId, final Integer datasetId) {

		this.studyValidator.validate(studyId, false);

		this.datasetValidator.validateDataset(studyId, datasetId);

		return this.removePedigreeRelatedVariablesIfNecessary(this.middlewareDatasetService.getObservationSetVariables(datasetId));
	}

	@Override
	public long countObservationsByVariables(final Integer studyId, final Integer datasetId, final List<Integer> variableIds) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);

		return this.middlewareDatasetService.countObservationsByVariables(datasetId, variableIds);
	}

	@Override
	public long countObservationsByInstance(final Integer studyId, final Integer datasetId, final Integer instanceId) {
		this.validateStudyDatasetAndInstances(studyId, datasetId, Arrays.asList(instanceId));
		return this.middlewareDatasetService.countObservationsByInstance(datasetId, instanceId);
	}

	@Override
	public MeasurementVariable addDatasetVariable(final Integer studyId, final Integer datasetId, final DatasetVariable datasetVariable,
		final VariableType variableType) {
		this.studyValidator.validate(studyId, true);

		final Integer variableId = datasetVariable.getVariableId();
		final StandardVariable traitVariable =
			this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, false, variableType);

		final String alias = this.getAlias(datasetVariable, traitVariable);
		final VariableType type = VariableType.getById(datasetVariable.getVariableTypeId());
		this.middlewareDatasetService.addDatasetVariable(datasetId, variableId, type, alias);

		final MeasurementVariable measurementVariable = this.measurementVariableTransformer.transform(traitVariable, false);
		measurementVariable.setName(alias);
		measurementVariable.setVariableType(type);
		measurementVariable.setRequired(false);
		return measurementVariable;
	}

	@Override
	public void addDatasetVariables(final Integer studyId, final Integer datasetId,
									final List<DatasetVariable> datasetVariables, final VariableType variableType) {
		this.studyValidator.validate(studyId, true);

		datasetVariables.forEach(datasetVariable -> {
			final StandardVariable traitVariable =
				this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, false, variableType);

			this.middlewareDatasetService.addDatasetVariable(datasetId, datasetVariable.getVariableId(),
				VariableType.getById(datasetVariable.getVariableTypeId()), this.getAlias(datasetVariable, traitVariable));
		});
	}

	private String getAlias(final DatasetVariable datasetVariable, final StandardVariable traitVariable) {
		return datasetVariable.getStudyAlias() != null ? datasetVariable.getStudyAlias() : traitVariable.getName();
	}

	@Override
	public List<MeasurementVariableDto> getDatasetVariablesByType(
		final Integer studyId, final Integer datasetId, final VariableType variableType) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		return this.middlewareDatasetService.getDatasetVariablesByType(datasetId, variableType);
	}

	@Override
	public void removeDatasetVariables(final Integer studyId, final Integer datasetId, final List<Integer> variableIds,
		final VariableType variableType) {
		this.studyValidator.validate(studyId, true);
		for (final Integer variableId : variableIds) {
			if (DatasetServiceImpl.PROTECTED_VARIABLE_IDS.contains(variableId)) {
				final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
				errors.reject("dataset.protected.variable.cannot.be.deleted", new Object[] {String.valueOf(variableId)}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, variableIds);
		this.datasetValidator.validateNotRemovingSystemEntryDetailToAlreadyGeneratedExperiment(datasetId, variableIds);
		this.middlewareDatasetService.removeDatasetVariables(studyId, datasetId, variableIds);
	}

	@Override
	public ObservationDto createObservation(
		final Integer studyId, final Integer datasetId, final Integer observationUnitId, final ObservationDto observation) {

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Arrays.asList(observation.getVariableId()));
		this.observationValidator.validateObservationUnit(datasetId, observationUnitId);
		this.observationValidator.validateVariableValue(observation.getVariableId(), observation.getValue());
		return this.middlewareDatasetService.createObservation(observation);

	}

	@Override
	public ObservationDto updateObservation(
		final Integer studyId, final Integer datasetId, final Integer observationId, final Integer observationUnitId,
		final ObservationDto observationDto) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.observationValidator.validateObservation(datasetId, observationUnitId, observationId, observationDto);
		observationDto.setObservationUnitId(observationUnitId);
		observationDto.setObservationId(observationId);
		return this.middlewareDatasetService.updatePhenotype(observationId, observationDto);

	}

	@Override
	public List<DatasetDTO> getDatasets(final Integer studyId, final Set<Integer> datasetTypeIds) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final Set<Integer> datasetTypeIdList = new TreeSet<>();
		this.studyValidator.validate(studyId, false);

		final Map<Integer, DatasetTypeDTO> datasetTypeMap = this.datasetTypeService.getAllDatasetTypesMap();
		if (datasetTypeIds != null) {
			for (final Integer dataSetTypeId : datasetTypeIds) {
				if (!datasetTypeMap.containsKey(dataSetTypeId)) {
					errors.reject("dataset.type.id.not.exist", new Object[] {dataSetTypeId}, "");
					throw new ResourceNotFoundException(errors.getAllErrors().get(0));
				} else {
					datasetTypeIdList.add(dataSetTypeId);
				}
			}
		}

		final List<org.generationcp.middleware.domain.dms.DatasetDTO> datasetDTOS =
			this.middlewareDatasetService.getDatasetsWithVariables(studyId, datasetTypeIdList);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<DatasetDTO> datasetDTOs = new ArrayList<>();
		for (final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO : datasetDTOS) {
			final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
			if (datasetDto.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
				datasetDto.setName(PLOT_DATASET_NAME);
			}
			final List<org.generationcp.middleware.service.impl.study.StudyInstance> datasetInstances =
				this.middlewareDatasetService.getDatasetInstances(datasetDTO.getDatasetId());
			datasetDto.setInstances(this.convertToStudyInstances(mapper, datasetInstances));
			datasetDTOs.add(datasetDto);
		}
		return datasetDTOs;
	}

	@Override
	public DatasetDTO getDataset(final String crop, final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(datasetId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
		if (datasetDto.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			datasetDto.setName(PLOT_DATASET_NAME);
		}
		datasetDto.setInstances(this.convertToStudyInstances(mapper, datasetDTO.getInstances()));
		datasetDto.setStudyId(studyId);
		datasetDto.setCropName(crop);

		return datasetDto;
	}

	@Override
	public Integer countAllObservationUnitsForDataset(
		final Integer datasetId, final List<Integer> instanceIds, final Boolean draftMode) {
		return this.middlewareDatasetService.countAllObservationUnitsForDataset(datasetId, instanceIds, draftMode);
	}

	@Override
	public long countFilteredObservationUnitsForDataset(
		final Integer datasetId, final List<Integer> instanceIds, final Boolean draftMode,
		final ObservationUnitsSearchDTO.Filter filter) {
		return this.middlewareDatasetService.countFilteredObservationUnitsForDataset(datasetId, instanceIds, draftMode, filter);
	}

	@Override
	public List<StudyInstance> getDatasetInstances(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return this.convertToStudyInstances(mapper, this.middlewareDatasetService.getDatasetInstances(datasetId));
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getInstanceObservationUnitRowsMap(
		final int studyId, final int datasetId, final List<Integer> instanceIds) {
		this.validateStudyDatasetAndInstances(studyId, datasetId, instanceIds);
		final Map<Integer, List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow>> observationUnitRowsMap =
			this.middlewareDatasetService.getInstanceIdToObservationUnitRowsMap(studyId, datasetId, instanceIds);
		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final Map<Integer, List<ObservationUnitRow>> map = new LinkedHashMap<>();
		for (final Map.Entry<Integer, List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow>> entry : observationUnitRowsMap
			.entrySet()) {
			final Integer instanceNumber = entry.getKey();
			final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows =
				observationUnitRowsMap.get(instanceNumber);
			final List<ObservationUnitRow> list = new ArrayList<>();
			this.mapObservationUnitRows(observationUnitRowMapper, observationUnitRows, list);
			map.put(instanceNumber, list);
		}
		return map;
	}

	void validateStudyDatasetAndInstances(
		final int studyId, final int datasetId, final List<Integer> instanceIds) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		if (instanceIds != null) {
			this.instanceValidator.validate(datasetId, new HashSet<>(instanceIds));
		}
	}

	@Override
	public List<ObservationUnitRow> getObservationUnitRows(
		final int studyId, final int datasetId, final ObservationUnitsSearchDTO searchDTO, final Pageable pageable) {

		// TODO: we need to remove this because it won't work when names will be added to observations table.
		// We are assuming that every sort property correspond to a term, and this won't be longer valid for names
		Pageable convertedPageable = null;
		// TODO: Fix me! pagination must not depends if the sort parameter is present or not.
		if (pageable != null && pageable.getSort() != null) {
			final Iterator<Sort.Order> iterator = pageable.getSort().iterator();
			if (iterator.hasNext()) {
				// Convert the sort property name from termid to actual term name.
				final Sort.Order sort = iterator.next();
				final String sortProperty;
				if (NumberUtils.isNumber(sort.getProperty()) && Integer.valueOf(sort.getProperty()) > 0) {
					final Term term = this.ontologyDataManager.getTermById(Integer.valueOf(sort.getProperty()));
					sortProperty = term.getName();
				} else {
					sortProperty = sort.getProperty();
				}
				pageable.getSort().and(new Sort(sort.getDirection(), sortProperty));
				convertedPageable =
					new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort.getDirection(), sortProperty);
			}
		}

		this.validateStudyDatasetAndInstances(studyId, datasetId, searchDTO.getInstanceIds());

		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows =
			this.middlewareDatasetService.getObservationUnitRows(studyId, datasetId, searchDTO, convertedPageable);

		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<ObservationUnitRow> list = new ArrayList<>();
		this.mapObservationUnitRows(observationUnitRowMapper, observationUnitRows, list);

		return list;
	}

	@Override
	public List<Map<String, Object>> getObservationUnitRowsAsMapList(
		final int studyId, final int datasetId, final ObservationUnitsSearchDTO searchDTO) {

		this.validateStudyDatasetAndInstances(studyId, datasetId, searchDTO.getInstanceIds());

		return this.middlewareDatasetService.getObservationUnitRowsAsMapList(studyId, datasetId, searchDTO, null);
	}

	@Override
	public DatasetDTO generateSubObservationDataset(
		final String cropName, final Integer studyId, final Integer parentId, final DatasetGeneratorInput datasetGeneratorInput) {

		// checks that study exists and it is not locked
		this.studyValidator.validate(studyId, true);

		// checks input matches validation rules
		final BindingResult bindingResult = new MapBindingResult(new HashMap<>(), DatasetGeneratorInput.class.getName());

		this.datasetValidator.validateDatasetBelongsToStudy(studyId, parentId);
		this.instanceValidator.validate(parentId, Sets.newHashSet(datasetGeneratorInput.getInstanceIds()));
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
	public void deleteObservation(
		final Integer studyId, final Integer datasetId, final Integer observationUnitId, final Integer observationId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.observationValidator.validateObservation(datasetId, observationUnitId, observationId, null);
		this.middlewareDatasetService.deletePhenotype(observationId);

	}

	private List<StudyInstance> convertToStudyInstances(
		final ModelMapper mapper, final List<org.generationcp.middleware.service.impl.study.StudyInstance> middlewareStudyInstances) {

		final List<StudyInstance> instances = new ArrayList<>();
		for (final org.generationcp.middleware.service.impl.study.StudyInstance instance : middlewareStudyInstances) {
			final StudyInstance datasetInstance = mapper.map(instance, StudyInstance.class);
			instances.add(datasetInstance);
		}
		return instances;
	}

	@Override
	public void importObservations(final Integer studyId, final Integer datasetId, final ObservationsPutRequestInput input) {

		BindingResult errors = new MapBindingResult(new HashMap<>(), ObservationsPutRequestInput.class.getName());

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);

		this.studyBookTableValidator.validateList(input.getData());

		final List<MeasurementVariable> datasetMeasurementVariables =
			this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId);

		if (datasetMeasurementVariables.isEmpty()) {
			errors.reject("no.variables.dataset", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		this.addObsUnitIdColumnAndValuesIfNecessary(errors, studyId, datasetId, input);
		final StudyBookTableBuilder studyBookTableBuilder = new StudyBookTableBuilder();
		final Table<String, String, String> table = studyBookTableBuilder.buildObservationsTable(input.getData(), datasetMeasurementVariables);

		// Get Map<OBS_UNIT_ID, Observations>
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = this.middlewareDatasetService
			.getObservationUnitsAsMap(datasetId, datasetMeasurementVariables, new ArrayList<>(table.rowKeySet()));

		if (storedData.isEmpty()) {
			errors.reject("none.obs.unit.id.matches", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final int rowsNotBelongingToDataset = table.rowKeySet().size() - storedData.size();

		// remove elements that does not belong to the dataset
		if (rowsNotBelongingToDataset != 0) {
			final List<String> obsUnitIdsList = new ArrayList<>(table.rowKeySet());
			obsUnitIdsList.removeAll(storedData.keySet());
			for (final String obsUnitId : obsUnitIdsList) {
				table.row(obsUnitId).clear();
			}
		}
		// Convert date values if necessary
		this.correctKSUDateFormatIfNecessary(table, datasetMeasurementVariables);

		// Check for data issues
		this.studyBookTableValidator.validateStudyBookValuesDataTypes(table, datasetMeasurementVariables, false);

		// Processing warnings
		if (input.isProcessWarnings()) {
			errors = this.processObservationsDataWarningsAsErrors(table, storedData, rowsNotBelongingToDataset,
				studyBookTableBuilder.getDuplicatedFoundNumber(), input.isDraftMode());
		}
		if (!errors.hasErrors()) {
			this.middlewareDatasetService.importDataset(datasetId, table, input.isDraftMode(), false);
		} else {
			throw new PreconditionFailedException(errors.getAllErrors());
		}

	}

	@Override
	public void importEnvironmentVariableValues(final Integer studyId, final Integer datasetId, final EnvironmentVariableValuesPutRequestInput input) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), EnvironmentVariableValuesPutRequestInput.class.getName());

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);

		this.studyBookTableValidator.validateList(input.getData());

		final List<MeasurementVariable> datasetMeasurementVariables =
				this.middlewareDatasetService.getDatasetMeasurementVariablesByVariableType(datasetId,
						Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.ENVIRONMENT_CONDITION.getId()));

		if (datasetMeasurementVariables.isEmpty()) {
			errors.reject("no.variables.dataset", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final StudyBookTableBuilder studyBookTableBuilder = new StudyBookTableBuilder();
		final Table<String, String, String> table = studyBookTableBuilder.buildEnvironmentVariablesTable(input.getData(), datasetMeasurementVariables);

		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> trialInstanceMap =
				this.getEnvironmentObservationUnitRows(datasetMeasurementVariables, studyId, datasetId);
		// remove elements that does not belong to the dataset
		final List<String> trialInstanceNumberList = new ArrayList<>(table.rowKeySet());
		trialInstanceNumberList.removeAll(trialInstanceMap.keySet());
		for (final String trialInstanceNumber : trialInstanceNumberList) {
			table.row(trialInstanceNumber).clear();
		}

		// Check for data issues
		this.studyBookTableValidator.validateStudyBookValuesDataTypes(table, datasetMeasurementVariables, true);

		if (studyBookTableBuilder.getDuplicatedFoundNumber() > 0) {
			errors.reject("duplicated.trial.instance.number", null, "");
		}

		if (trialInstanceNumberList.size() > 0) {
			errors.reject("some.trial.instance.number.invalid",null, "");
		}

		if (!errors.hasErrors()) {
			this.middlewareDatasetService.importEnvironmentVariableValues(studyId, datasetId, table);
		} else {
			throw new PreconditionFailedException(errors.getAllErrors());
		}

	}

	// FIXME assuming one dataset for now
	@Override
	public void importObservations(final Integer studyDbId, final List<ObservationDTO> observations) {

		BindingResult errors = new MapBindingResult(new HashMap<>(), ObservationsPutRequestInput.class.getName());

		final org.generationcp.middleware.domain.dms.DatasetDTO
			dataset = this.middlewareDatasetService.getDatasetByObsUnitDbId(observations.get(0).getObservationUnitDbId());
		final int datasetId = dataset.getDatasetId();

		// Study should be unlocked before importing the observation
		final Integer studyId = this.studyDataManager.getProjectIdByStudyDbId(studyDbId);
		this.studyValidator.checkIfStudyIsLockedForCurrentUser(studyId);

		final List<MeasurementVariable> datasetMeasurementVariables =
			this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId);

		if (datasetMeasurementVariables.isEmpty()) {
			errors.reject("no.variables.dataset", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		// transform BrAPI format to DatasetService format
		final ObservationsPutRequestInput input = transformObservations(observations, datasetMeasurementVariables);

		this.studyBookTableValidator.validateList(input.getData());

		final StudyBookTableBuilder studyBookTableBuilder = new StudyBookTableBuilder();
		final Table<String, String, String> table = studyBookTableBuilder.buildObservationsTable(input.getData(), datasetMeasurementVariables);

		// Get Map<OBS_UNIT_ID, Observations>
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = this.middlewareDatasetService
			.getObservationUnitsAsMap(datasetId, datasetMeasurementVariables, new ArrayList<>(table.rowKeySet()));

		if (storedData.isEmpty()) {
			errors.reject("none.obs.unit.id.matches", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final int rowsNotBelongingToDataset = table.rowKeySet().size() - storedData.size();

		// remove elements that does not belong to the dataset
		if (rowsNotBelongingToDataset != 0) {
			final List<String> obsUnitIdsList = new ArrayList<>(table.rowKeySet());
			obsUnitIdsList.removeAll(storedData.keySet());
			for (final String obsUnitId : obsUnitIdsList) {
				table.row(obsUnitId).clear();
			}
		}

		// Check for data issues
		this.studyBookTableValidator.validateStudyBookValuesDataTypes(table, datasetMeasurementVariables, false);

		// Processing warnings
		if (input.isProcessWarnings()) {
			errors = this.processObservationsDataWarningsAsErrors(table, storedData, rowsNotBelongingToDataset,
				studyBookTableBuilder.getDuplicatedFoundNumber(), input.isDraftMode());
		}
		if (!errors.hasErrors()) {
			final Table<String, Integer, Integer> observationDbIdsTable =
				this.middlewareDatasetService.importDataset(datasetId, table, input.isDraftMode(), true);
			// We need to return the observationDbIds (mapped in a table by observationUnitId and variableId) of the created/updated observations.
			observations.stream().forEach(
				o -> o.setObservationDbId(observationDbIdsTable.get(o.getObservationUnitDbId(), o.getObservationVariableDbId())));
		} else {
			throw new PreconditionFailedException(errors.getAllErrors());
		}

	}

	void addObsUnitIdColumnAndValuesIfNecessary(final BindingResult errors, final Integer studyId, final Integer datasetId, final ObservationsPutRequestInput input) {
		final org.generationcp.middleware.domain.dms.DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);
		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(dataSet.getDatasetTypeId());
		// Check if the dataset where the data will be imported is SubObservationType.
		// Imported data with no OBS_UNIT_ID column means that the data is imported from a transposed file
		if (datasetType.isSubObservationType()) {
			final List<List<String>> data = input.getData();
			final List<String> headers = data.get(0);
			if (!headers.contains(StudyBookTableBuilder.OBS_UNIT_ID)) {
				if(!headers.contains(StudyBookTableBuilder.TRIAL_INSTANCE)) {
					errors.reject("required.header.trial.instance", null, "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
				if(!headers.contains(StudyBookTableBuilder.PLOT_NO)) {
					errors.reject("required.header.plot.no", null, "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
				final int trialInstanceIndex = headers.indexOf(StudyBookTableBuilder.TRIAL_INSTANCE);
				final Set<Integer> instanceNumbers = new HashSet<>();
				final List<List<String>> values = data.subList(1, data.size());
				for(final List<String> row: values) {
					final String trialInstanceValue = row.get(trialInstanceIndex);
					if (StringUtils.isEmpty(trialInstanceValue)) {
						errors.reject("empty.trial.instance", null, "");
						throw new ApiRequestValidationException(errors.getAllErrors());
					}
					instanceNumbers.add(Integer.valueOf(trialInstanceValue));
				}
				// Retrieve OBS_UNIT_ID values using TRIAL_INSTANCE, PLOT_NO, and OBSERVATION_UNIT variable value
				final List<Integer> instanceIds = dataSet.getInstances().stream().filter(instance -> instanceNumbers.contains(instance.getInstanceNumber()))
						.map(org.generationcp.middleware.service.impl.study.StudyInstance::getInstanceId).collect(Collectors.toList());
				final Map<Integer, List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow>> observationUnitRowsMap =
						this.middlewareDatasetService.getInstanceIdToObservationUnitRowsMap(studyId, datasetId, instanceIds);

				// <TRIAL_INSTANCE, <PLOT_NO, <OBSERVATION_UNIT variable value, OBS_UNIT_ID>>> MAP
				final Map<String, Map<String, Map<String, String>>> obsUnitIdMap = new HashMap<>();
				final Optional<MeasurementVariable> observationUnitVariable = this.getObservationUnitVariable(dataSet.getVariables());
				final String observationUnitVariableName = observationUnitVariable.get().getName();
				for (final Integer instanceId: observationUnitRowsMap.keySet()) {
					final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> rows = observationUnitRowsMap.get(instanceId);
					for (final org.generationcp.middleware.service.api.dataset.ObservationUnitRow row: rows) {
						final String instanceNumber = row.getTrialInstance().toString();
						obsUnitIdMap.putIfAbsent(instanceNumber, new HashMap<>());
						final String plotNo = row.getVariableValueByVariableId(TermId.PLOT_NO.getId());
						obsUnitIdMap.get(instanceNumber).putIfAbsent(plotNo, new HashMap<>());
						final String observationUnitVariableValue = row.getVariables().get(observationUnitVariableName).getValue();
						obsUnitIdMap.get(instanceNumber).get(plotNo).put(observationUnitVariableValue, row.getObsUnitId());
					}
				}

				final int plotNoIndex = headers.indexOf(StudyBookTableBuilder.PLOT_NO);
				final int observationUnitVariableIndex = headers.indexOf(observationUnitVariableName);
				headers.add(StudyBookTableBuilder.OBS_UNIT_ID);

				for(final List<String> row: values) {
					final String trialInstanceValue = row.get(trialInstanceIndex);
					final String plotNo = row.get(plotNoIndex);
					final String observationUnitVariableValue = row.get(observationUnitVariableIndex);
					row.add(obsUnitIdMap.get(trialInstanceValue).get(plotNo).get(observationUnitVariableValue));
				}
			}
		}
	}

	private Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> getEnvironmentObservationUnitRows(
			final List<MeasurementVariable> environmentVariables, final Integer studyId, final Integer datasetId) {
		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
		final List<MeasurementVariableDto> environmentDetails = new ArrayList<>();
		final List<MeasurementVariableDto> environmentConditions = new ArrayList<>();
		for (final MeasurementVariable variable: environmentVariables) {
			if (VariableType.ENVIRONMENT_DETAIL.getId().equals(variable.getVariableType().getId())) {
				environmentDetails.add(new MeasurementVariableDto(variable.getTermId(), variable.getName()));
			} else if (VariableType.ENVIRONMENT_CONDITION.getId().equals(variable.getVariableType().getId())) {
				environmentConditions.add(new MeasurementVariableDto(variable.getTermId(), variable.getName()));
			}
		}
		searchDTO.setEnvironmentDetails(environmentDetails);
		searchDTO.setEnvironmentConditions(environmentConditions);
		searchDTO.setEnvironmentDatasetId(datasetId);
		final PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE);
		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> rows = this.middlewareDatasetService
			.getObservationUnitRows(studyId, datasetId, searchDTO, pageRequest);
		return rows.stream().collect(Collectors.toMap(row -> String.valueOf(row.getTrialInstance()), Function.identity()));
	}

	static ObservationsPutRequestInput transformObservations(
		final List<ObservationDTO> observations,
		final List<MeasurementVariable> datasetMeasurementVariables) {

		final Map<Integer, MeasurementVariable> varMap =
			datasetMeasurementVariables.stream().collect(Collectors.toMap(MeasurementVariable::getTermId, Function.identity()));
		final Set<Integer> variableIds =
			new TreeSet<>(observations.stream().map(ObservationDTO::getObservationVariableDbId).collect(Collectors.toSet()));
		final List<String> variableNames =
			variableIds.stream().map(termId -> varMap.get(termId).getAlias()).collect(Collectors.toList());

		/* tree -> {
		 *     obsUnit1 -> {
		 *         var1 -> [obs1],
		 *         var2 -> [obs2]
		 *         var3 -> null,
		 *     },
		 *     obsUnit2 -> {
		 *         var1 -> null,
		 *         var2 -> [obs3],
		 *         var3 -> [obs4],
		 *     }
		 * }
		 */
		final Map<String, Map<Integer, List<ObservationDTO>>> tree = observations.stream().collect(Collectors
			.groupingBy(ObservationDTO::getObservationUnitDbId,
				LinkedHashMap::new,
				Collectors.groupingBy(ObservationDTO::getObservationVariableDbId,
					LinkedHashMap::new,
					Collectors.toList())));

		final ObservationsPutRequestInput input = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = new ArrayList<>();
		headers.add("OBS_UNIT_ID");
		headers.addAll(variableNames);
		data.add(headers);
		for (final Map.Entry<String, Map<Integer, List<ObservationDTO>>> obsUnit : tree.entrySet()) {
			final List<String> row = new ArrayList<>();
			row.add(obsUnit.getKey());
			for (final Integer variableId : variableIds) {
				final List<ObservationDTO> dtos = obsUnit.getValue().get(variableId);
				String value = dtos != null ? dtos.get(0).getValue() : "";

				// NA (Not Available) in statistics is synonymous to "missing" value in BMS.
				// So we need to convert NA value to "missing"
				if (NOT_AVAILABLE_VALUE.equals(value)) {
					// Only categorical and numeric type variables support "missing" value,
					// so for other data types, NA (Not Available) should be treated as empty string.
					value = isNumericOrCategorical(varMap.get(variableId)) ? MISSING_VALUE : StringUtils.EMPTY;
				}
				row.add(value);
			}
			data.add(row);
		}
		input.setData(data);
		return input;
	}

	private static boolean isNumericOrCategorical(final MeasurementVariable measurementVariable) {
		return measurementVariable.getDataTypeId().equals(DataType.NUMERIC_VARIABLE.getId())
			|| measurementVariable.getDataTypeId().equals(DataType.CATEGORICAL_VARIABLE.getId());
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final Integer projectId, final List<Integer> variableTypes) {
		return this.middlewareDatasetService.getObservationSetVariables(projectId, variableTypes);
	}

	@Override
	public Boolean hasDatasetDraftDataOutOfBounds(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
		return this.middlewareDatasetService.hasDatasetDraftDataOutOfBounds(datasetId);
	}

	@Override
	public void acceptDraftDataAndSetOutOfBoundsToMissing(final Integer studyId, final Integer datasetId, final Set<Integer> instanceIds) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
		if (!CollectionUtils.isEmpty(instanceIds)) {
			this.instanceValidator.validate(datasetId, instanceIds);
		}
		this.middlewareDatasetService.acceptDraftDataAndSetOutOfBoundsToMissing(studyId, datasetId, instanceIds);
	}

	@Override
	public void acceptDraftDataFilteredByVariable(
		final Integer studyId, final Integer datasetId,
		final ObservationUnitsSearchDTO searchDTO) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator
			.validateExistingDatasetVariables(studyId, datasetId, Lists.newArrayList(searchDTO.getFilter().getVariableId()));
		this.middlewareDatasetService.acceptDraftDataFilteredByVariable(datasetId, searchDTO, studyId);
	}

	@Override
	public void setValueToVariable(
		final Integer studyId, final Integer datasetId, final ObservationUnitsParamDTO paramDTO) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		final Integer variableId = paramDTO.getObservationUnitsSearchDTO().getFilter().getVariableId();
		this.datasetValidator
			.validateExistingDatasetVariables(
				studyId, datasetId, Lists.newArrayList(variableId));
		this.observationValidator.validateVariableValue(variableId, paramDTO.getNewValue());
		this.middlewareDatasetService.setValueToVariable(datasetId, paramDTO, studyId);
	}

	@Override
	public void deleteVariableValues(
		final Integer studyId, final Integer datasetId, final ObservationUnitsSearchDTO searchDTO) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		final Integer variableId = searchDTO.getFilter().getVariableId();
		this.datasetValidator
			.validateExistingDatasetVariables(
				studyId, datasetId, Lists.newArrayList(variableId));
		this.middlewareDatasetService.deleteVariableValues(studyId, datasetId, searchDTO);
	}

	@Override
	public void acceptDatasetDraftData(final Integer studyId, final Integer datasetId, final Set<Integer> instanceIds) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
		if (!CollectionUtils.isEmpty(instanceIds)) {
			this.instanceValidator.validate(datasetId, instanceIds);
		}
		this.middlewareDatasetService.acceptDatasetDraftData(studyId, datasetId, instanceIds);
	}

	@Override
	public void rejectDatasetDraftData(final Integer studyId, final Integer datasetId, final Set<Integer> instanceIds) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
		if (!CollectionUtils.isEmpty(instanceIds)) {
			this.instanceValidator.validate(datasetId, instanceIds);
		}
		this.middlewareDatasetService.rejectDatasetDraftData(datasetId, instanceIds);
	}

	@Override
	public FilteredPhenotypesInstancesCountDTO countFilteredInstancesAndObservationUnits(
		final Integer studyId,
		final Integer datasetId, final ObservationUnitsSearchDTO observationUnitsSearchDTO) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId,
			Lists.newArrayList(observationUnitsSearchDTO.getFilter().getVariableId()));
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId,
			Lists.newArrayList(observationUnitsSearchDTO.getFilter().getVariableId()));
		return this.middlewareDatasetService.countFilteredInstancesAndObservationUnits(datasetId, observationUnitsSearchDTO);
	}

	private BindingResult processObservationsDataWarningsAsErrors(
		final Table<String, String, String> table,
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData,
		final Integer rowsNotBelongingToDataset, final Integer duplicatedFoundNumber, final Boolean draftMode) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), ObservationsPutRequestInput.class.getName());
		if (duplicatedFoundNumber > 0) {
			errors.reject("duplicated.obs.unit.id", null, "");
		}

		if (rowsNotBelongingToDataset != 0) {
			errors.reject("some.obs.unit.id.matches", new String[] {String.valueOf(rowsNotBelongingToDataset)}, "");
		}

		if (this.isInputOverwritingData(table, storedData, draftMode)) {
			errors.reject("warning.import.overwrite.data", null, "");
		}

		return errors;
	}

	private Boolean isInputOverwritingData(
		final Table<String, String, String> table,
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData, final Boolean draftMode) {
		boolean overwritingData = false;

		externalLoop:
		for (final String observationUnitId : table.rowKeySet()) {

			final org.generationcp.middleware.service.api.dataset.ObservationUnitRow storedObservations = storedData.get(observationUnitId);

			for (final String variableName : table.columnKeySet()) {

				final org.generationcp.middleware.service.api.dataset.ObservationUnitData observation =
					storedObservations.getVariables().get(variableName);

				if (observation == null) {
					continue;
				}

				if ((!draftMode && observation.getValue() != null //
					&& !observation.getValue().equalsIgnoreCase(table.get(observationUnitId, variableName))) //
					|| (draftMode && observation.getDraftValue() != null //
					&& !observation.getDraftValue().equalsIgnoreCase(table.get(observationUnitId, variableName)))) {

					overwritingData = true;

					break externalLoop;
				}
			}

		}
		return overwritingData;
	}

	private void mapObservationUnitRows(
		final ModelMapper observationUnitRowMapper,
		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows,
		final List<ObservationUnitRow> list) {
		for (final org.generationcp.middleware.service.api.dataset.ObservationUnitRow dto : observationUnitRows) {
			final Map<String, ObservationUnitData> variables = new HashMap<>();
			final Map<String, ObservationUnitData> environmentVariables = new HashMap<>();
			for (final String data : dto.getVariables().keySet()) {
				variables.put(data, observationUnitRowMapper.map(dto.getVariables().get(data), ObservationUnitData.class));
			}
			for (final String data : dto.getEnvironmentVariables().keySet()) {
				environmentVariables
					.put(data, observationUnitRowMapper.map(dto.getEnvironmentVariables().get(data), ObservationUnitData.class));
			}
			final ObservationUnitRow observationUnitRow = observationUnitRowMapper.map(dto, ObservationUnitRow.class);
			observationUnitRow.setVariables(variables);
			observationUnitRow.setEnvironmentVariables(environmentVariables);
			list.add(observationUnitRow);
		}
	}

	@Override
	public List<MeasurementVariable> getAllDatasetVariables(final int studyId, final int datasetId) {

		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(datasetId);
		final List<Integer> subObsDatasetTypeIds = this.datasetTypeService.getSubObservationDatasetTypeIds();
		final int environmentDatasetId =
			this.studyDataManager.getDataSetsByType(studyId, DatasetTypeEnum.SUMMARY_DATA.getId()).get(0).getId();
		final int plotDatasetId;

		if (datasetDTO.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			plotDatasetId = datasetDTO.getDatasetId();
		} else {
			plotDatasetId = datasetDTO.getParentDatasetId();
		}

		final List<MeasurementVariable> studyVariables = this.middlewareDatasetService
			.getObservationSetVariables(studyId, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));
		final List<MeasurementVariable> environmentDetailAndConditionVariables = this.middlewareDatasetService
			.getObservationSetVariables(environmentDatasetId, Lists.newArrayList(
				VariableType.ENVIRONMENT_DETAIL.getId(),
				VariableType.ENVIRONMENT_CONDITION.getId()));
		this.addLocationVariables(environmentDetailAndConditionVariables);
		// Experimental Design variables have value at dataset level. Perform sorting to ensure that they come first
		Collections.sort(environmentDetailAndConditionVariables, (var1, var2) -> {
			final String value1 = var1.getValue();
			final String value2 = var2.getValue();
			if (value1 != null && value2 != null) {
				return value1.compareTo(value2);
			} else if (value1 == null && value2 == null) {
				return 0;
			} else {
				return (value1 == null) ? 1 : -1;
			}
		});

		final List<MeasurementVariable> plotDataSetColumns =
			this.middlewareDatasetService
				.getObservationSetVariables(
					plotDatasetId,
					Lists.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.TREATMENT_FACTOR.getId(), VariableType.OBSERVATION_UNIT.getId(), VariableType.ENTRY_DETAIL.getId(),
						VariableType.GERMPLASM_ATTRIBUTE.getId(), VariableType.GERMPLASM_PASSPORT.getId()));
		final List<MeasurementVariable> treatmentFactors =
			this.middlewareDatasetService
				.getObservationSetVariables(plotDatasetId, Lists.newArrayList(TermId.MULTIFACTORIAL_INFO.getId()));
		plotDataSetColumns.removeAll(treatmentFactors);

		final List<MeasurementVariable> traits =
			this.middlewareDatasetService.getObservationSetVariables(datasetId, Lists.newArrayList(VariableType.TRAIT.getId()));
		final List<MeasurementVariable> selectionVariables =
			this.middlewareDatasetService.getObservationSetVariables(datasetId, Lists.newArrayList(VariableType.SELECTION_METHOD.getId()));
		final List<MeasurementVariable> allVariables = new ArrayList<>();
		allVariables.addAll(studyVariables);
		allVariables.addAll(environmentDetailAndConditionVariables);
		allVariables.addAll(treatmentFactors);
		allVariables.addAll(plotDataSetColumns);

		//Add variables that are specific to the sub-observation dataset types
		if (Arrays.stream(subObsDatasetTypeIds.toArray()).anyMatch(datasetDTO.getDatasetTypeId()::equals)) {
			final List<MeasurementVariable> subObservationSetColumns =
				this.middlewareDatasetService
					.getObservationSetVariables(datasetId, Lists.newArrayList(
						VariableType.GERMPLASM_DESCRIPTOR.getId(),
						VariableType.OBSERVATION_UNIT.getId()));
			allVariables.addAll(subObservationSetColumns);

		}
		allVariables.addAll(traits);
		allVariables.addAll(selectionVariables);
		return allVariables;
	}

	void addLocationVariables(final List<MeasurementVariable> environmentDetailAndConditionVariables) {
		// check if LOCATION_ABBR already exists in the study, add if not present
		final OptionalInt indexOfLocationAbbr = IntStream.range(0, environmentDetailAndConditionVariables.size())
			.filter(i -> environmentDetailAndConditionVariables.get(i).getTermId() == TermId.LOCATION_ABBR.getId())
			.findFirst();

		if (!indexOfLocationAbbr.isPresent()) {
			final MeasurementVariable locationAbbrVariable = new MeasurementVariable();
			locationAbbrVariable.setAlias(TermId.LOCATION_ABBR.name());
			locationAbbrVariable.setName(LOCATION_ABBR_VARIABLE_NAME);
			environmentDetailAndConditionVariables.add(0, locationAbbrVariable);
		}

		final MeasurementVariable locationIdVariable = new MeasurementVariable();
		locationIdVariable.setAlias(TermId.LOCATION_ID.name());
		locationIdVariable.setName(LOCATION_ID_VARIABLE_NAME);
		environmentDetailAndConditionVariables.add(0, locationIdVariable);
	}

	@Override
	public void replaceObservationUnitsEntry(final int studyId, final int datasetId, final ObservationUnitEntryReplaceRequest request) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validatePlotDatasetType(datasetId);
		this.studyValidator.validateStudyHasNoMeansDataset(studyId);

		BaseValidator.checkNotNull(request, PARAM_NULL, new String[] {"request"});
		BaseValidator.checkNotNull(request.getSearchRequest(), PARAM_NULL, new String[] {"searchRequest"});
		BaseValidator.checkNotNull(request.getEntryId(), PARAM_NULL, new String[] {"entryId"});

		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		this.searchCompositeDtoValidator.validateSearchCompositeDto(request.getSearchRequest(), errors);

		this.studyEntryValidator.validateStudyContainsEntries(studyId, Collections.singletonList(request.getEntryId()));

		this.studyValidator.validateHasNoCrossesOrSelections(studyId);

		this.processSearchComposite(request.getSearchRequest());

		final ObservationUnitsSearchDTO observationUnitsSearchDTO = request.getSearchRequest().getSearchRequest();

		// Add the required observation table columns necessary for this function
		final Map<Integer, String> requiredColumns =
			this.ontologyDataManager.getTermsByIds(Lists.newArrayList(TermId.TRIAL_INSTANCE_FACTOR.getId(),
				TermId.OBS_UNIT_ID.getId(), TermId.GID.getId())).stream().collect(Collectors.toMap(Term::getId, Term::getName));
		observationUnitsSearchDTO.setVisibleColumns(new HashSet<>(requiredColumns.values()));

		// observation units
		final List<ObservationUnitRow> observationUnitRows =
			this.getObservationUnitRows(studyId, datasetId, observationUnitsSearchDTO, null);
		if (observationUnitRows.isEmpty()) {
			errors.reject("study.entry.replace.empty.units", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (observationUnitRows.stream().anyMatch(o -> !o.getSamplesCount().equals("-"))) {
			errors.reject("study.entry.replace.samples.found", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<Integer> observationUnitIds = observationUnitRows.stream().map(ObservationUnitRow::getObservationUnitId).collect(
			Collectors.toList());
		final StudyTransactionsRequest studyTransactionsRequest = new StudyTransactionsRequest();
		final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
		transactionsSearchDto.setTransactionStatus(
			Lists.newArrayList(TransactionStatus.PENDING.getIntValue(), TransactionStatus.CONFIRMED.getIntValue()));
		studyTransactionsRequest.setTransactionsSearch(transactionsSearchDto);
		studyTransactionsRequest.setObservationUnitIds(observationUnitIds);

		if (this.studyTransactionsService.countStudyTransactions(studyId, studyTransactionsRequest) > 0) {
			errors.reject("study.entry.replace.transactions.found", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.middlewareDatasetService.replaceObservationUnitEntry(observationUnitIds, request.getEntryId());
	}

	@Override
	public ObservationUnitsMetadata getObservationUnitsMetadata(final int studyId, final int datasetId,
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> request) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		BaseValidator.checkNotNull(request, PARAM_NULL, new String[] {"request"});

		final BindingResult errors = new MapBindingResult(new HashMap<>(), SearchCompositeDto.class.getName());
		this.searchCompositeDtoValidator.validateSearchCompositeDto(request, errors);

		this.processSearchComposite(request);

		final ObservationUnitsSearchDTO observationUnitsSearchDTO = request.getSearchRequest();

		// Add the required observation table columns necessary for this function
		final Map<Integer, String> requiredColumns =
			this.ontologyDataManager.getTermsByIds(Lists.newArrayList(TermId.TRIAL_INSTANCE_FACTOR.getId(), TermId.GID.getId())).stream()
				.collect(Collectors.toMap(Term::getId, Term::getName));
		observationUnitsSearchDTO.setVisibleColumns(new HashSet<>(requiredColumns.values()));

		final List<ObservationUnitRow> observationUnitRows =
			this.getObservationUnitRows(studyId, datasetId, observationUnitsSearchDTO, null);

		final ObservationUnitsMetadata observationUnitsMetadata = new ObservationUnitsMetadata();
		observationUnitsMetadata.setObservationUnitsCount(Long.valueOf(observationUnitRows.size()));
		observationUnitsMetadata.setInstancesCount(
			observationUnitRows.stream().map(ObservationUnitRow::getTrialInstance).distinct().count());
		return observationUnitsMetadata;
	}

	@Override
	public Long countObservationUnits(final Integer datasetId) {
		return this.middlewareDatasetService.countObservationUnits(datasetId);

	}

	@Override
	public void updatePlotDatasetProperties(final Integer studyId, final PlotDatasetPropertiesDTO plotDatasetPropertiesDTO,
		final String programUUID) {
		this.studyValidator.validate(studyId, true);

		if (!CollectionUtils.isEmpty(plotDatasetPropertiesDTO.getVariableIds())) {
			this.variableValidator.validate(new HashSet<>(plotDatasetPropertiesDTO.getVariableIds()));
		}

		if (!CollectionUtils.isEmpty(plotDatasetPropertiesDTO.getNameTypeIds())) {
			this.germplasmNameTypeValidator.validate(new HashSet<>(plotDatasetPropertiesDTO.getNameTypeIds()));
		}
		this.studyValidator.validateUpdateStudyEntryColumnsWithSupportedVariableTypes(plotDatasetPropertiesDTO.getVariableIds(),
			programUUID);

		// Add the Pedigree related variables existing in the dataset when user has no VIEW_PEDIGREE_INFORMATION_PERMISSIONS
		if (!SecurityUtil.hasAnyAuthority(PermissionsEnum.VIEW_PEDIGREE_INFORMATION_PERMISSIONS)) {
			final org.generationcp.middleware.domain.dms.DatasetDTO plotDataset = this.middlewareDatasetService
					.getDatasetsWithVariables(studyId, Collections.singleton(DatasetTypeEnum.PLOT_DATA.getId())).get(0);
			final List<Integer> pedigreeRelatedVariables = plotDataset.getVariables().
					stream().filter(variable -> StudyEntryServiceImpl.PEDIGREE_RELATED_COLUMN_IDS.contains(variable.getTermId()))
					.map(MeasurementVariable::getTermId).collect(Collectors.toList());
			plotDatasetPropertiesDTO.getVariableIds().addAll(pedigreeRelatedVariables);
		}
		this.studyValidator.validateMaxStudyEntryColumnsAllowed(plotDatasetPropertiesDTO, programUUID);
		this.middlewareDatasetService.updatePlotDatasetProperties(studyId, plotDatasetPropertiesDTO, programUUID);
	}

	@Override
	public List<GermplasmNameTypeDTO> getAllPlotDatasetNameTypes(final Integer datasetId) {
		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(datasetId);
		final int plotDatasetId;
		if (datasetDTO.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			plotDatasetId = datasetDTO.getDatasetId();
		} else {
			plotDatasetId = datasetDTO.getParentDatasetId();
		}
		return this.middlewareDatasetService.getDatasetNameTypes(plotDatasetId);
	}

	@Override
	public List<MeasurementVariable> getVariablesByVariableTypes(final Integer studyId, final List<Integer> variableTypes) {
		this.studyValidator.validate(studyId, false);
		variableTypes.forEach(this.datasetValidator::validateVariableType);
		return this.middlewareDatasetService.getDatasetMeasurementVariablesByVariableType(studyId, variableTypes);
	}

	private void processSearchComposite(final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchDTO) {
		if (searchDTO.getItemIds() != null && !searchDTO.getItemIds().isEmpty()) {
			final ObservationUnitsSearchDTO searchRequest = new ObservationUnitsSearchDTO();
			final ObservationUnitsSearchDTO.Filter filter = searchRequest.new Filter();
			filter.setFilteredNdExperimentIds(searchDTO.getItemIds());
			searchRequest.setFilter(filter);
			searchDTO.setSearchRequest(searchRequest);
		}
	}

	private void correctKSUDateFormatIfNecessary(final Table<String, String, String> table,
		final List<MeasurementVariable> measurementVariables) {
		final List<String> dateVariables = measurementVariables.stream().filter(
				measurementVariable -> measurementVariable.getDataTypeId() != null
					&& measurementVariable.getDataTypeId() == TermId.DATE_VARIABLE.getId())
			.map(MeasurementVariable::getName).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(dateVariables)) {
			for (final String colVariable : table.columnKeySet()) {
				if (dateVariables.contains(colVariable)) {
					for (final String obsUnit : table.rowKeySet()) {
						String value = table.get(obsUnit, colVariable);
						final Date ksuParsed = Util.tryParseDate(value, Util.DATE_AS_NUMBER_FORMAT_KSU);
						if (ksuParsed != null) {
							value = Util.formatDateAsStringValue(ksuParsed, Util.DATE_AS_NUMBER_FORMAT);
							table.put(obsUnit, colVariable, value);
						}
					}
				}
			}
		}
	}

	private Optional<MeasurementVariable> getObservationUnitVariable(final List<MeasurementVariable> columns) {
		return columns.stream().filter(variable -> variable.getVariableType().getId() == TermId.OBSERVATION_UNIT.getId())
				.findFirst();
	}

	private List<MeasurementVariable> removePedigreeRelatedVariablesIfNecessary(final List<MeasurementVariable> measurementVariables) {
		if (!SecurityUtil.hasAnyAuthority(PermissionsEnum.VIEW_PEDIGREE_INFORMATION_PERMISSIONS)) {
			return measurementVariables.stream().filter(variable -> !StudyEntryServiceImpl.PEDIGREE_RELATED_COLUMN_IDS.contains(variable.getTermId()))
					.collect(Collectors.toList());
		}
		return measurementVariables;
	}
}
