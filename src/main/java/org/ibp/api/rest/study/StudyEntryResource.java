package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorRequestDto;
import org.generationcp.middleware.domain.study.StudyEntryPropertyBatchUpdateRequest;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.service.api.study.StudyEntryColumnDTO;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.study.StudyEntryDetailsImportRequest;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.study.StudyEntryMetadata;
import org.ibp.api.java.study.StudyEntryService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.data.domain.Pageable;
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

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.math.NumberUtils.isNumber;

// TODO: Move these services to StudyResource
@Api(value = "Study Entry Services")
@Controller
@RequestMapping("/crops")
public class StudyEntryResource {

	@Resource
	private StudyEntryService studyEntryService;

	@Resource
	private DatasetService datasetService;

	@ApiOperation(value = "Replace germplasm entry in study",
		notes = "Replace germplasm entry in study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/{entryId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'REPLACE_GERMPLASM')")
	@ResponseBody
	public ResponseEntity<Void> replaceStudyEntry(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer entryId, @RequestBody final StudyEntryDto studyEntryDto) {
		this.studyEntryService.replaceStudyEntry(studyId, entryId, studyEntryDto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Create germplasm entries in study based on the specified germplasm ids",
		notes = "Create germplasm entries in study based on the specified germplasm ids")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'ADD_NEW_ENTRIES')")
	@ResponseBody
	public ResponseEntity<Void> createStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID, @PathVariable final Integer studyId,
		@ApiParam("Study Entry template for batch generation. SearchComposite is a list of gids")
		@RequestBody final StudyEntryGeneratorRequestDto studyEntryGeneratorRequestDto) {

		this.studyEntryService.createStudyEntries(studyId, studyEntryGeneratorRequestDto);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Create germplasm entries in study based on the specified germplasm list id",
		notes = "Create germplasm entries in study based on the specified germplasm list id ")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/generation", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'ADD_NEW_ENTRIES')")
	@ResponseBody
	public ResponseEntity<Void> createStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID, @PathVariable final Integer studyId,
		@RequestParam(value = "listId", required = true) final Integer listId) {

		this.studyEntryService.createStudyEntries(studyId, listId);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Delete germplasm entries in study",
		notes = "Delete germplasm entries in study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'ADD_NEW_ENTRIES')")
	@ResponseBody
	public ResponseEntity deleteStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId) {

		this.studyEntryService.deleteStudyEntries(studyId);
		// TODO: this action must be moved to study PATH resource
		this.studyEntryService.fillWithCrossExpansion(studyId, null);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Update germplasm entries property",
		notes = "Update germplasm entries property")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/properties", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'MODIFY_ENTRY_DETAILS_VALUES')")
	@ResponseBody
	public ResponseEntity updateStudyEntriesProperty(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestBody final StudyEntryPropertyBatchUpdateRequest updateRequestDto) {

		this.studyEntryService.updateStudyEntriesProperty(studyId, updateRequestDto);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@ApiOperation(value = "Returns a paginated list of study entries",
		notes = "Returns a paginated list of study entries")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property(,asc|desc). " +
				"Default sort order is ascending. " +
				"Multiple sort criteria are supported.")
	})
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/entries", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<StudyEntryDto>> getStudyEntries(final @PathVariable String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody(required = false) final StudyEntrySearchDto searchDTO,
		@ApiIgnore final Pageable pageable) {

		final PagedResult<StudyEntryDto> pageResult =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<StudyEntryDto>() {

				@Override
				public long getCount() {
					return StudyEntryResource.this.studyEntryService.countAllStudyEntries(studyId);
				}

				@Override
				public long getFilteredCount() {
					final StudyEntrySearchDto.Filter filter = (Objects.isNull(searchDTO) ? null : searchDTO.getFilter());
					return StudyEntryResource.this.studyEntryService.countFilteredStudyEntries(studyId, filter);
				}

				@Override
				public List<StudyEntryDto> getResults(final PagedResult<StudyEntryDto> pagedResult) {
					final StudyEntrySearchDto.Filter filter = (Objects.isNull(searchDTO) ? null : searchDTO.getFilter());
					return StudyEntryResource.this.studyEntryService.getStudyEntries(studyId, filter, pageable);
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Filtered-Count", Long.toString(pageResult.getFilteredResults()));
		headers.add("X-Total-Count", Long.toString(pageResult.getTotalResults()));
		return new ResponseEntity<>(pageResult.getPageResults(), headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Entry Descriptors as Columns", notes = "Retrieves ALL MeasurementVariables associated to the entry plus "
		+ "some calculated inventory columns")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/entries/table/columns", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariable>> getEntryTableHeader(@PathVariable final String crop,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId) {

		final List<MeasurementVariable> entryDescriptors =
			this.studyEntryService.getEntryTableHeader(studyId);

		return new ResponseEntity<>(entryDescriptors, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Study Entries metadata",
		notes = "Get Study Entries metadata")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/entries/metadata", method = RequestMethod.GET)
	public ResponseEntity<StudyEntryMetadata> countStudyTestEntries(@PathVariable final String crop,
		@PathVariable final String programUUID, @PathVariable final Integer studyId) {

		return new ResponseEntity<>(this.studyEntryService.getStudyEntriesMetadata(studyId, programUUID), HttpStatus.OK);
	}

	@ApiOperation("Set generation level for study and fill with cross expansion")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/pedigree-generation-level", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Void> fillWithCrossExpansion(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final String programUUID,
		@RequestBody @ApiParam("a positive number, without quotation marks. E.g level: 2") final String level
	) {
		BaseValidator.checkArgument(isNumber(level), "error.generationlevel.invalid");
		final int levelInt = Integer.parseInt(level);
		BaseValidator.checkArgument(levelInt > 0 && levelInt <= 10, "error.generationlevel.max");
		this.studyEntryService.fillWithCrossExpansion(studyId, levelInt);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation("Get cross expansion level for study")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/pedigree-generation-level", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Integer> getCrossExpansionLevel(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final String programUUID) {
		final Integer crossExpansionLevel = this.studyEntryService.getCrossExpansionLevel(studyId);
		return new ResponseEntity<>(crossExpansionLevel, HttpStatus.OK);
	}

	@ApiIgnore
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/entries/columns", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyEntryColumnDTO>> getStudyEntriesColumns(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final String programUUID) {
		return new ResponseEntity<>(this.studyEntryService.getStudyEntryColumns(studyId, programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Get the variables associated to the study filtered by variableType", notes = "Get the list variables filtered by variableType")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/entries/variables", method = RequestMethod.GET)
	public ResponseEntity<List<Variable>> getVariables(
		@PathVariable final String cropName, @PathVariable final Integer studyId,
		@PathVariable final String programUUID, @RequestParam final Integer variableTypeId) {

		final List<Variable> variables =
			this.studyEntryService.getVariableListByStudyAndType(cropName, programUUID, studyId, variableTypeId);
		return new ResponseEntity<>(variables, HttpStatus.OK);
	}

	@ApiOperation(value = "Import Study Entry Details")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'IMPORT_ENTRY_DETAILS')")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/entries/import", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> importStudyEntryDetails(
		@PathVariable final String cropName, @PathVariable final Integer studyId,
		@PathVariable final String programUUID,
		@RequestBody final StudyEntryDetailsImportRequest studyEntryDetailsImportRequest
	) {
		if (studyEntryDetailsImportRequest.getData() != null && !studyEntryDetailsImportRequest.getData().isEmpty()) {
			this.studyEntryService.importUpdates(studyId, studyEntryDetailsImportRequest);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
