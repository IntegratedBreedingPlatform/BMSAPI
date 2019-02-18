package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDatasetExportService {

	public static final String XLS = "xls";
	public static final String CSV = "csv";
	public static String[] TRAIT_FILE_HEADERS = {"trait", "format", "defaultValue", "minimum",
		"maximum", "details", "categories", "isVisible", "realPosition"};

	public static final List<Integer> DATA_TYPE_LIST = Arrays.asList(
		TermId.NUMERIC_VARIABLE.getId(),
		TermId.CATEGORICAL_VARIABLE.getId(), TermId.DATE_VARIABLE.getId(), TermId.CHARACTER_VARIABLE.getId());

	public static final ImmutableMap<Integer, String> DATA_TYPE_FORMATS = ImmutableMap.<Integer, String> builder()
		.put(TermId.CATEGORICAL_VARIABLE.getId(), "categorical").put(TermId.NUMERIC_VARIABLE.getId(), "numeric")
		.put(TermId.DATE_VARIABLE.getId(), "date").put(TermId.CHARACTER_VARIABLE.getId(), "text")
		.put(0, "unrecognized").build();

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
	protected FieldbookService fieldbookService;

	protected ZipUtil zipUtil = new ZipUtil();

	protected void validate(final int studyId, final int datasetId, final Set<Integer> instanceIds) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.instanceValidator.validate(datasetId, instanceIds);
	}

	protected File generate(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final DatasetFileGenerator generator, final boolean singleFile, final String fileExtension) throws  IOException{

		final Study study = this.studyDataManager.getStudy(studyId);
		final DatasetDTO dataSet = this.datasetService.getDataset(datasetId);


		// Get all variables for the dataset
		final List<MeasurementVariable> columns = this.getColumns(study.getId(), dataSet.getDatasetId());
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap = getSelectedDatasetInstancesMap(dataSet.getInstances(),
			instanceIds);
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap =
			this.getObservationUnitRowMap(study, dataSet, selectedDatasetInstancesMap);
		final DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder = DatasetCollectionOrderServiceImpl.CollectionOrder.findById(collectionOrderId);
		final int trialDatasetId = this.studyDataManager.getDataSetsByType(study.getId(), DataSetType.SUMMARY_DATA).get(0).getId();
		this.datasetCollectionOrderService.reorder(collectionOrder, trialDatasetId, selectedDatasetInstancesMap, observationUnitRowMap);

		if(singleFile) {
			return this.generateInSingleFile(study, observationUnitRowMap, columns, generator, fileExtension);
		} else  {
			return this.generateFiles(study, dataSet, selectedDatasetInstancesMap, observationUnitRowMap, columns, generator, fileExtension);
		}

	}

	public File generateInSingleFile(final Study study,
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns, final DatasetFileGenerator generator, final  String fileExtension)
		throws IOException {

		final File temporaryFolder = Files.createTempDir();
		final String sanitizedFileName = FileUtils.sanitizeFileName(String.format("%s_AllInstances." + fileExtension, study.getName()));
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

		return generator.generateMultiInstanceFile (observationUnitRowMap, columns, fileNameFullPath);
	}


	protected File generateFiles(final Study study, final DatasetDTO dataSetDto,
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap,
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns, final DatasetFileGenerator generator, final String fileExtension)
		throws IOException {
		final List<File> files = new ArrayList<>();
		final File temporaryFolder = Files.createTempDir();
		for(final Integer instanceDBID: observationUnitRowMap.keySet()) {
			// Build the filename with the following format:
			// study_name + TRIAL_INSTANCE number + location_abbr +  dataset_type + dataset_name
			final String sanitizedFileName = FileUtils.sanitizeFileName(String
				.format(
					"%s_%s_%s_%s." + fileExtension, study.getName() + "-" + selectedDatasetInstancesMap.get(instanceDBID).getInstanceNumber(), selectedDatasetInstancesMap.get(instanceDBID).getLocationAbbreviation(),
					DataSetType.findById(dataSetDto.getDatasetTypeId()).getReadableName(), dataSetDto.getName()));
			final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;
			files.add(
				generator.generateSingleInstanceFile(study.getId(), dataSetDto, columns, observationUnitRowMap.get(instanceDBID), fileNameFullPath));
		}

		if(this instanceof DatasetKsuCSVExportServiceImpl || this instanceof DatasetKsuExcelExportServiceImpl) {
			final String sanitizedTraitsAndSelectionFilename = FileUtils.sanitizeFileName(String
				.format(
					"%s_%s_%s.trt", study.getName(), DataSetType.findById(dataSetDto.getDatasetTypeId()).getReadableName(),
					dataSetDto.getName()));
			final String traitsAndSelectionFilename =
				temporaryFolder.getAbsolutePath() + File.separator + sanitizedTraitsAndSelectionFilename;
			final List<MeasurementVariable> traitAndSelectionVariables = this.getTraitAndSelectionVariables(dataSetDto.getDatasetId());
			files.add(
				generator.generateTraitAndSelectionVariablesFile(this.convertTraitAndSelectionVariablesData(traitAndSelectionVariables), traitsAndSelectionFilename));
		}
		if (files.size() == 1) {
			return files.get(0);
		} else {
			return this.zipUtil.zipFiles(study.getName(), files);
		}
	}

	public void setZipUtil(final ZipUtil zipUtil) {
		this.zipUtil = zipUtil;
	}


	protected Map<Integer, StudyInstance> getSelectedDatasetInstancesMap(final List<StudyInstance> studyInstances, final Set<Integer> instanceIds) {
		Map<Integer, StudyInstance> studyInstanceMap = new HashMap<>();
		for(StudyInstance studyInstance: studyInstances) {
			if (instanceIds.contains(studyInstance.getInstanceDbId())) {
				studyInstanceMap.put(studyInstance.getInstanceDbId(), studyInstance);
			}
		}
		return studyInstanceMap;
	}

	protected List<String[]> convertTraitAndSelectionVariablesData(final List<MeasurementVariable> variables) {
		final List<String[]> data = new ArrayList<>();
		data.add(AbstractDatasetExportService.TRAIT_FILE_HEADERS);

		// get name of breeding method property and get all methods
		final String propertyName = this.ontologyDataManager.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
		final List<Method> methods = this.fieldbookService.getAllBreedingMethods(false);

		int index = 1;
		for (final MeasurementVariable variable : variables) {
			final List<String> traitAndSelectionVariablesData = new ArrayList<>();
			traitAndSelectionVariablesData.add(variable.getName());
			traitAndSelectionVariablesData.add(this.getDataTypeDescription(variable));
			// default value
			traitAndSelectionVariablesData.add("");
			if (variable.getMinRange() != null) {
				traitAndSelectionVariablesData.add(variable.getMinRange().toString());
			} else {
				traitAndSelectionVariablesData.add("");
			}
			if (variable.getMaxRange() != null) {
				traitAndSelectionVariablesData.add(variable.getMaxRange().toString());
			} else {
				traitAndSelectionVariablesData.add("");
			}
			traitAndSelectionVariablesData.add(""); // details
			traitAndSelectionVariablesData.add(this.getPossibleValuesString(propertyName, methods, variable));
			traitAndSelectionVariablesData.add("TRUE");
			traitAndSelectionVariablesData.add(String.valueOf(index));
			index++;
			data.add(traitAndSelectionVariablesData.toArray(new String[] {}));
		}

		return data;
	}

	protected String getPossibleValuesString(
		final String propertyName, final List<Method> methods, final MeasurementVariable variable) {
		final StringBuilder possibleValuesString = new StringBuilder();
		if (variable.getPossibleValues() != null && !variable.getPossibleValues().isEmpty()
			&& !variable.getProperty().equals(propertyName)) {
			for (final ValueReference value : variable.getPossibleValues()) {
				if (possibleValuesString.length() > 0) {
					possibleValuesString.append("/");
				}
				possibleValuesString.append(value.getName());
			}
		} else if (variable.getProperty().equals(propertyName)) {
			// add code for breeding method properties
			for (final Method method : methods) {
				if (possibleValuesString.length() > 0) {
					possibleValuesString.append("/");
				}
				possibleValuesString.append(method.getMcode());
			}
		}
		return possibleValuesString.toString();
	}

	protected String getDataTypeDescription(final MeasurementVariable variable) {
		Integer dataType = variable.getDataTypeId();
		if (variable.getDataTypeId() == null || !AbstractDatasetExportService.DATA_TYPE_LIST.contains(variable.getDataTypeId())) {
			dataType = 0;
		}
		return AbstractDatasetExportService.DATA_TYPE_FORMATS.get(dataType);
	}

	protected List<MeasurementVariable> getTraitAndSelectionVariables(final int datasetId) {
		return this.datasetService.getMeasurementVariables(datasetId, Lists.newArrayList(VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId()));
	}
	
	protected List<MeasurementVariable> moveSelectedVariableInTheFirstColumn(List<MeasurementVariable> columns, final int variableId) {
		int trialInstanceIndex = 0;
		for(MeasurementVariable column: columns) {
			if(variableId == column.getTermId()) {
				final MeasurementVariable trialInstanceMeasurementVariable = columns.remove(trialInstanceIndex);
				columns.add(0, trialInstanceMeasurementVariable);
				break;
			}
			trialInstanceIndex++;
		}
		return columns;

	}

	public List<MeasurementVariable> getColumns(final int studyId, final int datasetId) {
		final DatasetDTO dataSetDTO = this.datasetService.getDataset(datasetId);
		final int plotDatasetId = dataSetDTO.getParentDatasetId();

		final List<MeasurementVariable> plotDataSetColumns =
			this.datasetService
				.getMeasurementVariables(plotDatasetId,
					Lists.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.TREATMENT_FACTOR.getId(), VariableType.OBSERVATION_UNIT.getId()));
		final List<MeasurementVariable> subObservationSetColumns =
			this.datasetService
				.getMeasurementVariables(datasetId, Lists.newArrayList(
					VariableType.GERMPLASM_DESCRIPTOR.getId(),
					VariableType.OBSERVATION_UNIT.getId()));

		final List<MeasurementVariable> allVariables = new ArrayList<>();
		allVariables.addAll(plotDataSetColumns);
		allVariables.addAll(subObservationSetColumns);
		return this.moveSelectedVariableInTheFirstColumn(allVariables, TermId.OBS_UNIT_ID.getId());
	}

	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(final Study study, final DatasetDTO dataset, final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		return this.studyDatasetService.getInstanceObservationUnitRowsMap(study.getId(), dataset.getDatasetId(), new ArrayList<>(selectedDatasetInstancesMap.keySet()));
	}
}
