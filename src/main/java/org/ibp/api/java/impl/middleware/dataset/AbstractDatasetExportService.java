package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractDatasetExportService {

	static final String XLS = "xls";
	static final String CSV = "csv";

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	protected DatasetService studyDatasetService;

	@Autowired
	protected DatasetCollectionOrderService datasetCollectionOrderService;

	@Autowired
	protected OntologyDataManager ontologyDataManager;

	@Resource
	protected org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Resource
	protected StudyDataManager studyDataManager;

	@Resource
	protected DatasetTypeService datasetTypeService;

	@Autowired
	private TransactionService transactionService;

	private ZipUtil zipUtil = new ZipUtil();

	protected void validate(final int studyId, final int datasetId, final Set<Integer> instanceIds) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.instanceValidator.validate(datasetId, instanceIds);
	}

	File generate(
		final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId,
		final DatasetFileGenerator generator, final boolean singleFile, final String fileExtension) throws IOException {

		final Study study = this.studyDataManager.getStudy(studyId);
		final DatasetDTO dataSet = this.datasetService.getDataset(datasetId);

		// Get all variables for the dataset
		final List<MeasurementVariable> columns = this.getColumns(study.getId(), dataSet.getDatasetId());
		if (dataSet.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
			transactionsSearchDto.setTransactionStatus(Arrays.asList(0,1));
			transactionsSearchDto.setPlantingStudyIds(Arrays.asList(studyId));
			if (this.transactionService.countSearchTransactions(transactionsSearchDto) > 0) {
				this.addStockIdColumn(columns);
			}
		}

		final Map<Integer, StudyInstance> selectedDatasetInstancesMap = this.getSelectedDatasetInstancesMap(
			dataSet.getInstances(),
			instanceIds);
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap =
			this.getObservationUnitRowMap(study, dataSet, selectedDatasetInstancesMap);
		final DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder =
			DatasetCollectionOrderServiceImpl.CollectionOrder.findById(collectionOrderId);
		final int trialDatasetId = this.studyDataManager.getDataSetsByType(study.getId(), DatasetTypeEnum.SUMMARY_DATA.getId()).get(0).getId();
		this.datasetCollectionOrderService.reorder(collectionOrder, trialDatasetId, selectedDatasetInstancesMap, observationUnitRowMap);

		if (singleFile) {
			return this.generateInSingleFile(study, dataSet, observationUnitRowMap, columns, generator, fileExtension);
		} else {
			return this
				.generateFiles(study, dataSet, selectedDatasetInstancesMap, observationUnitRowMap, columns, generator, fileExtension);
		}

	}

	File generateInSingleFile(
		final Study study,
		final DatasetDTO dataSet, final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap,
		final List<MeasurementVariable> columns,
		final DatasetFileGenerator generator, final String fileExtension)
		throws IOException {

		final File temporaryFolder = Files.createTempDir();
		final String dataSetName =
			DatasetTypeEnum.PLOT_DATA.getId() == dataSet.getDatasetTypeId() ? DatasetServiceImpl.PLOT_DATASET_NAME : dataSet.getName();
		final String fileName = FileNameGenerator.generateFileName(String.format("%s_%s", StringUtil.truncate(study.getName(), 45, true), StringUtil.truncate(dataSetName, 45, true)), fileExtension);
		final String sanitizedFileName = FileUtils.sanitizeFileName(fileName);
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

		return generator.generateMultiInstanceFile(observationUnitRowMap, columns, fileNameFullPath);
	}

	File generateFiles(
		final Study study, final DatasetDTO dataSetDto,
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap,
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns,
		final DatasetFileGenerator generator, final String fileExtension)
		throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final List<File> files =
			this.getInstanceFiles(study, dataSetDto, selectedDatasetInstancesMap, observationUnitRowMap, columns, generator, fileExtension,
				temporaryFolder);
		return this.getReturnFile(study, files);
	}

	File getReturnFile(final Study study, final List<File> files) throws IOException {
		if (files.size() == 1) {
			return files.get(0);
		} else {
			return this.zipUtil.zipFiles(FileNameGenerator.generateFileName(study.getName(), null), files);
		}
	}

	List<File> getInstanceFiles(
		final Study study, final DatasetDTO dataSetDto, final Map<Integer, StudyInstance> selectedDatasetInstancesMap,
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns,
		final DatasetFileGenerator generator, final String fileExtension, final File temporaryFolder) throws IOException {
		final List<File> files = new ArrayList<>();

		final Map<Integer, DatasetTypeDTO> datasetTypeMap = this.datasetTypeService.getAllDatasetTypesMap();
		for (final Integer instanceDBID : observationUnitRowMap.keySet()) {
			// Build the filename with the following format:
			// study_name + TRIAL_INSTANCE number + location_abbr +  dataset_type + dataset_name
			final String studyName = StringUtil.truncate(study.getName(), 30, true);
			final String locationAbbr = StringUtil.truncate(selectedDatasetInstancesMap.get(instanceDBID).getLocationAbbreviation(), 10, true);
			final String datasetTypeName = StringUtil.truncate(datasetTypeMap.get(dataSetDto.getDatasetTypeId()).getName(), 10, true);
			final String datasetName = StringUtil.truncate(dataSetDto.getName(), 30, true);
			final String sanitizedFileName = FileUtils.sanitizeFileName(String
				.format(
					"%s_%s_%s_%s",
					studyName + "-" + selectedDatasetInstancesMap.get(instanceDBID).getInstanceNumber(),
					locationAbbr, datasetTypeName, datasetName));
			final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + FileNameGenerator.generateFileName(sanitizedFileName, fileExtension);
			files.add(
				generator.generateSingleInstanceFile(study.getId(), dataSetDto, columns, observationUnitRowMap.get(instanceDBID),
					fileNameFullPath, selectedDatasetInstancesMap.get(instanceDBID)));
		}
		return files;
	}

	Map<Integer, StudyInstance> getSelectedDatasetInstancesMap(final List<StudyInstance> studyInstances, final Set<Integer> instanceIds) {
		final Map<Integer, StudyInstance> studyInstanceMap = new LinkedHashMap<>();
		for (final StudyInstance studyInstance : studyInstances) {
			if (instanceIds.contains(studyInstance.getInstanceId())) {
				studyInstanceMap.put(studyInstance.getInstanceId(), studyInstance);
			}
		}
		return studyInstanceMap;
	}

	List<MeasurementVariable> moveSelectedVariableInTheFirstColumn(final List<MeasurementVariable> columns, final int variableId) {
		int trialInstanceIndex = 0;
		for (final MeasurementVariable column : columns) {
			if (variableId == column.getTermId()) {
				final MeasurementVariable trialInstanceMeasurementVariable = columns.remove(trialInstanceIndex);
				columns.add(0, trialInstanceMeasurementVariable);
				break;
			}
			trialInstanceIndex++;
		}
		return columns;
	}

	protected void addStockIdColumn(final List<MeasurementVariable> plotDataSetColumns) {
		final Optional<MeasurementVariable>
			designationColumn = plotDataSetColumns.stream().filter(measurementVariable ->
			measurementVariable.getTermId() == TermId.DESIG.getId()).findFirst();
		// Set the variable name of this virtual Column to STOCK_ID, to match the stock of planting inventory
		plotDataSetColumns.add(plotDataSetColumns.indexOf(designationColumn.get()) + 1,
			this.addTermIdColumn(TermId.STOCK_ID, VariableType.GERMPLASM_DESCRIPTOR,null, true));
	}

	protected abstract List<MeasurementVariable> getColumns(int studyId, int datasetId);

	protected abstract Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(
		Study study, DatasetDTO dataset, Map<Integer, StudyInstance> selectedDatasetInstancesMap);

	void setZipUtil(final ZipUtil zipUtil) {
		this.zipUtil = zipUtil;
	}

	private MeasurementVariable addTermIdColumn(final TermId TermId, final VariableType VariableType, final String name, final boolean factor) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName(StringUtils.isBlank(name) ? TermId.name() : name);
		measurementVariable.setAlias(TermId.name());
		measurementVariable.setTermId(TermId.getId());
		measurementVariable.setVariableType(VariableType);
		measurementVariable.setFactor(factor);
		return measurementVariable;
	}

}
