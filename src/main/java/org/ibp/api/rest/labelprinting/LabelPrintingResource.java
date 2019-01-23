package org.ibp.api.rest.labelprinting;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.exception.NotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "Label Printing Services")
@RestController
public class LabelPrintingResource {

	@Autowired
	private SubObservationDatasetLabelPrinting subObservationDatasetLabelPrinting;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@RequestMapping(value = "/crops/{cropname}/labelPrinting/{printingLabelType}/labels/summary", method = RequestMethod.POST)
	@ApiOperation(value = "Get Summary of Labels Needed according to the specified printing label type",
			notes = "Returns summary of labels needed according to the printing label type and input in the request body.")
	@ResponseBody
	public ResponseEntity<LabelsNeededSummaryResponse> getLabelsNeededSummary(
			@PathVariable
			String cropname,
			@PathVariable
			Integer printingLabelType,
			@RequestBody
			LabelsNeededSummaryInput labelsNeededSummaryInput) {

		final LabelPrintingType labelPrintingType = LabelPrintingType.getEnumByCode(printingLabelType);
		this.validateLabelPrintingType(labelPrintingType);

		final LabelPrintingStrategy labelPrintingStrategy = this.locateLabelPrintingImpl(labelPrintingType);
		labelPrintingStrategy.validateInputData(labelsNeededSummaryInput);
		final LabelsNeededSummary labelsNeededSummary = labelPrintingStrategy.getSummaryOfLabelsNeeded(labelsNeededSummaryInput);
		final LabelsNeededSummaryResponse labelsNeededSummaryResponse =
				labelPrintingStrategy.transformLabelsNeededSummary(labelsNeededSummary);

		return new ResponseEntity<>(labelsNeededSummaryResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/labelPrinting/{printingLabelType}/metadata", method = RequestMethod.POST)
	@ApiOperation(value = "Get the metadata according to the specified printing label type and the input in the request body",
			notes = "Returns summary of labels needed according to the printing label type and input in the request body.")
	@ResponseBody
	public ResponseEntity<Map<String, String>> getOriginResourceMetadada(
			@PathVariable
			String cropname,
			@PathVariable
			Integer printingLabelType,
			@RequestBody
			LabelsNeededSummaryInput labelsNeededSummaryInput) {

		final LabelPrintingType labelPrintingType = LabelPrintingType.getEnumByCode(printingLabelType);
		this.validateLabelPrintingType(labelPrintingType);

		final LabelPrintingStrategy labelPrintingStrategy = this.locateLabelPrintingImpl(labelPrintingType);
		labelPrintingStrategy.validateInputData(labelsNeededSummaryInput);
		final Map<String, String> metadata = labelPrintingStrategy.getOriginResourceMetadata(labelsNeededSummaryInput);

		return new ResponseEntity<>(metadata, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/labelPrinting/{printingLabelType}/labelTypes", method = RequestMethod.POST)
	@ApiOperation(value = "Get the list of available label fields according to the specified printing label type and the input in the request body",
		notes = "Returns list of available label fields grouped by type according to the printing label type and input in the request body.")
	@ResponseBody
	public ResponseEntity<List<LabelType>> getAvailableLabelFields(
		@PathVariable
			String cropname,
		@PathVariable
			Integer printingLabelType,
		@RequestBody
			LabelsNeededSummaryInput labelsNeededSummaryInput) {

		final LabelPrintingType labelPrintingType = LabelPrintingType.getEnumByCode(printingLabelType);
		this.validateLabelPrintingType(labelPrintingType);

		final LabelPrintingStrategy labelPrintingStrategy = this.locateLabelPrintingImpl(labelPrintingType);
		labelPrintingStrategy.validateInputData(labelsNeededSummaryInput);
		final List<LabelType> labelTypes = labelPrintingStrategy.getAvailableLabelFields(labelsNeededSummaryInput);

		return new ResponseEntity<>(labelTypes, HttpStatus.OK);
	}

	private LabelPrintingStrategy locateLabelPrintingImpl(final LabelPrintingType labelPrintingType) {
		switch (labelPrintingType) {
			case SUBOBSERVATION_DATASET:
				return subObservationDatasetLabelPrinting;
			default:
				return null;
		}
	}

	private void validateLabelPrintingType(final LabelPrintingType labelPrintingType) {
		if (labelPrintingType == null) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("label.printing.type.not.supported", "");
			throw new NotSupportedException(errors.getAllErrors().get(0));
		}
	}

}
