package org.ibp.api.rest.labelprinting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.labelprinting.LabelPrintingType;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.filegenerator.CSVLabelsFileGenerator;
import org.ibp.api.rest.labelprinting.filegenerator.ExcelLabelsFileGenerator;
import org.ibp.api.rest.labelprinting.filegenerator.LabelsFileGenerator;
import org.ibp.api.rest.labelprinting.filegenerator.PDFLabelsFileGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Api(value = "Label Printing Services")
@RestController
public class LabelPrintingResource {

	@Autowired
	private LabelPrintingStrategy observationDatasetLabelPrinting;

	@Autowired
	private LabelPrintingStrategy subObservationDatasetLabelPrinting;

	@Autowired
	private LabelPrintingStrategy lotLabelPrinting;

	@Autowired
	private CSVLabelsFileGenerator csvLabelsFileGenerator;

	@Autowired
	private PDFLabelsFileGenerator pdfLabelsFileGenerator;

	@Autowired
	private ExcelLabelsFileGenerator excelLabelsFileGenerator;

	@Autowired
	private HttpServletRequest request;


	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/labelPrinting/{labelPrintingType}/labels/summary", method = RequestMethod.POST)
	@ApiOperation(value = "Get Summary of Labels Needed according to the specified printing label type",
			notes = "Returns summary of labels needed according to the printing label type and input in the request body.")
	@ResponseBody
	public ResponseEntity<LabelsNeededSummaryResponse> getLabelsNeededSummary(
			@PathVariable final String cropname, @PathVariable final String programUUID,
			@PathVariable final String labelPrintingType,
			@RequestBody final LabelsInfoInput labelsInfoInput) {

		final LabelPrintingStrategy labelPrintingStrategy = this.getLabelPrintingStrategy(labelPrintingType);
		labelPrintingStrategy.validateLabelsInfoInputData(labelsInfoInput);
		final LabelsNeededSummary labelsNeededSummary = labelPrintingStrategy.getSummaryOfLabelsNeeded(labelsInfoInput);
		final LabelsNeededSummaryResponse labelsNeededSummaryResponse =
				labelPrintingStrategy.transformLabelsNeededSummary(labelsNeededSummary);

		return new ResponseEntity<>(labelsNeededSummaryResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/labelPrinting/{labelPrintingType}/metadata", method = RequestMethod.POST)
	@ApiOperation(value = "Get the metadata according to the specified printing label type and the input in the request body",
			notes = "Returns summary of labels needed according to the printing label type and input in the request body.")
	@ResponseBody
	public ResponseEntity<OriginResourceMetadata> getOriginResourceMetadada(
			@PathVariable final String cropname, @PathVariable final String programUUID,
			@PathVariable final String labelPrintingType,
			@RequestBody final LabelsInfoInput labelsInfoInput) {

		final LabelPrintingStrategy labelPrintingStrategy = this.getLabelPrintingStrategy(labelPrintingType);
		labelPrintingStrategy.validateLabelsInfoInputData(labelsInfoInput);
		final OriginResourceMetadata originResourceMetadata = labelPrintingStrategy.getOriginResourceMetadata(labelsInfoInput);

		return new ResponseEntity<>(originResourceMetadata, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/labelPrinting/{labelPrintingType}/labelTypes", method = RequestMethod.POST)
	@ApiOperation(value = "Get the list of available label fields according to the specified printing label type and the input in the request body",
			notes = "Returns list of available label fields grouped by type according to the printing label type and input in the request body.")
	@ResponseBody
	public ResponseEntity<List<LabelType>> getAvailableLabelFields(
		@PathVariable final String cropname, @PathVariable final String programUUID,
		@PathVariable final String labelPrintingType,
		@RequestBody final LabelsInfoInput labelsInfoInput) {

		final LabelPrintingStrategy labelPrintingStrategy = this.getLabelPrintingStrategy(labelPrintingType);
		labelPrintingStrategy.validateLabelsInfoInputData(labelsInfoInput);
		final List<LabelType> labelTypes = labelPrintingStrategy.getAvailableLabelTypes(labelsInfoInput);

		return new ResponseEntity<>(labelTypes, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/labelPrinting/{labelPrintingType}/labels/{fileExtension}", method = RequestMethod.POST)
	@ApiOperation(value = "Export the labels to a specified file type")
	@ResponseBody
	public ResponseEntity<FileSystemResource> getLabelsFile(
		@PathVariable final String cropname, @PathVariable final String programUUID,
		@PathVariable final String labelPrintingType,
		@PathVariable final String fileExtension,
		@RequestBody final LabelsGeneratorInput labelsGeneratorInput ) {

		final LabelPrintingStrategy labelPrintingStrategy = this.getLabelPrintingStrategy(labelPrintingType);
		final LabelsFileGenerator labelsFileGenerator = this.getLabelsFileGenerator(fileExtension, labelPrintingStrategy);

		labelPrintingStrategy.validateLabelsGeneratorInputData(labelsGeneratorInput);

		labelsGeneratorInput.setAllAvailablefields(labelPrintingStrategy.getAllAvailableFields(labelsGeneratorInput));

		final LabelsData labelsData = labelPrintingStrategy.getLabelsData(labelsGeneratorInput);

		final File file;
		try {
			file = labelsFileGenerator.generate(labelsGeneratorInput, labelsData);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.export.labelPrinting", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
		final HttpHeaders headers = new HttpHeaders();
		headers
				.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	public LabelPrintingStrategy getLabelPrintingStrategy(final String labelPrintingType) {
		final LabelPrintingType labelPrintingTypeEnum = LabelPrintingType.getEnumByCode(labelPrintingType);

		if (!this.hasAuthority(labelPrintingTypeEnum)) {
			throw new AccessDeniedException("");
		}

		if (labelPrintingTypeEnum == null) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("label.printing.type.not.supported", "");
			throw new NotSupportedException(errors.getAllErrors().get(0));
		}

		final LabelPrintingStrategy labelPrintingStrategy;

		switch (labelPrintingTypeEnum) {
			case OBSERVATION_DATASET:
				labelPrintingStrategy = this.observationDatasetLabelPrinting;
				break;
			case SUBOBSERVATION_DATASET:
				labelPrintingStrategy = this.subObservationDatasetLabelPrinting;
				break;
			case LOT:
				labelPrintingStrategy = this.lotLabelPrinting;
				break;
			default:
				labelPrintingStrategy = null;
		}

		return labelPrintingStrategy;
	}

	private boolean hasAuthority(final LabelPrintingType labelPrintingTypeEnum) {
		switch (labelPrintingTypeEnum) {
			case OBSERVATION_DATASET:
			case SUBOBSERVATION_DATASET:
				return this.request.isUserInRole(PermissionsEnum.ADMIN.name())
					|| this.request.isUserInRole(PermissionsEnum.BREEDING_ACTIVITIES.name())
					|| this.request.isUserInRole(PermissionsEnum.MANAGE_STUDIES.name());
			case LOT:
				return this.request.isUserInRole(PermissionsEnum.ADMIN.name())
					|| this.request.isUserInRole(PermissionsEnum.CROP_MANAGEMENT.name())
					|| this.request.isUserInRole(PermissionsEnum.MANAGE_INVENTORY.name())
					|| this.request.isUserInRole(PermissionsEnum.MANAGE_LOTS.name())
					|| this.request.isUserInRole(PermissionsEnum.LOT_LABEL_PRINTING.name());
			default:
				return false;
		}
	}

	LabelsFileGenerator getLabelsFileGenerator(final String fileExtension, final LabelPrintingStrategy labelPrintingStrategy) {
		final LabelsFileGenerator labelsFileGenerator;
		final FileType fileType = FileType.getEnum(fileExtension);
		if (fileType == null || !labelPrintingStrategy.getSupportedFileTypes().contains(fileType)) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("file.type.not.supported", "");
			throw new NotSupportedException(errors.getAllErrors().get(0));
		}
		switch (fileType) {
			case CSV:
				labelsFileGenerator = this.csvLabelsFileGenerator;
				break;
			case PDF:
				labelsFileGenerator = this.pdfLabelsFileGenerator;
				break;
			case XLS:
				// Generic Excel generator
				// It's possible to switch again here by strategy to return a specific generator
				labelsFileGenerator = this.excelLabelsFileGenerator;
				break;
			default:
				labelsFileGenerator = null;
		}
		return labelsFileGenerator;
	}

	public void setRequest(final HttpServletRequest request) {
		this.request = request;
	}

}
