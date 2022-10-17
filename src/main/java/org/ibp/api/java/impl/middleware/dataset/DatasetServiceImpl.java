package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.api.brapi.v1.observation.ObservationDTO;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.dataset.ObservationDto;
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
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationsTableValidator;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.java.impl.middleware.name.validator.GermplasmNameTypeValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.study.ObservationUnitsMetadata;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.generationcp.middleware.domain.dataset.PlotDatasetPropertiesDTO;
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
	private ObservationsTableValidator observationsTableValidator;

	@Autowired
	private DatasetTypeService datasetTypeService;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private StudyTransactionsService studyTransactionsService;

	@Autowired
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private GermplasmNameTypeValidator germplasmNameTypeValidator;

	static final String PLOT_DATASET_NAME = "Observations";

	@Override
	public List<MeasurementVariable> getObservationSetColumns(
		final Integer studyId, final Integer datasetId, final Boolean draftMode) {

		this.studyValidator.validate(studyId, false);

		this.datasetValidator.validateDataset(studyId, datasetId);

		return this.middlewareDatasetService.getObservationSetColumns(studyId, datasetId, draftMode);
	}

	@Override
	public List<MeasurementVariable> getSubObservationSetVariables(
		final Integer studyId, final Integer datasetId) {

		this.studyValidator.validate(studyId, false);

		this.datasetValidator.validateDataset(studyId, datasetId);

		return this.middlewareDatasetService.getObservationSetVariables(datasetId);
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
	public MeasurementVariable addDatasetVariable(final Integer studyId, final Integer datasetId, final DatasetVariable datasetVariable) {
		this.studyValidator.validate(studyId, true);

		final Integer variableId = datasetVariable.getVariableId();
		final StandardVariable traitVariable =
			this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, false);

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
		final List<DatasetVariable> datasetVariables) {
		this.studyValidator.validate(studyId, true);

		datasetVariables.forEach(datasetVariable -> {
			final StandardVariable traitVariable =
				this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, false);

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
	public void removeDatasetVariables(final Integer studyId, final Integer datasetId, final List<Integer> variableIds) {
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
			this.middlewareDatasetService.getDatasets(studyId, datasetTypeIdList);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<DatasetDTO> datasetDTOs = new ArrayList<>();
		for (final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO : datasetDTOS) {
			final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
			if (datasetDto.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
				datasetDto.setName(PLOT_DATASET_NAME);
			}
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
		final Integer datasetId, final Integer instanceId, final Boolean draftMode) {
		return this.middlewareDatasetService.countAllObservationUnitsForDataset(datasetId, instanceId, draftMode);
	}

	@Override
	public long countFilteredObservationUnitsForDataset(
		final Integer datasetId, final Integer instanceId, final Boolean draftMode,
		final ObservationUnitsSearchDTO.Filter filter) {
		return this.middlewareDatasetService.countFilteredObservationUnitsForDataset(datasetId, instanceId, draftMode, filter);
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
		if (pageable != null && pageable.getSort() != null) {
			final Iterator<Sort.Order> iterator = pageable.getSort().iterator();
			if (iterator.hasNext()) {
				// Convert the sort property name from termid to actual term name.
				final Sort.Order sort = iterator.next();
				final String sortProperty;
				if (NumberUtils.isNumber(sort.getProperty()) && Integer.valueOf(sort.getProperty()) > 0) {
					final Term term = this.ontologyDataManager.getTermById(Integer.valueOf(sort.getProperty()));
					if (null == term) {
						sortProperty = String.format("NAME_%s", sort.getProperty());
					} else {
						sortProperty = term.getName();
					}
				} else {
					sortProperty = sort.getProperty();
				}
				pageable.getSort().and(new Sort(sort.getDirection(), sortProperty));
				convertedPageable =
					new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort.getDirection(), sortProperty);
			}
		}

		List<Integer> instanceIds = null;
		if (searchDTO.getInstanceId() != null) {
			instanceIds = Arrays.asList(searchDTO.getInstanceId());
		}
		this.validateStudyDatasetAndInstances(studyId, datasetId, instanceIds);

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

		List<Integer> instanceIds = null;
		if (searchDTO.getInstanceId() != null) {
			instanceIds = Arrays.asList(searchDTO.getInstanceId());
		}
		this.validateStudyDatasetAndInstances(studyId, datasetId, instanceIds);

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

		this.observationsTableValidator.validateList(input.getData());

		final List<MeasurementVariable> datasetMeasurementVariables =
			this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId);

		if (datasetMeasurementVariables.isEmpty()) {
			errors.reject("no.variables.dataset", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final ObservationUnitsTableBuilder observationUnitsTableBuilder = new ObservationUnitsTableBuilder();
		final Table<String, String, String> table = observationUnitsTableBuilder.build(input.getData(), datasetMeasurementVariables);

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
		this.observationsTableValidator.validateObservationsValuesDataTypes(table, datasetMeasurementVariables);

		// Processing warnings
		if (input.isProcessWarnings()) {
			errors = this.processObservationsDataWarningsAsErrors(table, storedData, rowsNotBelongingToDataset,
				observationUnitsTableBuilder.getDuplicatedFoundNumber(), input.isDraftMode());
		}
		if (!errors.hasErrors()) {
			this.middlewareDatasetService.importDataset(datasetId, table, input.isDraftMode(), false);
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

		this.observationsTableValidator.validateList(input.getData());

		final ObservationUnitsTableBuilder observationUnitsTableBuilder = new ObservationUnitsTableBuilder();
		final Table<String, String, String> table = observationUnitsTableBuilder.build(input.getData(), datasetMeasurementVariables);

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
		this.observationsTableValidator.validateObservationsValuesDataTypes(table, datasetMeasurementVariables);

		// Processing warnings
		if (input.isProcessWarnings()) {
			errors = this.processObservationsDataWarningsAsErrors(table, storedData, rowsNotBelongingToDataset,
				observationUnitsTableBuilder.getDuplicatedFoundNumber(), input.isDraftMode());
		}
		if (!errors.hasErrors()) {
			final Table<String, Integer, Integer> observationDbIdsTable = this.middlewareDatasetService.importDataset(datasetId, table, input.isDraftMode(), true);
			// We need to return the observationDbIds (mapped in a table by observationUnitId and variableId) of the created/updated observations.
			observations.stream().forEach(
				o -> o.setObservationDbId(observationDbIdsTable.get(o.getObservationUnitDbId(), o.getObservationVariableDbId())));
		} else {
			throw new PreconditionFailedException(errors.getAllErrors());
		}

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
	public void acceptDraftDataAndSetOutOfBoundsToMissing(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
		this.middlewareDatasetService.acceptDraftDataAndSetOutOfBoundsToMissing(studyId, datasetId);
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
	public void acceptAllDatasetDraftData(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
		this.middlewareDatasetService.acceptAllDatasetDraftData(studyId, datasetId);
	}

	@Override
	public void rejectDatasetDraftData(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
		this.middlewareDatasetService.rejectDatasetDraftData(datasetId);
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

		// observation units
		final List<ObservationUnitRow> observationUnitRows =
			this.getObservationUnitRows(studyId, datasetId, request.getSearchRequest().getSearchRequest(),null);
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
		transactionsSearchDto.setTransactionStatus(Lists.newArrayList(TransactionStatus.PENDING.getIntValue(),TransactionStatus.CONFIRMED.getIntValue()));
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

		final List<ObservationUnitRow> observationUnitRows =
			this.getObservationUnitRows(studyId, datasetId, request.getSearchRequest(),null);

		final ObservationUnitsMetadata observationUnitsMetadata = new ObservationUnitsMetadata();
		observationUnitsMetadata.setObservationUnitsCount(Long.valueOf(observationUnitRows.size()));
		observationUnitsMetadata.setInstancesCount(observationUnitRows.stream().map(ObservationUnitRow::getTrialInstance).distinct().count());
		return observationUnitsMetadata;
	}

	@Override
	public Long countObservationUnits(final Integer datasetId) {
		return this.middlewareDatasetService.countObservationUnits(datasetId);

	}

	@Override
	public void updatePlotDatasetProperties(final Integer studyId, final PlotDatasetPropertiesDTO plotDatasetPropertiesDTO, final String programUUID) {
		this.studyValidator.validate(studyId, true);
		plotDatasetPropertiesDTO.getVariableIds().forEach(this.termValidator::validate);
		plotDatasetPropertiesDTO.getNameTypeIds().forEach(this.germplasmNameTypeValidator::validate);
		this.studyValidator.validateUpdateStudyEntryColumnsWithSupportedVariableTypes(plotDatasetPropertiesDTO.getVariableIds(), programUUID);
		// TODO: Is needed to include NameTypes here?
		this.studyValidator.validateMaxStudyEntryColumnsAllowed(plotDatasetPropertiesDTO.getVariableIds(), programUUID);

		this.middlewareDatasetService.updatePlotDatasetProperties(studyId, plotDatasetPropertiesDTO, programUUID);
	}

	@Override
	public List<GermplasmNameTypeDTO> getAllPlotDatasetNames(final Integer datasetId) {
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
	public void deleteNameTypeFromStudies(final Integer nameTypeId) {
		this.germplasmNameTypeValidator.validate(nameTypeId);
		this.middlewareDatasetService.deleteNameTypeFromStudies(nameTypeId);
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
}
