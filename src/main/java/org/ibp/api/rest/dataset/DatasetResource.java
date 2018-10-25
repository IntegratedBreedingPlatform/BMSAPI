package org.ibp.api.rest.dataset;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ibp.api.java.impl.middleware.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

	@ApiOperation(value = "It will retrieve a list of datasets", notes = "Retrieves the list of datasets for the specified study.")
	@RequestMapping(value = "/crops/{cropname}/studies/{studyId}/datasets/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<DatasetDTO>> getDatasets(
		@PathVariable final String cropname,
		@PathVariable final Integer studyId,
		@RequestParam(value = "filterByTypeIds", required = false) final Set<Integer> filterByTypeIds) {
		return new ResponseEntity<>(this.datasetService.getDatasetByStudyId(studyId, filterByTypeIds), HttpStatus.OK);
	}
}
