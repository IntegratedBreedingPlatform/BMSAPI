package org.ibp.api.rest.study;

import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.pojos.SortedPageRequest;
import org.generationcp.middleware.service.api.study.StudyGermplasmSourceDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmSourceRequest;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.study.StudyGermplasmSourceTable;
import org.ibp.api.java.study.StudyGermplasmSourceService;
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
public class StudyGermplasmSourceResource {

	@Resource
	private StudyGermplasmSourceService studyGermplasmSourceService;

	@ApiOperation(value = "It will retrieve all generated germplasm in a study",
		notes = "It will retrieve all generated germplasm in a study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/germplasm-sources/table", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<StudyGermplasmSourceTable> getStudyGermplasmSourceTable(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestBody final StudyGermplasmSourceRequest studyGermplasmSourceRequest) {

		Preconditions.checkNotNull(studyGermplasmSourceRequest, "params cannot be null");
		final SortedPageRequest sortedRequest = studyGermplasmSourceRequest.getSortedRequest();
		Preconditions.checkNotNull(sortedRequest, "sortedRequest inside params cannot be null");

		final Integer pageNumber = sortedRequest.getPageNumber();
		final Integer pageSize = sortedRequest.getPageSize();
		final PagedResult<StudyGermplasmSourceDto> pageResult =
			new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<StudyGermplasmSourceDto>() {

				@Override
				public long getCount() {
					return StudyGermplasmSourceResource.this.studyGermplasmSourceService
						.countStudyGermplasmSourceList(studyGermplasmSourceRequest);
				}

				@Override
				public long getFilteredCount() {
					return StudyGermplasmSourceResource.this.studyGermplasmSourceService
						.countFilteredStudyGermplasmSourceList(studyGermplasmSourceRequest);
				}

				@Override
				public List<StudyGermplasmSourceDto> getResults(final PagedResult<StudyGermplasmSourceDto> pagedResult) {
					return StudyGermplasmSourceResource.this.studyGermplasmSourceService
						.getStudyGermplasmSourceList(studyGermplasmSourceRequest);
				}
			});

		final StudyGermplasmSourceTable studyGermplasmSourceTable = new StudyGermplasmSourceTable();
		studyGermplasmSourceTable.setData(pageResult.getPageResults());
		studyGermplasmSourceTable.setRecordsTotal((int) pageResult.getTotalResults());
		studyGermplasmSourceTable.setRecordsFiltered((int) pageResult.getFilteredResults());
		return new ResponseEntity<>(studyGermplasmSourceTable, HttpStatus.OK);

	}

}
