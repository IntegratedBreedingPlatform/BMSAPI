package org.ibp.api.rest.labelprinting;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.exception.NotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Label Printing Services")
@RestController
public class LabelPrintingResource {

	@Autowired
	private SubObservationDatasetLabelPrinting subObservationDatasetLabelPrinting;

	@RequestMapping(value = "/crops/{cropname}/labelPrinting/{printingLabelType}/labels/summary", method = RequestMethod.POST)
	@ApiOperation(value = "Get Summary of Labels Needed according to the specified printing label type",
		notes = "Returns summary of labels needed according to the printing label type.")
	@ResponseBody
	public ResponseEntity<LabelsNeededSummaryResponse> getLabelsNeededSummary(
		@PathVariable String cropname, @PathVariable Integer printingLabelType,
		@RequestBody LabelsNeededSummaryInput labelsNeededSummaryInput) {
		final LabelPrintingType labelPrintingType = LabelPrintingType.getEnumByCode(printingLabelType);
		if (labelPrintingType == null) {
			throw new NotSupportedException(null);
		}
		final LabelPrintingStrategy labelPrintingStrategy = this.locateLabelPrintingImpl(labelPrintingType);
		labelPrintingStrategy.validateInputData(labelsNeededSummaryInput);
		final LabelsNeededSummary labelsNeededSummary = labelPrintingStrategy.getSummaryOfLabelsNeeded(labelsNeededSummaryInput);
		final LabelsNeededSummaryResponse labelsNeededSummaryResponse = labelPrintingStrategy.transformLabelsNeededSummary(labelsNeededSummary);

		return new ResponseEntity<>(labelsNeededSummaryResponse, HttpStatus.OK);
	}

	private LabelPrintingStrategy locateLabelPrintingImpl(final LabelPrintingType labelPrintingType) {
		switch (labelPrintingType) {
			case DATASET:
				return subObservationDatasetLabelPrinting;
			default:
				return null;
		}
	}

}
