
package org.ibp.api.brapi.v1.trial;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.service.api.study.StudyFilters;
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
	private StudyService studyService;

	@ApiOperation(value = "List of trial summaries", notes = "Get a list of trial summaries.")
	@RequestMapping(value = "/{crop}/brapi/v1/trials", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<TrialSummaries> listTrialSummaries(@PathVariable final String crop,
			@ApiParam(value = "Program filter to only return studies associated with given program id.", required = false) @RequestParam(value = "programDbId", required = false) final String programDbId,
			@ApiParam(value = "Location filter to only return studies associated with given location id.", required = false) @RequestParam(value = "locationDbId", required = false) final String locationDbId,
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.", required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.", required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize,
			@ApiParam(value = "Filter active status true/false", required = false) @RequestParam(value = "active", required = false) final Boolean active,
			@ApiParam(value = "Sort order. Name of the field to sorty by.", required = false) @RequestParam(value = "sortBy", required = false) final String sortBy,
			@ApiParam(value = "Sort order direction. Ascending/Descending.", required = false) @RequestParam(value = "sortOrder", required = false) final String sortOrder) {

		final String validationError = parameterValidation(active, sortBy, sortOrder);
		if (!StringUtils.isBlank(validationError)) {
			Map<String, String> status = new HashMap<>();
			status.put("message", validationError);
			Metadata metadata = new Metadata(null, status);
			TrialSummaries trialSummaries = new TrialSummaries().withMetadata(metadata).withResult(new Result<TrialSummary>());
			return new ResponseEntity<>(trialSummaries, HttpStatus.NOT_FOUND);
		}

		final Map<StudyFilters, String> parameters = setParameters(programDbId, locationDbId, sortBy, sortOrder);
		final PagedResult<StudySummary> resultPage = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<StudySummary>() {

			@Override
			public long getCount() {
				return TrialResourceBrapi.this.studyService.countStudies(parameters);
			}

			@Override
			public List<StudySummary> getResults(PagedResult<StudySummary> pagedResult) {
				return TrialResourceBrapi.this.studyService.getStudies(parameters, pagedResult.getPageSize(), pagedResult.getPageNumber());
			}
		});

		final List<TrialSummary> trialSummaryList = translatedResults(resultPage);
		final Result<TrialSummary> results = new Result<TrialSummary>().withData(trialSummaryList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final TrialSummaries trialSummaries = new TrialSummaries().withMetadata(metadata).withResult(results);

		return new ResponseEntity<>(trialSummaries, HttpStatus.OK);

	}

	private List<TrialSummary> translatedResults(PagedResult<StudySummary> resultPage) {
		final ModelMapper modelMapper = TrialMapper.getInstance();
		final List<TrialSummary> trialSummaryList = new ArrayList<>();

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
		return trialSummaryList;
	}

	private Map<StudyFilters, String> setParameters(final String programDbId, final String locationDbId, final String sortByField,
		final String sortOrder) {

		final Map<StudyFilters, String> parametersMap = new EnumMap<>(StudyFilters.class);
		if (!StringUtils.isBlank(programDbId)) {
			parametersMap.put(StudyFilters.PROGRAM_ID, programDbId);
		}
		if (!StringUtils.isBlank(locationDbId)) {
			parametersMap.put(StudyFilters.LOCATION_ID, locationDbId);
		}

		if (!StringUtils.isBlank(sortByField)) {
			parametersMap.put(StudyFilters.SORT_BY_FIELD, translateNameField(sortByField));
		} else {
			parametersMap.put(StudyFilters.SORT_BY_FIELD, "projectId");

		}
		if (StringUtils.isBlank(sortOrder) || "Ascending".equalsIgnoreCase(sortOrder)) {
			parametersMap.put(StudyFilters.ORDER, "asc");
		} else if (!StringUtils.isBlank(sortOrder) && "Descending".equalsIgnoreCase(sortOrder)) {
			parametersMap.put(StudyFilters.ORDER, "desc");
		}
		return parametersMap;
	}

	private String translateNameField(final String sortByField) {
		final String nameField;
		switch (sortByField) {
			case "programDbId":
				nameField = "programUUID";
				break;
			case "trialName":
				nameField = "name";
				break;
			case "trialDbId":
				nameField = "projectId";
				break;
			default:
				nameField = "";
				break;
		}
		return nameField;
	}

	private String parameterValidation(final Boolean active, final String sortBy, final String sortOrder) {
		final List<String> sortbyFields =
			ImmutableList.<String>builder().add("trialDbId").add("trialName").add("programDbId").build();
		final List<String> sortOrders = ImmutableList.<String>builder().add("Ascending").add("Descending").build();

		if (active != null && !active) {
			return gerMessageError(1);
		}
		if (!StringUtils.isBlank(sortBy) && !sortbyFields.contains(sortBy)) {
			return gerMessageError(2);

		}
		if (!StringUtils.isBlank(sortOrder) && !sortOrders.contains(sortOrder)) {
			return gerMessageError(3);
		}
		return "";
	}

	private String gerMessageError(final Integer messageValue) {
		final String messageError;
		switch (messageValue) {
			case 1:
				messageError = "not found inactive studies";
				break;
			case 2:
				messageError = "sortBy bad filter, expect trialDbId/trialName/programDbId";
				break;
			case 3:
				messageError = "sortOrder bad filter, expect Ascending/Descending";
				break;
			default:
				messageError = "";
				break;
		}
		return messageError;
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
