
package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.brapi.v1.observation.NewObservationRequest;
import org.generationcp.middleware.api.brapi.v1.observation.ObservationDTO;
import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v1.observation.ObservationVariableResult;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.exception.BrapiNotFoundException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Study services.
 */
@Api(value = "BrAPI Study Services")
@Controller
public class StudyResourceBrapi {

	public static final String CSV = "csv";
	private static final String TSV = "tsv";

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_DISPOSITION = "Content-Disposition";

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService studyService;

	@Autowired
	private VariableService variableService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private StudyInstanceService studyInstanceService;

	@ApiOperation(value = "List of studies", notes = "Get a list of studies.")
	@RequestMapping(value = "/{crop}/brapi/v1/studies", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<StudyInstanceDto>> listStudies(@PathVariable final String crop,
		@ApiParam(value = "Common name for the crop associated with this study.") @RequestParam(value = "commonCropName", required = false)
		final String commonCropName,
		@ApiParam(value = "Filter based on study type unique identifier") @RequestParam(value = "studyTypeDbId", required = false)
		final String studyTypeDbId,
		@ApiParam(value = "Program filter to only return studies associated with given program id.")
		@RequestParam(value = "programDbId", required = false) final String programDbId,
		@ApiParam(value = "Filter by location.") @RequestParam(value = "locationDbId", required = false) final String locationDbId,
		@ApiParam(value = "Filter by season.") @RequestParam(value = "seasonDbId", required = false) final String seasonDbId,
		@ApiParam(
			value =
				"Filter by trial.") @RequestParam(value = "trialDbId", required = false) final String trialDbId,
		@ApiParam(value = "Filter by study DbId") @RequestParam(value = "studyDbId", required = false) final String studyDbId,
		@ApiParam(value = "Filter active status true/false.") @RequestParam(value = "active", required = false, defaultValue = "true")
		final Boolean active,
		@ApiParam(value = "Name of the field to sort by.") @RequestParam(value = "sortBy", required = false) final String sortBy,
		@ApiParam(value = "Sort order direction. Ascending/Descending.") @RequestParam(value = "sortOrder", required = false)
		final String sortOrder,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final boolean isSortOrderValid = "ASC".equals(sortOrder) || "DESC".equals(sortOrder) || StringUtils.isEmpty(sortOrder);
		Preconditions.checkArgument(isSortOrderValid, "sortOrder should be either ASC or DESC");

		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest;

		if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize, new Sort(Sort.Direction.fromString(sortOrder), sortBy));
		} else {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize);
		}

		final StudySearchFilter studySearchFilter =
			new StudySearchFilter(studyTypeDbId, programDbId, locationDbId, seasonDbId, trialDbId, studyDbId, active);

		final PagedResult<StudyInstanceDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<StudyInstanceDto>() {

				@Override
				public long getCount() {
					return StudyResourceBrapi.this.studyService.countStudyInstances(studySearchFilter);
				}

				@Override
				public List<StudyInstanceDto> getResults(final PagedResult<StudyInstanceDto> pagedResult) {
					return StudyResourceBrapi.this.studyService.getStudyInstances(studySearchFilter, pageRequest);
				}
			});

		final List<StudyInstanceDto> summaryDtoList = resultPage.getPageResults();

		final Result<StudyInstanceDto> result = new Result<StudyInstanceDto>().withData(summaryDtoList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<StudyInstanceDto> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get study observation details as table", notes = "Get study observation details as table")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/table", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<StudyObservationTable>> getStudyObservationsAsTable(final HttpServletResponse response,
		@PathVariable final String crop, @PathVariable final int studyDbId,
		@ApiParam(value = "The format parameter will cause the data to be dumped to a file in the specified format",
			required = false) @RequestParam(value = "format", required = false) final String format)
		throws Exception {

		if (!StringUtils.isEmpty(format)) {
			if (StudyResourceBrapi.CSV.equalsIgnoreCase(format.trim())) {
				response.sendRedirect("/bmsapi/" + crop + "/brapi/v1/studies/" + studyDbId + "/table/csv");
				return new ResponseEntity<>(HttpStatus.OK);
			} else if (StudyResourceBrapi.TSV.equalsIgnoreCase(format.trim())) {

				response.sendRedirect("/bmsapi/" + crop + "/brapi/v1/studies/" + studyDbId + "/table/tsv");

				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", "Incorrect format"));
				final Metadata metadata = new Metadata(null, status);
				return new ResponseEntity<>(new SingleEntityResponse(metadata), HttpStatus.NOT_FOUND);
			}
		}

		return new ResponseEntity<>(this.getStudyObservations(studyDbId), HttpStatus.OK);
	}

	private SingleEntityResponse<StudyObservationTable> getStudyObservations(final int studyDbId) throws Exception {
		StudyObservationTable studyObservationsTable = new StudyObservationTable();

		final Integer trialDbId = this.studyDataManager.getProjectIdByStudyDbId(studyDbId);

		if (trialDbId == null) {
			throw new Exception("studyDbId " + studyDbId + " does not exist");
		}

		final TrialObservationTable trialObservationTable = this.studyService.getTrialObservationTable(trialDbId, studyDbId);

		final int resultNumber = trialObservationTable == null ? 0 : 1;

		if (resultNumber != 0) {
			final ModelMapper modelMapper = new ModelMapper();
			studyObservationsTable = modelMapper.map(trialObservationTable, StudyObservationTable.class);
		}

		final Pagination pagination =
			new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		final Metadata metadata = new Metadata().withPagination(pagination);

		return new SingleEntityResponse<>(metadata, studyObservationsTable);
	}

	@ApiOperation(value = "Get study details", notes = "Get study details")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}", method = RequestMethod.GET)
	@JsonView(BrapiView.BrapiV1_3.class)
	public ResponseEntity<SingleEntityResponse<StudyDetailsData>> getStudyDetails(@PathVariable final String crop, @PathVariable final Integer studyDbId) {

		final StudyDetailsDto mwStudyDetails = this.studyService.getStudyDetailsByGeolocation(studyDbId);
		if (Objects.isNull(mwStudyDetails)) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());;
			errors.reject("studydbid.invalid", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		//Add environment parameters to addtionalInfo
		final Map<String, String> additionalInfo = mwStudyDetails.getEnvironmentParameters().stream().collect(
				Collectors.toMap(MeasurementVariable::getName, MeasurementVariable::getValue));
		mwStudyDetails.getAdditionalInfo().putAll(additionalInfo);

		final Metadata metadata = new Metadata();
		final Pagination pagination = new Pagination().withPageNumber(1).withPageSize(1).withTotalCount(1L).withTotalPages(1);
		metadata.setPagination(pagination);
		final ModelMapper studyMapper = StudyMapper.getInstance();
		final StudyDetailsData result = studyMapper.map(mwStudyDetails, StudyDetailsData.class);

		if (mwStudyDetails.getMetadata().getLocationId() != null) {
			final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
			locationSearchRequest.setLocationIds(Collections.singletonList(mwStudyDetails.getMetadata().getLocationId()));
			final List<Location> locations = this.locationService.getLocations(locationSearchRequest, new PageRequest(0, 10));
			if (!locations.isEmpty()) {
				result.setLocation(locations.get(0));
			}
		}

		return ResponseEntity.ok(new SingleEntityResponse<>(metadata, result));
	}

	@ApiOperation(value = "", hidden = true)
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/table/csv", method = RequestMethod.GET)
	private ResponseEntity<FileSystemResource> streamCSV(@PathVariable final String crop, @PathVariable final Integer studyDbId)
		throws Exception {

		final File file = this.createDownloadFile(this.getStudyObservations(studyDbId).getResult(), ',', "studyObservations.csv");
		return StudyResourceBrapi.createResponseEntityForFileDownload(file);
	}

	@ApiOperation(value = "", hidden = true)
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/table/tsv", method = RequestMethod.GET)
	private ResponseEntity<FileSystemResource> streamTSV(@PathVariable final String crop, @PathVariable final Integer studyDbId)
		throws Exception {
		final File file = this.createDownloadFile(this.getStudyObservations(studyDbId).getResult(), '\t', "studyObservations.tsv");

		return StudyResourceBrapi.createResponseEntityForFileDownload(file);
	}

	private File createDownloadFile(final StudyObservationTable table, final char sep, final String pathname) throws IOException {
		// create mapper and schema
		final CsvMapper mapper = new CsvMapper();
		CsvSchema schema = mapper.schemaFor(List.class);
		schema = schema.withColumnSeparator(sep);

		// output writer
		final ObjectWriter myObjectWriter = mapper.writer(schema);
		final File resultFile = new File(pathname);
		final List<String> header = new ArrayList<>(table.getHeaderRow());

		final Object[] variableIds = table.getObservationVariableDbIds().toArray();
		final Object[] variableNames = table.getObservationVariableNames().toArray();
		for (int i = 0; i < variableIds.length; i++) {
			header.add(variableNames[i] + "|" + variableIds[i]);
		}

		final List<List<String>> data = table.getData();
		data.add(0, header);

		mapper.writeValue(resultFile, data);

		final FileOutputStream tempFileOutputStream = new FileOutputStream(resultFile);
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
		final OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, StandardCharsets.UTF_8);

		myObjectWriter.writeValue(writerOutputStream, data);
		return resultFile;
	}

	/**
	 * Creates ResponseEntity to download a file from a controller.
	 *
	 * @param file - file to be downloaded
	 * @return
	 */
	private static ResponseEntity<FileSystemResource> createResponseEntityForFileDownload(final File file) {

		final String filename = file.getName();
		final String fileWithFullPath = file.getAbsolutePath();
		final HttpHeaders respHeaders = new HttpHeaders();

		final File resource = new File(fileWithFullPath);
		final FileSystemResource fileSystemResource = new FileSystemResource(resource);

		final String mimeType = FileUtils.detectMimeType(filename);
		final String sanitizedFilename = FileUtils.sanitizeFileName(filename);

		respHeaders.set(StudyResourceBrapi.CONTENT_TYPE, String.format("%s;charset=utf-8", mimeType));
		respHeaders.set(StudyResourceBrapi.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"; filename*=utf-8''%s",
			sanitizedFilename, FileUtils.encodeFilenameForDownload(sanitizedFilename)));

		return new ResponseEntity<>(fileSystemResource, respHeaders, HttpStatus.OK);

	}

	@ApiOperation(value = "Get studies observation variables by studyDbId", notes = "Get studies observation variables by studyDbId")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/observationvariables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<VariableDTO>> getObservationVariables(final HttpServletResponse response,
		@PathVariable final String crop, @PathVariable final int studyDbId,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) throws BrapiNotFoundException {

		// Resolve the datasetId in which StudyDbId belongs to. (In BRAPI, studyDbId is nd_geolocation_id)
		final Optional<Integer> datasetIdForInstance = this.studyInstanceService.getDatasetIdForInstanceIdAndDatasetType(studyDbId, DatasetTypeEnum.PLOT_DATA);
		final Integer datasetId = datasetIdForInstance.isPresent() ? datasetIdForInstance.get() : null;
		if (datasetId == null) {
			throw new BrapiNotFoundException("The requested object studyDbId is not found.");
		}

		final PagedResult<VariableDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<VariableDTO>() {

				@Override
				public long getCount() {
					return StudyResourceBrapi.this.variableService.countVariablesByDatasetId(datasetId, Collections.unmodifiableList(
						Arrays.asList(VariableType.TRAIT.getId())));
				}

				@Override
				public List<VariableDTO> getResults(final PagedResult<VariableDTO> pagedResult) {
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return StudyResourceBrapi.this.variableService
						.getVariablesByDatasetId(datasetId, crop, Collections.unmodifiableList(
							Arrays.asList(VariableType.TRAIT.getId())), pagedResult.getPageSize(), pageNumber);
				}
			});

		final List<VariableDTO> observationVariables = resultPage.getPageResults();

		final String trialName = this.studyDataManager.getProject(datasetId).getStudy().getName();

		final ObservationVariableResult result =
			new ObservationVariableResult().withData(observationVariables).withStudyDbId(String.valueOf(studyDbId))
				.withTrialName(trialName);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<VariableDTO> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@JsonView(BrapiView.BrapiV1_3.class)
	@ApiOperation(value = "Get observation units by studyDbId")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/observationunits", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<PhenotypeSearchDTO>> listObservationUnitsByStudy(
		@PathVariable final String crop, @PathVariable final int studyDbId,
		@ApiParam(value = "The granularity level of observation units. see GET /observationlevels") @RequestParam(required = false)
		final String observationLevel,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION) @RequestParam(required = false) final Integer page,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION) @RequestParam(required = false) final Integer pageSize) {

		this.instanceValidator.validateStudyDbId(studyDbId);

		final Integer finalPageNumber = page == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : page;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PhenotypeSearchRequestDTO phenotypeSearchDTO = new PhenotypeSearchRequestDTO();
		phenotypeSearchDTO.setStudyDbIds(Lists.newArrayList(String.valueOf(studyDbId)));
		phenotypeSearchDTO.setObservationLevel(observationLevel);

		final BrapiPagedResult<PhenotypeSearchDTO> resultPage = new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize,
			new SearchSpec<PhenotypeSearchDTO>() {

				@Override
				public long getCount() {
					return StudyResourceBrapi.this.studyService.countPhenotypes(phenotypeSearchDTO);
				}

				@Override
				public List<PhenotypeSearchDTO> getResults(final PagedResult<PhenotypeSearchDTO> pagedResult) {
					return StudyResourceBrapi.this.studyService.searchPhenotypes(finalPageSize, finalPageNumber, phenotypeSearchDTO);
				}
			});

		final Result<PhenotypeSearchDTO> results = new Result<PhenotypeSearchDTO>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<PhenotypeSearchDTO> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "Put Observations", notes = "Put Observations")
	@RequestMapping(
		value = "/{crop}/brapi/v1/studies/{studyDbId}/observations",
		method = RequestMethod.PUT)
	public ResponseEntity<EntityListResponse<ObservationDTO>> putObservations(
		@PathVariable final String crop,
		@PathVariable final Integer studyDbId,
		@RequestBody final NewObservationRequest newObservationRequest) {

		this.studyDatasetService.importObservations(studyDbId, newObservationRequest.observations);

		final Result<ObservationDTO> results = new Result<ObservationDTO>().withObservations(newObservationRequest.observations);
		@SuppressWarnings("unchecked") final Metadata metadata = new Metadata().withStatus(
			Lists.newArrayList(Collections.singletonMap("ignored-fields", "collector, observationDbId, observationTimeStamp")));
		final EntityListResponse<ObservationDTO> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

}
