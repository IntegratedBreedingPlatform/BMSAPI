package org.ibp.api.rest.dataset;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.commons.ruleengine.naming.expression.ComponentPostProcessor;
import org.generationcp.commons.ruleengine.provider.PropertyFileRuleConfigurationProvider;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.SortedPageRequest;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.ruleengine.RulesPostProcessor;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Api(value = "Dataset Services")
@Controller
@RequestMapping("/crops")
public class DatasetResource {

	public static final String CSV = "csv";
	public static final String XLS = "xls";
	public static final String KSU_CSV = "ksu_csv";
	public static final String KSU_XLS = "ksu_xls";

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private DatasetExportService datasetCSVExportServiceImpl;

	@Autowired
	private DatasetExportService datasetExcelExportServiceImpl;

	@Autowired
	private DatasetExportService datasetKsuCSVExportServiceImpl;

	@Autowired
	private DatasetExportService datasetKsuExcelExportServiceImpl;

	@Autowired
	private PropertyFileRuleConfigurationProvider propertyFileRuleConfigurationProvider;

	@Autowired
	private RulesPostProcessor rulesPostProcessor;

	@ApiOperation(value = "Get Dataset Columns", notes = "Retrieves ALL MeasurementVariables (columns) associated to the dataset, "
		+ "that will be shown in the Observation Table")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/table/columns", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariable>> getSubObservationSetColumns(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestParam(required = false) final Boolean draftMode) {

		final List<MeasurementVariable> subObservationSetColumns = this.studyDatasetService.getSubObservationSetColumns(studyId, datasetId, draftMode);

