package org.ibp.api.brapi.v1.image;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.file.validator.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Api("BrAPI v1 Image services")
@RestController()
@RequestMapping("/{cropName}/brapi/v1")
public class ImageResourceBrapi {

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private FileMetadataService fileMetadataService;

	@Autowired
	private FileValidator fileValidator;

	@ApiOperation("Create a new image meta data object")
	@RequestMapping(value = "/images", method = RequestMethod.POST)
	public ResponseEntity<SingleEntityResponse<Image>> createImage(
		@PathVariable final String cropName,
		@RequestBody final ImageNewRequest body
	) {
		this.validateFileStorage();
		this.validateImage(body);

		final Image result = this.fileMetadataService.createImage(body);
		final SingleEntityResponse<Image> response = new SingleEntityResponse<>();
		response.setResult(result);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@ApiOperation("Update an image meta data")
	@RequestMapping(value = "/images/{imageDbId}", method = RequestMethod.PUT)
	public ResponseEntity<SingleEntityResponse<Image>> updateImage(
		@PathVariable final String cropName,
		@PathVariable("imageDbId") final String imageDbId,
		@RequestBody final ImageNewRequest body
	) {
		this.validateFileStorage();
		this.validateImage(body);

		final Image result = this.fileMetadataService.updateImage(imageDbId, body);
		final SingleEntityResponse<Image> response = new SingleEntityResponse<>();
		response.setResult(result);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@ApiOperation("Update an image with the image file content")
	@RequestMapping(value = "/images/{imageDbId}/imagecontent",
		produces = {"application/json"},
		consumes = {"image/*"},
		method = RequestMethod.PUT)
	public ResponseEntity<SingleEntityResponse<Image>> updateImageContent(
		@PathVariable final String cropName,
		@PathVariable("imageDbId") final String imageDbId,
		@RequestBody final byte[] imageContent
	) {
		this.validateFileStorage();

		final Image result = this.fileMetadataService.updateImageContent(imageDbId, imageContent);
		final SingleEntityResponse<Image> response = new SingleEntityResponse<>();
		response.setResult(result);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	private void validateFileStorage() {
		BaseValidator.checkArgument(this.fileStorageService.isConfigured(), "file.storage.not.configured");
	}

	private void validateImage(final ImageNewRequest body) {
		checkNotNull(body.getObservationUnitDbId(), "file.upload.brapi.images.observationunitdbid.required");
		final String imageFileName = body.getImageFileName();
		checkNotNull(imageFileName, "file.upload.brapi.images.filename.required");
		this.fileValidator.validateExtension(new MapBindingResult(new HashMap<>(), String.class.getName()), imageFileName);
	}
}
