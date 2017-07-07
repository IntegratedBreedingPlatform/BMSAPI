
package org.ibp.api.brapi.v1.trial;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/#reference/trials">BrAPI Trial services</a>.
 */
@Api(value = "BrAPI Trial Services")
@Controller
public class TrialResourceBrapi {

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService studyService;

	@ApiOperation(value = "List of trial summaries", notes = "Get a list of trial summaries.")
	@RequestMapping(value = "/{crop}/brapi/v1/trials", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<TrialSummaries> listTrialSummaries(@PathVariable final String crop,
			@ApiParam(value = "Program filter to only return studies associated with given program id.", required = false) @RequestParam(value = "programDbId", required = false) final String programDbId,
			@ApiParam(value = "Location filter to only return studies associated with given location id.", required = false) @RequestParam(value = "locationDbId", required = false) final String locationDbId,
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.", required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.", required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize,
			@ApiParam(value = "Filter active status true/false", required = false) @RequestParam(value = "active", required = false) Boolean active,
			@ApiParam(value = "Sort order. Name of the field to sorty by.", required = false) @RequestParam(value = "sortBy", required = false) Boolean sortBy,
			@ApiParam(value = "Sort order direction. Ascending/Descending.", required = false) @RequestParam(value = "sortOrder", required = false) Boolean sortOrder) {

		final PagedResult<StudySummary> resultPage = new PaginatedSearch().execute(pageNumber, pageSize,
				new SearchSpec<StudySummary>() {

					@Override
					public long getCount() {
						return TrialResourceBrapi.this.studyDataManager.countAllStudies(programDbId, locationDbId,
								null);
					}

					@Override
					public List<StudySummary> getResults(PagedResult<StudySummary> pagedResult) {
						return TrialResourceBrapi.this.studyDataManager.findPagedProjects(programDbId, locationDbId,
								null, pagedResult.getPageSize(), pagedResult.getPageNumber());
					}
				});

		final List<TrialSummary> trialSummaryList = new ArrayList<>();
		final ModelMapper modelMapper = TrialMapper.getInstance();

		for (final StudySummary mwStudy : resultPage.getPageResults()) {
			final TrialSummary trialSummaryDto = modelMapper.map(mwStudy, TrialSummary.class);
			for (final InstanceMetadata instance : mwStudy.getInstanceMetaData()) {
				final StudySummaryDto studyMetadata = new StudySummaryDto();
				studyMetadata.setStudyDbId(instance.getInstanceDbId());
				studyMetadata.setStudyName(instance.getTrialName() + " Environment Number " + instance.getInstanceNumber());
				studyMetadata
					.setLocationName(instance.getLocationName() != null ? instance.getLocationName() : instance.getLocationAbbreviation());
				trialSummaryDto.addStudy(studyMetadata);
			}
			trialSummaryList.add(trialSummaryDto);
		}

		final Result<TrialSummary> results = new Result<TrialSummary>().withData(trialSummaryList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
				.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final TrialSummaries trialSummaries = new TrialSummaries().withMetadata(metadata).withResult(results);

		return new ResponseEntity<>(trialSummaries, HttpStatus.OK);

	}

	@ApiOperation(value = "Get trial observation details as table", notes = "Get trial observation details as table")
	@RequestMapping(value = "/{crop}/brapi/v1/trials/{trialDbId}/table", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<TrialObservations> getTrialObservationsAsTable(@PathVariable final String crop,
			@PathVariable final Integer trialDbId) {

		org.ibp.api.brapi.v1.trial.TrialObservationTable trialObservationsTable = new org.ibp.api.brapi.v1.trial.TrialObservationTable();

		TrialObservationTable mwTrialObservationTable = this.studyService.getTrialObservationTable(trialDbId);

		int resultNumber = (mwTrialObservationTable == null) ? 0 : 1;

		if (resultNumber != 0) {
			PropertyMap<TrialObservationTable, org.ibp.api.brapi.v1.trial.TrialObservationTable> mappingSpec = new PropertyMap<TrialObservationTable, org.ibp.api.brapi.v1.trial.TrialObservationTable>() {

				@Override
				protected void configure() {
					map(source.getStudyDbId(), destination.getTrialDbId());
				}
			};
			ModelMapper modelMapper = new ModelMapper();
			modelMapper.addMappings(mappingSpec);
			trialObservationsTable = modelMapper.map(mwTrialObservationTable, org.ibp.api.brapi.v1.trial.TrialObservationTable.class);
		}

		Pagination pagination =
				new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		Metadata metadata = new Metadata().withPagination(pagination);
		TrialObservations trialObservations = new TrialObservations().setMetadata(metadata).setResult(trialObservationsTable);
		return new ResponseEntity<>(trialObservations, HttpStatus.OK);
	}
}
