package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorRequestDto;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.domain.common.PagedResult;
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

// TODO: Move these services to StudyResource
@Api(value = "Study Entry Services")
@Controller
@RequestMapping("/crops")
public class StudyEntryResource {

	@Resource
	private StudyEntryService studyEntryService;

	@ApiOperation(value = "Replace germplasm entry in study",
		notes = "Replace germplasm entry in study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/{entryId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<StudyEntryDto> replaceStudyEntry(final @PathVariable String cropname,
														   @PathVariable final String programUUID,
														   @PathVariable final Integer studyId, @PathVariable final Integer entryId, @RequestBody final StudyEntryDto studyEntryDto) {
		return new ResponseEntity<>(this.studyEntryService.replaceStudyEntry(studyId, entryId, studyEntryDto),
			HttpStatus.OK);

	}

	@ApiOperation(value = "Create germplasm entries in study based on the specified germplasm ids",
		notes = "Create germplasm entries in study based on the specified germplasm ids")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<List<StudyEntryDto>> createStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID,	@PathVariable final Integer studyId,
		@ApiParam("Study Entry template for batch generation. SearchComposite is a list of gids")
		@RequestBody final StudyEntryGeneratorRequestDto studyEntryGeneratorRequestDto) {
		return new ResponseEntity<>(
			this.studyEntryService.createStudyEntries(studyId, studyEntryGeneratorRequestDto),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Create germplasm entries in study based on the specified germplasm list id",
		notes = "Create germplasm entries in study based on the specified germplasm list id ")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<StudyEntryDto>> createStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID,	@PathVariable final Integer studyId,
		@RequestParam(value = "listId", required = true) final Integer listId) {
		return new ResponseEntity<>(
			this.studyEntryService.createStudyEntries(studyId, listId),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Delete germplasm entries in study",
		notes = "Delete germplasm entries in study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId) {

		this.studyEntryService.deleteStudyEntries(studyId);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@ApiOperation(value = "Update germplasm entry property",
		notes = "Update germplasm entry property")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/{entryId}/properties/{propertyId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateStudyEntryProperty(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer entryId, @PathVariable final Integer propertyId,
		@RequestBody final StudyEntryPropertyData studyEntryPropertyData) {

		this.studyEntryService.updateStudyEntryProperty(studyId, entryId, studyEntryPropertyData);

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
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@ResponseBody
	public ResponseEntity<List<StudyEntryDto>> getStudyEntries(final @PathVariable String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody(required = false) final StudyEntrySearchDto searchDTO,
		@ApiIgnore final Pageable pageable) {

		final PagedResult<StudyEntryDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<StudyEntryDto>() {

				@Override
				public long getCount() {
					return StudyEntryResource.this.studyEntryService.countAllStudyEntries(studyId);
				}

				@Override
				public List<StudyEntryDto> getResults(final PagedResult<StudyEntryDto> pagedResult) {
					final StudyEntrySearchDto.Filter filter = (Objects.isNull(searchDTO) ? null : searchDTO.getFilter());
					return StudyEntryResource.this.studyEntryService.getStudyEntries(studyId, filter, pageable);
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		headers.add("X-Total-Pages", Long.toString(resultPage.getTotalPages()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Entry Descriptors as Columns", notes = "Retrieves ALL MeasurementVariables associated to the entry plus "
		+ "some calculated inventory columns")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/entries/table/columns", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariable>> getEntryTableColumns(@PathVariable final String crop,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId) {

		final List<MeasurementVariable> entryDescriptors =
			this.studyEntryService.getEntryDescriptorColumns(studyId);

		return new ResponseEntity<>(entryDescriptors, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Study Entries metadata",
		notes = "Get Study Entries metadata")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/entries/metadata", method = RequestMethod.GET)
	public ResponseEntity<StudyEntryMetadata> countStudyTestEntries(@PathVariable final String crop,
		@PathVariable final String programUUID,	@PathVariable final Integer studyId) {

		return new ResponseEntity<>(this.studyEntryService.getStudyEntriesMetadata(studyId, programUUID), HttpStatus.OK);
	}
}
