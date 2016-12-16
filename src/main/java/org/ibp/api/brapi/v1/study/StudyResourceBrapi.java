package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a>
 * Study services.
 */
@Api(value = "BrAPI Study Services")
@Controller
public class StudyResourceBrapi {

	@SuppressWarnings("unused") // temporary
	@Autowired
	private StudyDataManager studyDataManager;

	@SuppressWarnings("unused") // temporary
	@Autowired
	private StudyService studyService;

	@ApiOperation(value = "List of study summaries", notes = "Get a list of study summaries.")
	@RequestMapping(value = "/{crop}/brapi/v1/studies", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudySummariesDto> listStudySummaries(@PathVariable final String crop,
			@ApiParam(
					value = "Studies are contained within a trial.  Provide the db id of the trial to list summary of studies within the trial. "
							+ "Use <code>GET /{crop}/brapi/v1/trials</code> service to retrieve trial summaries first to obtain trialDbIds to supply here. ",
					required = true) @RequestParam(value = "trialDbId", required = false) final String trialDbId,
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.",
					required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.",
					required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize) {

		/***
		 * Study in BrAPI land = Environment/Instance in BMS/Middleware land. We need to build services in Middleware to list all
		 * Environment/Instance metadata for a given trialDbId (required parameter). BrAPI does not yet have the trialDbId parameters but we
		 * need it here so we are going to add it and implement anyway. In future we may support taking array of trial Ids to list studies
		 * for. There is no point listing all instance/environment summaries across all trials.
		 * 
		 * studyDbId in BrAPI will map to nd_geolocation_id in Middleware.
		 * 
		 * For now, just returning an empty place holder message with status.
		 */

		Result<org.ibp.api.brapi.v1.study.StudySummaryDto> results = new Result<StudySummaryDto>();
		Pagination pagination = new Pagination();
		Metadata metadata =
				new Metadata().withPagination(pagination)
						.withStatus(Maps.newHashMap(ImmutableMap.of("message", "This call is not yet implemented.")));
		StudySummariesDto studiesList = new StudySummariesDto().setMetadata(metadata).setResult(results);

		return new ResponseEntity<>(studiesList, HttpStatus.OK);
	}

	@ApiOperation(value = "Get study observation details as table", notes = "Get study observation details as table")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/table", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudyObservations> getStudyObservationsAsTable(@PathVariable final String crop,
			@PathVariable final Integer studyDbId) {

		/***
		 * Study in BrAPI land = Environment/Instance in BMS/Middleware land. We need to build new services in Middleware to get
		 * Environment/Instance level measurement details as table.
		 * 
		 * studyDbId in BrAPI will map to nd_geolocation_id in Middleware.
		 * 
		 * For now, just returning an empty place holder message with status.
		 */
		StudyObservationTable brapiStudyDetailDto = new StudyObservationTable();

		Pagination pagination = new Pagination();
		Metadata metadata = new Metadata().withPagination(pagination)
				.withStatus(Maps.newHashMap(ImmutableMap.of("message", "This call is not yet implemented.")));
		StudyObservations studyDetailsDto = new StudyObservations().setMetadata(metadata).setResult(brapiStudyDetailDto);
		return new ResponseEntity<>(studyDetailsDto, HttpStatus.OK);
	}

}
