package org.ibp.api.rest.study;

import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.pojos.SortedPageRequest;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceRequest;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.study.GermplasmStudySourceTable;
import org.ibp.api.java.study.GermplasmStudySourceService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Study Germplasm Harvest Services")
@Controller
@RequestMapping("/crops")
public class GermplasmStudySourceResource {

	@Resource
	private GermplasmStudySourceService germplasmStudySourceService;

	@ApiOperation(value = "It will retrieve all generated germplasm in a study",
		notes = "It will retrieve all generated germplasm in a study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/germplasm-sources/table", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GermplasmStudySourceTable> getGermplasmStudySourceTable(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestBody final GermplasmStudySourceRequest germplasmStudySourceRequest) {

		Preconditions.checkNotNull(germplasmStudySourceRequest, "params cannot be null");
		final SortedPageRequest sortedRequest = germplasmStudySourceRequest.getSortedRequest();
		Preconditions.checkNotNull(sortedRequest, "sortedRequest inside params cannot be null");

		final Integer pageNumber = sortedRequest.getPageNumber();
		final Integer pageSize = sortedRequest.getPageSize();
		final PagedResult<GermplasmStudySourceDto> pageResult =
			new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<GermplasmStudySourceDto>() {

				@Override
				public long getCount() {
					return GermplasmStudySourceResource.this.germplasmStudySourceService
						.countGermplasmStudySourceList(germplasmStudySourceRequest);
				}

				@Override
				public long getFilteredCount() {
					return GermplasmStudySourceResource.this.germplasmStudySourceService
						.countFilteredGermplasmStudySourceList(germplasmStudySourceRequest);
				}

				@Override
				public List<GermplasmStudySourceDto> getResults(final PagedResult<GermplasmStudySourceDto> pagedResult) {
					return GermplasmStudySourceResource.this.germplasmStudySourceService
						.getGermplasmStudySourceList(germplasmStudySourceRequest);
				}
			});

		final GermplasmStudySourceTable germplasmStudySourceTable = new GermplasmStudySourceTable();
		germplasmStudySourceTable.setData(pageResult.getPageResults());
		germplasmStudySourceTable.setRecordsTotal((int) pageResult.getTotalResults());
		germplasmStudySourceTable.setRecordsFiltered((int) pageResult.getFilteredResults());
		return new ResponseEntity<>(germplasmStudySourceTable, HttpStatus.OK);

	}

}
