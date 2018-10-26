package org.ibp.api.rest.dataset;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.java.impl.middleware.dataset.DatasetService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

@Api(value = "Dataset Services")
@Controller
public class DatasetResource {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetResource.class);

	@Autowired
	private DatasetService datasetService;

	@ApiOperation(value = "Generate and save a dataset", notes = "Returns the basic information for the generated dataset")
	@RequestMapping(value = "/crops/{cropname}/studies/{studyId}/datasets/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Observation> generateDataset(@PathVariable
	final String cropname, @PathVariable final Integer studyId,
			@PathVariable final Integer observationId, @RequestBody final Object o) {
		//datasetService.generateSubObservationDataset(null);
		return null;
	}

	@ApiOperation(value = "It will retrieve a list of datasets", notes = "Retrieves the list of datasets for the specified study.")
	@RequestMapping(value = "/{cropname}/studies/{studyId}/datasets/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<DatasetDTO>> getDatasets(
		@PathVariable final String cropname,
		@PathVariable final Integer studyId,
		@RequestParam(value = "filterByTypeIds", required = false) final Set<Integer> filterByTypeIds) {
		//return new ResponseEntity<>(this.datasetService.getDatasetByStudyId(studyId, filterByTypeIds), HttpStatus.OK);
		return null;
	}

	@ApiOperation(value = "It will retrieve all the observation units including observations and props values in a format that will be used by the Observations table.")
	@RequestMapping(value = "/crops/{cropname}/studies/{studyId}/datasets/{datasetId}/instances/{instanceId}/observationUnits/table", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ObservationUnitTable> getObservationUnitTable(@PathVariable final String cropname,
		@PathVariable final Integer studyId, @PathVariable final Integer datasetId,
		@PathVariable final Integer instanceId, @RequestParam(value = "pageNumber", required = false) final Integer pageNumber, //
		@ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.", required = false)
		//
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {
		final PagedResult<ObservationUnitRow> pageResult =
			new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<ObservationUnitRow>() {

				@Override
				public long getCount() {
					return DatasetResource.this.datasetService.countTotalObservationUnitsForDataset(datasetId, instanceId);
				}

				@Override
				public List<ObservationUnitRow> getResults(final PagedResult<ObservationUnitRow> pagedResult) {
					// BRAPI services have zero-based indexing for pages but paging for Middleware method starts at 1
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return DatasetResource.this.datasetService.getObservationUnitRows(
						studyId,
						datasetId,
						instanceId,
						pagedResult.getPageNumber(),
						pagedResult.getPageSize(),
						pagedResult.getSortBy(),
						pagedResult.getSortOrder());
				}
			});

		final ObservationUnitTable observationUnitTable = new ObservationUnitTable();
		observationUnitTable.setData(pageResult.getPageResults());
		observationUnitTable.setDraw("1");
		observationUnitTable.setRecordsTotal((int) pageResult.getTotalResults());
		observationUnitTable.setRecordsFiltered((int) pageResult.getTotalResults());
		return new ResponseEntity<>(observationUnitTable, HttpStatus.OK);
	}
}
