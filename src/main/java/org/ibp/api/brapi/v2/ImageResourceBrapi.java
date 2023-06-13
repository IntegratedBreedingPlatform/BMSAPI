package org.ibp.api.brapi.v2;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.impl.middleware.common.validator.ImageValidator;
import org.ibp.api.java.impl.middleware.file.validator.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api("BrAPI v2 Image services")
@Controller(value = "ImageResourceBrapiV2")
public class ImageResourceBrapi {

	@Autowired
	private FileMetadataService fileMetadataService;

	@Autowired
	private FileValidator fileValidator;

	@Autowired
	private ImageValidator imageValidator;

	@ApiOperation("Create a new image meta data object")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/brapi/v2/images", method = RequestMethod.POST)
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<Image>> createImage(
		@PathVariable final String crop,
		@RequestBody final ImageNewRequest body
	) {
		this.fileValidator.validateFileStorage();
		this.imageValidator.validateImage(body);

		final Image result = this.fileMetadataService.createImage(body);
		final SingleEntityResponse<Image> response = new SingleEntityResponse<>();
		response.setResult(result);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

}
