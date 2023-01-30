package org.ibp.api.rest.dataset;

import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fest.util.Collections;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dataset.PlotDatasetPropertiesDTO;
import org.generationcp.middleware.domain.dataset.ProjectPropertiesDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.dataset.FilteredPhenotypesInstancesCountDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitEntryReplaceRequest;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsParamDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.DatasetLock;
import org.ibp.api.java.impl.middleware.study.ObservationUnitsMetadata;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	private SearchRequestService searchRequestService;

	@Autowired
	protected ResourceBundleMessageSource messageSource;

	@Autowired
	private DatasetLock datasetLock;

	@ApiOperation(value = "Get Dataset Columns", notes = "Retrieves ALL MeasurementVariables (columns) associated to the dataset, "
		+ "that will be shown in the Observation Table")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/table/columns", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariable>> getObservationSetColumns(@PathVariable final String crop, @PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestParam(required = false) final Boolean draftMode) {

		final List<MeasurementVariable> observationSetColumns =
			this.studyDatasetService.getObservationSetColumns(studyId, datasetId, draftMode);

		return new ResponseEntity<>(observationSetColumns, HttpStatus.OK);
	}

	@ApiOperation(value = "Count Phenotypes", notes = "Returns count of phenotypes for variables")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/variables/observations", method = RequestMethod.HEAD)
	public ResponseEntity<String> countPhenotypes(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {

		final long count = this.studyDatasetService.countObservationsByVariables(studyId, datasetId, Arrays.asList(variableIds));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Add Dataset Variable", notes = "Add Dataset Variable")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.PUT)
	public ResponseEntity<MeasurementVariable> addVariable(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final DatasetVariable datasetVariable) {
		MeasurementVariable variable = null;
		try {
			// TODO: We need to find a better way to lock the specific dataset where the variable is added instead of the resource.
			this.datasetLock.lockWrite();
			variable = this.studyDatasetService.addDatasetVariable(studyId, datasetId, datasetVariable);
		} finally {
			this.datasetLock.unlockWrite();
		}
		return new ResponseEntity<>(variable, HttpStatus.OK);
	}

	@ApiOperation(value = "Remove dataset variables", notes = "Remove a set of variables from dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeVariables(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {

		this.studyDatasetService.removeDatasetVariables(studyId, datasetId, Arrays.asList(variableIds));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get the list of dataset variables filtered by variableType", notes = "Get the list of dataset variables filtered by variableType")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/variables/{variableTypeId}", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariableDto>> getVariables(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer variableTypeId) {

		final List<MeasurementVariableDto> variables =
			this.studyDatasetService.getDatasetVariablesByType(studyId, datasetId, VariableType.getById(variableTypeId));
		return new ResponseEntity<>(variables, HttpStatus.OK);
	}

	@ApiOperation(value = "Add Observation", notes = "Add Observation")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations", method = RequestMethod.POST)
	public ResponseEntity<ObservationDto> addObservation(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer observationUnitId,
		@RequestBody final ObservationDto observation) {

		return new ResponseEntity<>(
			this.studyDatasetService.createObservation(studyId, datasetId, observationUnitId, observation), HttpStatus.OK);
	}

	@ApiOperation(value = "Update Observation", notes = "Update Observation")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}", method = RequestMethod.PATCH)
	public ResponseEntity<ObservationDto> updateObservation(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer observationUnitId, @PathVariable final Integer observationId,
		@ApiParam("Only some fields will be updated: ie. value, draftValue") @RequestBody final ObservationDto observationDto) {

		return new ResponseEntity<>(
			this.studyDatasetService.updateObservation(studyId, datasetId, observationId, observationUnitId, observationDto),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Count Phenotypes for specific instance (environment)", notes = "Returns count of phenotypes for specific instance (environment)")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{instanceId}", method = RequestMethod.HEAD)
	public ResponseEntity<String> countPhenotypesByInstance(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer instanceId) {

		final long count = this.studyDatasetService.countObservationsByInstance(studyId, datasetId, instanceId);
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Generate and save a sub-observation dataset", notes = "Returns the basic information for the generated dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/{parentId}/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<DatasetDTO> generateDataset(@PathVariable final String cropName, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer parentId, @RequestBody final DatasetGeneratorInput datasetGeneratorInput) {

		return new ResponseEntity<>(
			this.studyDatasetService.generateSubObservationDataset(cropName, studyId, parentId, datasetGeneratorInput), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve all the observation units", notes = "It will retrieve all the observation units including observations and props values in a format that will be used by the Observations table.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/table", method = RequestMethod.POST)
	@ResponseBody
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	public ResponseEntity<List<ObservationUnitRow>> getObservationUnitTable(@PathVariable final String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, //
		@PathVariable final Integer datasetId, //
		@RequestBody final ObservationUnitsSearchDTO searchDTO,
		@ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {
		Preconditions.checkNotNull(searchDTO, "params cannot be null");

		final Boolean draftMode = searchDTO.getDraftMode();

		final PagedResult<ObservationUnitRow> pageResult =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<ObservationUnitRow>() {

				@Override
				public long getCount() {
					return DatasetResource.this.studyDatasetService.countAllObservationUnitsForDataset(datasetId, searchDTO.getInstanceIds(), draftMode);
				}

				@Override
				public long getFilteredCount() {
					return DatasetResource.this.studyDatasetService
						.countFilteredObservationUnitsForDataset(datasetId, searchDTO.getInstanceIds(), searchDTO.getDraftMode(), searchDTO.getFilter());
				}

				@Override
				public List<ObservationUnitRow> getResults(final PagedResult<ObservationUnitRow> pagedResult) {
					return DatasetResource.this.studyDatasetService.getObservationUnitRows(studyId, datasetId, searchDTO, pageable);
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Filtered-Count", Long.toString(pageResult.getFilteredResults()));
		headers.add("X-Total-Count", Long.toString(pageResult.getTotalResults()));
		return new ResponseEntity<>(pageResult.getPageResults(), headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Post observation-units search", notes = "Post observation-units search.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<SearchDto> postSearchObservation(@PathVariable final String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, //
		@PathVariable final Integer datasetId, //
		@RequestBody final ObservationUnitsSearchDTO observationUnitsSearchDTO) {

		final Locale locale = LocaleContextHolder.getLocale();
		Preconditions.checkNotNull(observationUnitsSearchDTO, this.getMessageSource().getMessage("parameters.cannot.be.null", null, locale));

		final String searchRequestId =
			this.searchRequestService.saveSearchRequest(observationUnitsSearchDTO, ObservationUnitsSearchDTO.class).toString();
		return new ResponseEntity<>(new SearchDto(searchRequestId), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve all the observation units in a simple JSON array table format",
		notes = "It will retrieve data from variables specified in filterColumns at observation/sub-observation level. Returns data as a simple JSON Array table format.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/mapList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getObservationUnitTableAsJSONArray(@PathVariable final String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final ObservationUnitsSearchDTO searchDTO) {

		Preconditions.checkNotNull(searchDTO, "params cannot be null");
		Preconditions.checkNotNull(searchDTO.getFilter(), "filter inside params cannot be null");

		return new ResponseEntity<>(this.studyDatasetService.getObservationUnitRowsAsMapList(studyId, datasetId, searchDTO), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a list of datasets", notes = "Retrieves the list of datasets for the specified study.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets", method = RequestMethod.GET)
	public ResponseEntity<List<DatasetDTO>> getDatasets(@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@RequestParam(value = "datasetTypeIds", required = false) final Set<Integer> datasetTypeIds) {

		return new ResponseEntity<>(this.studyDatasetService.getDatasets(studyId, datasetTypeIds), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a dataset given the id", notes = "Retrieves a dataset given the id")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}", method = RequestMethod.GET)
	public ResponseEntity<DatasetDTO> getDataset(@PathVariable final String crop, @PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {

		return new ResponseEntity<>(this.studyDatasetService.getDataset(crop, studyId, datasetId), HttpStatus.OK);
	}

	@ApiOperation(value = "Delete Observation", notes = "Delete Observation")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(
		value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}",
		method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteObservation(@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer observationUnitId,
		@PathVariable final Integer observationId) {

		this.studyDatasetService.deleteObservation(studyId, datasetId, observationUnitId, observationId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Put Observations Dataset", notes = "Put Observations Dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(
		value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/observations",
		method = RequestMethod.PUT)
	public ResponseEntity<Void> postObservationUnits(@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final ObservationsPutRequestInput input) {

		this.studyDatasetService.importObservations(studyId, datasetId, input);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Retrieves all instances associated to the dataset", notes = "Retrieves all instances associated to the dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/instances", method = RequestMethod.GET)
	public ResponseEntity<List<StudyInstance>> getDatasetInstances(@PathVariable final String crop, @PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {

		return new ResponseEntity<>(this.studyDatasetService.getDatasetInstances(studyId, datasetId), HttpStatus.OK);
	}

	@ApiOperation(value = "Exports the dataset to a specified file type", notes = "Exports the dataset to a specified file type")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')"  + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/{fileType}", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> exportDataset(
		@PathVariable final String crop, @PathVariable final String programUUID,
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
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Move draft value to saved value in sub-observation dataset", notes = "Save information for the imported dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/acceptance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> acceptDraftData(@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {

		this.studyDatasetService.acceptAllDatasetDraftData(studyId, datasetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Reject draft value in sub-observation dataset", notes = "Reject information for the imported dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/rejection", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> rejectDraftData(@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {

		this.studyDatasetService.rejectDatasetDraftData(studyId, datasetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Check if exist draft values out of bounds in sub-observation dataset", notes = "Check out of bounds")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/out-of-bounds", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Void> checkOutOfBoundDraftData(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		final Boolean hasOutOfBounds = this.studyDatasetService.hasDatasetDraftDataOutOfBounds(studyId, datasetId);

		if (hasOutOfBounds) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "Set missing value to saved value in sub-observation dataset", notes = "Set missing for the imported dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/set-as-missing", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> setValuesToMissing(@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {

		this.studyDatasetService.acceptDraftDataAndSetOutOfBoundsToMissing(studyId, datasetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Count Filtered Phenotypes and Instances per Variable", notes = "Returns count of phenotypes for variables")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/observations/filter/count", method = RequestMethod.POST)
	public ResponseEntity<FilteredPhenotypesInstancesCountDTO> countFilteredInstancesAndObservationUnits(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final ObservationUnitsSearchDTO filterParams) {

		final FilteredPhenotypesInstancesCountDTO
			result = this.studyDatasetService.countFilteredInstancesAndObservationUnits(studyId, datasetId, filterParams);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ApiOperation(value = "Move draft value to saved value in sub-observation dataset", notes = "Save information for the imported dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/filter/acceptance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> acceptDraftDataByVariable(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final ObservationUnitsSearchDTO searchDTO) {

		this.studyDatasetService.acceptDraftDataFilteredByVariable(studyId, datasetId, searchDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Set value to the selected variable", notes = "Set value to the selected variable")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/filter/set-value", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> setValueToVariable(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final ObservationUnitsParamDTO paramDTO) {

		this.studyDatasetService.setValueToVariable(studyId, datasetId, paramDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Delete values of the selected variable", notes = "Delete values of the selected variable")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/filter/delete-value", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> deleteVariableValues(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final ObservationUnitsSearchDTO observationUnitsSearchDTO) {

		this.studyDatasetService.deleteVariableValues(studyId, datasetId, observationUnitsSearchDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get all Dataset properties", notes = "Get all Dataset properties")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/properties", method = RequestMethod.GET)
	public ResponseEntity<ProjectPropertiesDTO> getAllproperties(@PathVariable final String crop, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer datasetId) {
		final ProjectPropertiesDTO projectPropertiesDTO = new ProjectPropertiesDTO();
		projectPropertiesDTO.setVariables(this.studyDatasetService.getAllDatasetVariables(studyId, datasetId));
		projectPropertiesDTO.setNameTypes(this.studyDatasetService.getAllPlotDatasetNameTypes(datasetId));
		return new ResponseEntity<>(projectPropertiesDTO, HttpStatus.OK);
	}

	@ApiOperation(value = "Replace the entry (gid) for a set of observation units", notes = "Replace the entry (gid) for a set of observation units")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/entries", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> updateObservationUnitsEntry(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final ObservationUnitEntryReplaceRequest request) {

		this.studyDatasetService.replaceObservationUnitsEntry(studyId, datasetId, request);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get metadata for a set of observation units", notes = "Get metadata for a set of observation units")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/metadata", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ObservationUnitsMetadata> getObservationUnitsMetadata(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> request) {

		return new ResponseEntity<>(this.studyDatasetService.getObservationUnitsMetadata(studyId, datasetId, request), HttpStatus.OK);
	}

	@ApiOperation( value = "Count observation units of dataset", notes = "Returns count of observation units of dataset")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units", method = RequestMethod.HEAD)
	public ResponseEntity<String> countObservationUnits(
		@PathVariable final String crop, @PathVariable final String programUUID, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {

		final Long count = this.studyDatasetService.countObservationUnits(datasetId);
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Dataset-Observation-Unit", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiIgnore
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/plot-datasets/properties", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Void> updatePlotDatasetProperties(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final String programUUID,
		@RequestBody final PlotDatasetPropertiesDTO plotDatasetPropertiesDTO) {
		this.studyDatasetService.updatePlotDatasetProperties(studyId, plotDatasetPropertiesDTO, programUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation( value = "Returns the variables associated to the given study filtered by a given variable types")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/variables/types/{variableTypeIds}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MeasurementVariable>> getStudyVariablesByVariableTypes(@PathVariable final String crop,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final List<Integer> variableTypeIds) {
		final List<MeasurementVariable> variables = this.studyDatasetService.getVariablesByVariableTypes(studyId, variableTypeIds);
		return new ResponseEntity<>(variables, HttpStatus.OK);
	}

	public ResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

}

