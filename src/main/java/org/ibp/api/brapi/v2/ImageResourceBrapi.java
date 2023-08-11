package org.ibp.api.brapi.v2;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.impl.middleware.common.validator.ImageValidator;
import org.ibp.api.java.impl.middleware.file.validator.FileValidator;
import org.ibp.api.java.impl.middleware.permission.validator.BrapiPermissionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Api("BrAPI v2 Image services")
@Controller(value = "ImageResourceBrapiV2")
public class ImageResourceBrapi {

	@Autowired
	private FileMetadataService fileMetadataService;

	@Autowired
	private FileValidator fileValidator;

	@Autowired
	private ImageValidator imageValidator;

	@Autowired
	private BrapiPermissionValidator permissionValidator;

	@ApiOperation("Create a new image meta data object")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/brapi/v2/images", method = RequestMethod.POST)
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<Image>> createImage(
		@PathVariable final String crop,
		@RequestBody final List<ImageNewRequest> body
	) {

		this.fileValidator.validateFileStorage();

		final List<Image> imageList = new ArrayList<>();
		for (final ImageNewRequest imageNewRequest : body) {
			this.permissionValidator.validateProgramByObservationUnitDbId(crop, Arrays.asList(imageNewRequest.getObservationUnitDbId()), true);
			this.imageValidator.validateImage(imageNewRequest);
			imageList.add(this.fileMetadataService.createImage(imageNewRequest));
		}
		final Result<Image> results = new Result<Image>().withData(imageList);
		final EntityListResponse<Image> response = new EntityListResponse<>(results);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@ApiOperation("Update an image meta data")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/images/{imageDbId}", method = RequestMethod.PUT)
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<Image>> updateImage(
			@PathVariable final String cropName,
			@PathVariable("imageDbId") final String imageDbId,
			@RequestBody final ImageNewRequest body
	) {
		this.permissionValidator.validateProgramByObservationUnitDbId(cropName, Arrays.asList(body.getObservationUnitDbId()), true);

		this.fileValidator.validateFileStorage();
		this.imageValidator.validateImage(body);

		final Image result = this.fileMetadataService.updateImage(imageDbId, body);
		final SingleEntityResponse<Image> response = new SingleEntityResponse<>();
		response.setResult(result);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@ApiOperation("Update an image with the image file content")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/images/{imageDbId}/imagecontent",
			produces = {"application/json"},
			consumes = {"image/*"},
			method = RequestMethod.PUT)
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<Image>> updateImageContent(
			@PathVariable final String cropName,
			@PathVariable("imageDbId") final String imageDbId,
			@RequestBody final byte[] imageContent
	) {
		this.fileValidator.validateFileStorage();

		final Image result = this.fileMetadataService.updateImageContent(imageDbId, imageContent);
		final SingleEntityResponse<Image> response = new SingleEntityResponse<>();
		response.setResult(result);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}


}
