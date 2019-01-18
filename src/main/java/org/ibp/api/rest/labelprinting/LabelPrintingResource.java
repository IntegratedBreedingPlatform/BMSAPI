package org.ibp.api.rest.labelprinting;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.exception.NotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "Label Printing Services")
@RestController
public class LabelPrintingResource {

	@RequestMapping(value = "/crops/{cropname}/labelPrinting/{printingLabelType}/labels/summary", method = RequestMethod.GET)
	@ApiOperation(value = "Get Summary of Labels Needed according to the specified printing label type",
		notes = "Returns summary of labels needed according to the printing label type.")
	public ResponseEntity<LabelsNeededSummary> getLabelsNeededSummary (@PathVariable String cropname, @PathVariable Integer printingLabelType) {
		final LabelPrintingType labelPrintingType = LabelPrintingType.getEnumByCode(printingLabelType);
		if (labelPrintingType == null) {
			throw new NotSupportedException(null);
		}
		final LabelsNeededSummary labelsNeededSummary = new LabelsNeededSummary();
		final List<String> headers = new ArrayList<>();
		headers.add("Environment");
		headers.add("# of sub-obs units");
		headers.add("Labels needed");
		labelsNeededSummary.setHeaders(headers);

		final List<Map<String, String>> values = new ArrayList<>();
		Map<String, String> row1 = new HashMap<>();
		row1.put("Environment", "1");
		row1.put("# of sub-obs units", "20");
		row1.put("Labels needed", "20");
		values.add(row1);

		Map<String, String> row2 = new HashMap<>();
		row2.put("Environment", "2");
		row2.put("# of sub-obs units", "20");
		row2.put("Labels needed", "20");
		values.add(row2);

		labelsNeededSummary.setValues(values);

		return new ResponseEntity<>(labelsNeededSummary, HttpStatus.OK);
	}



}
