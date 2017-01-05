package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.location.Location;
import org.ibp.api.java.study.StudyService;
import org.modelmapper.ModelMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Autowired
	private StudyService studyService;

	@Autowired
	private LocationDataManager locationDataManager;

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
			@PathVariable final int studyDbId,
			@ApiParam(
					value = "Studies are contained within a trial. Provide the db id of the trial to list summary of studies within the trial. "
							+ "Use <code>GET /{crop}/brapi/v1/trials</code> service to retrieve trial summaries first to obtain trialDbIds to supply here. ",
					required = true) @RequestParam(value = "trialDbId", required = true) final int trialDbId) {

		StudyObservationTable studyObservationsTable = new StudyObservationTable();

		TrialObservationTable trialObservationTable = this.studyService.getTrialObservationTable(trialDbId, studyDbId);

		int resultNumber = (trialObservationTable == null) ? 0 : 1;

		if (resultNumber != 0) {
			ModelMapper modelMapper = new ModelMapper();
			studyObservationsTable = modelMapper.map(trialObservationTable, StudyObservationTable.class);
		}

		Pagination pagination =
				new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		Metadata metadata = new Metadata().withPagination(pagination);
		StudyObservations studyObservations = new StudyObservations().setMetadata(metadata).setResult(studyObservationsTable);
		return new ResponseEntity<>(studyObservations, HttpStatus.OK);
	}

	@ApiOperation(value = "Get study details", notes = "Get study details")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}", method = RequestMethod.GET)
	public ResponseEntity<StudyDetails> getStudyDetails(@PathVariable final String crop, @PathVariable final Integer studyDbId) {

		StudyDetails studyDetails = new StudyDetails();
		Metadata metadata = new Metadata();
		Pagination pagination = new Pagination().withPageNumber(1).withPageSize(0).withTotalCount(0L).withTotalPages(0);
		metadata.setPagination(pagination);
		metadata.setStatus(new HashMap<String, String>());
		studyDetails.setMetadata(metadata);

		final StudyDetailsDto mwStudyDetails = this.studyService.getStudyDetailsDto(studyDbId);
		if (mwStudyDetails != null) {
			final ModelMapper mapper = StudyMapper.getInstance();
			final StudyDetailsData result = mapper.map(mwStudyDetails, StudyDetailsData.class);

			if (mwStudyDetails.getMetadata().getLocationId() != null) {
				Map<String, String> filters = new HashMap<>();
				filters.put("locId", String.valueOf(mwStudyDetails.getMetadata().getLocationId()));
				List<LocationDetailsDto> locations = locationDataManager.getLocalLocationsByFilter(0, 1, filters);
				if (locations.size() > 0) {
					Location location = mapper.map(locations.get(0), Location.class);
					result.setLocation(location);
				}
			}
			studyDetails.setResult(result);

			return ResponseEntity.ok(studyDetails);
		} else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}

	}


}