		return new ResponseEntity<>(subObservationSetColumns, HttpStatus.OK);
	}

	@ApiOperation(value = "Count Phenotypes", notes = "Returns count of phenotypes for variables")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables/observations", method = RequestMethod.HEAD)
	public ResponseEntity<String> countPhenotypes(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {

		final long count = this.studyDatasetService.countPhenotypes(studyId, datasetId, Arrays.asList(variableIds));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Add Dataset Variable", notes = "Add Dataset Variable")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.PUT)
	public ResponseEntity<MeasurementVariable> addVariable(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final DatasetVariable datasetTrait) {

		final MeasurementVariable variable = this.studyDatasetService.addDatasetVariable(studyId, datasetId, datasetTrait);
		return new ResponseEntity<>(variable, HttpStatus.OK);
	}

	@ApiOperation(value = "Remove dataset variables", notes = "Remove a set of variables from dataset")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeVariables(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {
		this.studyDatasetService.removeVariables(studyId, datasetId, Arrays.asList(variableIds));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get the list of dataset variables filtered by variableType", notes = "Get the list of dataset variables filtered by variableType")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables/{variableTypeId}", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariableDto>> getVariables(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer variableTypeId) {
		final List<MeasurementVariableDto> variables = this.studyDatasetService.getVariables(studyId, datasetId, VariableType.getById(variableTypeId));
		return new ResponseEntity<>(variables, HttpStatus.OK);
	}

	@ApiOperation(value = "Add Observation", notes = "Add Observation")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}", method = RequestMethod.POST)
	public ResponseEntity<ObservationDto> addObservation(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer observationUnitId,
		@RequestBody final ObservationDto observation) {

		return new ResponseEntity<>(
			this.studyDatasetService.addObservation(studyId, datasetId, observationUnitId, observation), HttpStatus.OK);
	}

	@ApiOperation(value = "Update Observation", notes = "Update Observation")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}", method = RequestMethod.PATCH)
	public ResponseEntity<ObservationDto> updateObservation(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer observationUnitId, @PathVariable final Integer observationId,
		@ApiParam("Only some fields will be updated: ie. value, draftValue") @RequestBody final ObservationDto observationDto) {

		return new ResponseEntity<>(
			this.studyDatasetService.updateObservation(studyId, datasetId, observationId, observationUnitId, observationDto),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Count Phenotypes for specific instance (environment)", notes = "Returns count of phenotypes for specific instance (environment)")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{instanceId}", method = RequestMethod.HEAD)
	public ResponseEntity<String> countPhenotypesByInstance(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer instanceId) {

		final long count = this.studyDatasetService.countPhenotypesByInstance(studyId, datasetId, instanceId);
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Generate and save a sub-observation dataset", notes = "Returns the basic information for the generated dataset")
	@RequestMapping(value = "/{cropName}/studies/{studyId}/datasets/{parentId}/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<DatasetDTO> generateDataset(@PathVariable
	final String cropName, @PathVariable final Integer studyId, @PathVariable final Integer parentId, @RequestBody final DatasetGeneratorInput datasetGeneratorInput) {
		return new ResponseEntity<>(this.studyDatasetService.generateSubObservationDataset(cropName, studyId, parentId, datasetGeneratorInput), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve all the observation units", notes = "It will retrieve all the observation units including observations and props values in a format that will be used by the Observations table.")
	@RequestMapping(value = "/{cropname}/studies/{studyId}/datasets/{datasetId}/observationUnits/table", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ObservationUnitTable> getObservationUnitTable(@PathVariable final String cropname, //
		@PathVariable final Integer studyId, //
		@PathVariable final Integer datasetId, //
		@RequestBody  final ObservationUnitsSearchDTO searchDTO) {

		Preconditions.checkNotNull(searchDTO, "params cannot be null");
		final SortedPageRequest sortedRequest = searchDTO.getSortedRequest();
		Preconditions.checkNotNull(sortedRequest, "sortedRequest inside params cannot be null");

		final Integer pageNumber = sortedRequest.getPageNumber();
		final Integer pageSize = sortedRequest.getPageSize();

		final Integer instanceId = searchDTO.getInstanceId();
		final Boolean draftMode = searchDTO.getDraftMode();

		final PagedResult<ObservationUnitRow> pageResult =
			new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<ObservationUnitRow>() {

				@Override
				public long getCount() {
					return DatasetResource.this.studyDatasetService.countAllObservationUnitsForDataset(datasetId, instanceId, draftMode);
				}

				@Override
				public long getFilteredCount() {
					return DatasetResource.this.studyDatasetService
						.countFilteredObservationUnitsForDataset(datasetId, instanceId, searchDTO.getDraftMode(), searchDTO.getFilter());
				}

				@Override
				public List<ObservationUnitRow> getResults(final PagedResult<ObservationUnitRow> pagedResult) {
					return DatasetResource.this.studyDatasetService.getObservationUnitRows(studyId, datasetId, searchDTO);
				}
			});

		final ObservationUnitTable observationUnitTable = new ObservationUnitTable();
		observationUnitTable.setData(pageResult.getPageResults());
		observationUnitTable.setDraw(searchDTO.getDraw());
		observationUnitTable.setRecordsTotal((int) pageResult.getTotalResults());
		observationUnitTable.setRecordsFiltered((int) pageResult.getFilteredResults());
		return new ResponseEntity<>(observationUnitTable, HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a list of datasets", notes = "Retrieves the list of datasets for the specified study.")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets", method = RequestMethod.GET)
	public ResponseEntity<List<DatasetDTO>> getDatasets(@PathVariable final String crop, @PathVariable final Integer studyId,
		@RequestParam(value = "datasetTypeIds", required = false) final Set<Integer> datasetTypeIds) {
		return new ResponseEntity<>(this.studyDatasetService.getDatasets(studyId, datasetTypeIds), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a dataset given the id", notes = "Retrieves a dataset given the id")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}", method = RequestMethod.GET)
	public ResponseEntity<DatasetDTO> getDataset(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		return new ResponseEntity<>(this.studyDatasetService.getDataset(crop, studyId, datasetId), HttpStatus.OK);
	}

	@ApiOperation(value = "Delete Observation", notes = "Delete Observation")
	@RequestMapping(
			value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}",
			method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteObservation(@PathVariable final String crop, @PathVariable final Integer studyId,
			@PathVariable final Integer datasetId, @PathVariable final Integer observationUnitId,
			@PathVariable final Integer observationId) {
		this.studyDatasetService.deleteObservation(studyId, datasetId, observationUnitId, observationId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Put Observations Dataset", notes = "Put Observations Dataset")
	@RequestMapping(
			value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/observations",
			method = RequestMethod.PUT)
	public ResponseEntity<Void> postObservationUnits(@PathVariable final String crop, @PathVariable final Integer studyId,
			@PathVariable final Integer datasetId, @RequestBody final ObservationsPutRequestInput input) {
		this.studyDatasetService.importObservations(studyId, datasetId, input);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Retrieves all instances associated to the dataset", notes = "Retrieves all instances associated to the dataset")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/instances", method = RequestMethod.GET)
	public ResponseEntity<List<StudyInstance>> getDatasetInstances(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		return new ResponseEntity<>(this.studyDatasetService.getDatasetInstances(studyId, datasetId), HttpStatus.OK);
	}

	@ApiOperation(value = "Exports the dataset to a specified file type", notes = "Exports the dataset to a specified file type")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/{fileType}", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> exportDataset(
		@PathVariable final String crop,
		@PathVariable final Integer studyId, @PathVariable final Integer datasetId, @PathVariable final String fileType,

		@RequestParam(value = "instanceIds") final Set<Integer> instanceIds,
		@RequestParam(value = "collectionOrderId") final Integer collectionOrderId,
		@RequestParam(value = "singleFile") final boolean singleFile) {

		final DatasetExportService exportMethod = this.getExportFileStrategy(fileType);
		if (exportMethod != null) {
			final File file = exportMethod.export(studyId, datasetId, instanceIds, collectionOrderId, singleFile);
			return this.getFileSystemResourceResponseEntity(file);
		}

		return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
	}

	private DatasetExportService getExportFileStrategy(final String fileType) {
		final String trimmedFileType = fileType.trim();
		if (DatasetResource.CSV.equalsIgnoreCase(trimmedFileType)) {
			return this.datasetCSVExportServiceImpl;
		} else if (DatasetResource.XLS.equalsIgnoreCase(trimmedFileType)) {
			return this.datasetExcelExportServiceImpl;
		} else if (DatasetResource.KSU_CSV.equalsIgnoreCase(trimmedFileType)) {
			return this.datasetKsuCSVExportServiceImpl;
		} else if (DatasetResource.KSU_XLS.equalsIgnoreCase(trimmedFileType)) {
			return this.datasetKsuExcelExportServiceImpl;
		}
		return null;
	}

	private ResponseEntity<FileSystemResource> getFileSystemResourceResponseEntity(final File file) {
		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Move draft value to saved value in sub-observation dataset", notes = "Save information for the imported dataset")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/acceptance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> acceptDraftData(@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		this.studyDatasetService.acceptDraftData(studyId, datasetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Reject draft value in sub-observation dataset", notes = "Reject information for the imported dataset")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/rejection", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> rejectDraftData(@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		this.studyDatasetService.rejectDraftData(studyId, datasetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Check if exist draft values out of bounds in sub-observation dataset", notes = "Check out of bounds")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/out-of-bounds", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Void> checkOutOfBoundDraftData(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		final Boolean hasOutOfBounds = this.studyDatasetService.checkOutOfBoundDraftData(studyId, datasetId);

		if (hasOutOfBounds) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "Set missing value to saved value in sub-observation dataset", notes = "Set missing for the imported dataset")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/set-as-missing", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> setValuesToMissing(@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		this.studyDatasetService.setValuesToMissing(studyId, datasetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
