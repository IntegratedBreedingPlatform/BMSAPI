package org.ibp.api.rest.sample;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Sample Services")
@Controller
@RequestMapping("/crops")
@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'SAMPLE_LISTS')")
public class SampleResource {

	@Autowired
	public SampleService sampleService;

	@ApiOperation(value = "Get samples", notes = "Get samples")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/samples", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<SampleDTO>> filter(@PathVariable final String crop, @PathVariable final String programUUID,
        @RequestParam(required = false) @ApiParam(value = "The observation unit to which the samples belong") final String obsUnitId,
        @RequestParam(required = false) @ApiParam(value = "The list to which the samples belong") final Integer listId,
		// TODO describe in swagger?
		final Pageable pageable) {

		final PagedResult<SampleDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<SampleDTO>() {

				@Override
				public long getCount() {
					return SampleResource.this.sampleService.countFilter(obsUnitId, listId);
				}

				@Override
				public List<SampleDTO> getResults(final PagedResult<SampleDTO> pagedResult) {
					return SampleResource.this.sampleService.filter(obsUnitId, listId, pageable);
				}
			});

		final List<SampleDTO> samples = resultPage.getPageResults();

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));

		return new ResponseEntity<>(samples, headers, HttpStatus.OK);
	}
}
