package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.MethodService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class BaseDatasetKsuExportService extends AbstractDatasetExportService {

	@Resource
	protected MethodService methodService;

	@Resource
	protected OntologyDataManager ontologyDataManager;

	@Resource
	protected DatasetTypeService datasetTypeService;

	public static String[] TRAIT_FILE_HEADERS = {
		"trait", "format", "defaultValue", "minimum",
		"maximum", "details", "categories", "isVisible", "realPosition"};

	public static final List<Integer> DATA_TYPE_LIST = Arrays.asList(
		TermId.NUMERIC_VARIABLE.getId(),
		TermId.CATEGORICAL_VARIABLE.getId(), TermId.DATE_VARIABLE.getId(), TermId.CHARACTER_VARIABLE.getId());

	public static final ImmutableMap<Integer, String> DATA_TYPE_FORMATS = ImmutableMap.<Integer, String>builder()
		.put(TermId.CATEGORICAL_VARIABLE.getId(), "categorical").put(TermId.NUMERIC_VARIABLE.getId(), "numeric")
		.put(TermId.DATE_VARIABLE.getId(), "date").put(TermId.CHARACTER_VARIABLE.getId(), "text")
		.put(0, "unrecognized").build();

	@Override
	protected File generateFiles(
		final Study study, final DatasetDTO dataSetDto,
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap,
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns,
		final DatasetFileGenerator generator, final String fileExtension)
		throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final List<File> files =
			this.getInstanceFiles(study, dataSetDto, selectedDatasetInstancesMap, observationUnitRowMap, columns, generator, fileExtension,
				temporaryFolder);

		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(dataSetDto.getDatasetTypeId());
		final String sanitizedTraitsAndSelectionFilename = FileUtils.sanitizeFileName(String
			.format(
				"%s_%s_%s", StringUtil.truncate(study.getName(), 35, true),
					StringUtil.truncate(datasetType.getName(), 10, true),
				StringUtil.truncate(dataSetDto.getName(), 35, true)));
		final String traitsAndSelectionFilename =
			temporaryFolder.getAbsolutePath() + File.separator + FileNameGenerator.generateFileName(sanitizedTraitsAndSelectionFilename, "trt");
		final List<MeasurementVariable> traitAndSelectionVariables = this.getTraitAndSelectionVariables(dataSetDto.getDatasetId());
		files.add(
			generator.generateTraitAndSelectionVariablesFile(this.convertTraitAndSelectionVariablesData(traitAndSelectionVariables),
				traitsAndSelectionFilename));

		return this.getReturnFile(study, files);
	}

	protected List<String[]> convertTraitAndSelectionVariablesData(final List<MeasurementVariable> variables) {
		final List<String[]> data = new ArrayList<>();
		data.add(BaseDatasetKsuExportService.TRAIT_FILE_HEADERS);

		// get name of breeding method property and get all methods
		final String propertyName = this.ontologyDataManager.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
		final List<Method> methods = this.methodService.getAllBreedingMethods();

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
		final List<String> possibleValues = new ArrayList<>();
		// For scenario where the possible values are cvterms
		if (!variable.getProperty().equals(propertyName) && !CollectionUtils.isEmpty(variable.getPossibleValues())) {
			for (final ValueReference value : variable.getPossibleValues()) {
				possibleValues.add(value.getName());
			}
			// For scenario where the possible values are breeding methods
		} else if (variable.getProperty().equals(propertyName)) {
			// add code for breeding method properties
			for (final Method method : methods) {
				possibleValues.add(method.getMcode());
			}
		}
		return StringUtils.join(possibleValues, "/");
	}

	protected String getDataTypeDescription(final MeasurementVariable variable) {
		Integer dataType = variable.getDataTypeId();
		if (variable.getDataTypeId() == null || !BaseDatasetKsuExportService.DATA_TYPE_LIST.contains(variable.getDataTypeId())) {
			dataType = 0;
		}
		return BaseDatasetKsuExportService.DATA_TYPE_FORMATS.get(dataType);
	}

	protected List<MeasurementVariable> getTraitAndSelectionVariables(final int datasetId) {
		return this.datasetService.getObservationSetVariables(datasetId, Lists.newArrayList(VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId()));
	}

	@Override
	public List<MeasurementVariable> getColumns(final int studyId, final int datasetId) {
		final DatasetDTO dataSetDTO = this.datasetService.getDataset(datasetId);
		final List<Integer> subObsDatasetTypeIds = this.datasetTypeService.getSubObservationDatasetTypeIds();

		final int plotDatasetId;
		if (dataSetDTO.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			plotDatasetId = dataSetDTO.getDatasetId();
		} else {
			plotDatasetId = dataSetDTO.getParentDatasetId();
		}

		final List<MeasurementVariable> plotDataSetColumns =
			this.datasetService
				.getObservationSetVariables(plotDatasetId,
					Lists.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.TREATMENT_FACTOR.getId(), VariableType.OBSERVATION_UNIT.getId()));

		final List<MeasurementVariable> allVariables = new ArrayList<>();
		allVariables.addAll(plotDataSetColumns);
		//Add variables that are specific to the sub-observation dataset types
		if (Arrays.stream(subObsDatasetTypeIds.toArray()).anyMatch(dataSetDTO.getDatasetTypeId()::equals)) {
			final List<MeasurementVariable> subObservationSetColumns =
				this.datasetService
					.getObservationSetVariables(datasetId, Lists.newArrayList(
						VariableType.GERMPLASM_DESCRIPTOR.getId(),
						VariableType.OBSERVATION_UNIT.getId()));
			allVariables.addAll(subObservationSetColumns);
		}
		return this.moveSelectedVariableInTheFirstColumn(allVariables, TermId.OBS_UNIT_ID.getId());
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(
		final Study study, final DatasetDTO dataset, final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		return this.studyDatasetService.getInstanceObservationUnitRowsMap(study.getId(), dataset.getDatasetId(),
			new ArrayList<>(selectedDatasetInstancesMap.keySet()));
	}

}
